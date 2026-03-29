package io.dhoondlay.shield.service;

import io.dhoondlay.shield.config.ShieldProperties;
import io.dhoondlay.shield.entity.AuditLog;
import io.dhoondlay.shield.entity.DownstreamConfig;
import io.dhoondlay.shield.model.DetectionMatch;
import io.dhoondlay.shield.model.ShieldResult;
import io.dhoondlay.shield.model.ThreatSeverity;
import io.dhoondlay.shield.repository.AuditLogRepository;
import io.dhoondlay.shield.repository.DownstreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;

/**
 * Core reactive redaction pipeline.
 * Processes high-concurrency requests in a non-blocking manner.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedactionService {

    private final ShieldScanner scanner;
    private final DownstreamProxyClient proxyClient;
    private final DownstreamRepository downstreamRepository;
    private final AuditLogRepository auditLogRepository;
    private final ShieldProperties shieldProperties;

    // -------------------------------------------------------------------------

    public Mono<ShieldResult> process(String rawText, String action, String forwardToAlias) {
        return Mono.defer(() -> {
            long start = System.currentTimeMillis();
            String effectiveAction = (action == null || action.isBlank()) ? "R" : action.toUpperCase();

            // 1. Multi-threaded Parallel Scan (CPU-intensive)
            return scanner.scanParallel(rawText).flatMap(matches -> {
                int riskScore = Math.min(100, matches.stream().mapToInt(DetectionMatch::riskWeight).sum());
                ThreatSeverity severity = calculateSeverity(riskScore);
                List<String> detections = matches.stream().map(DetectionMatch::patternName).distinct().toList();

                log.info("[MULTI-THREAD-100K] action={} matches={} risk={} severity={}", effectiveAction, matches.size(), riskScore, severity);

                // Analyze only (A)
                if ("A".equals(effectiveAction)) {
                    return finalizeResult(rawText, null, severity, riskScore, detections, false, start, effectiveAction, forwardToAlias);
                }

                // Apply redactions for R and F
                String sanitizedText = applyRedactions(rawText, matches);

                // Forward (F)
                if ("F".equals(effectiveAction)) {
                    if (severity == ThreatSeverity.CRITICAL && shieldProperties.isBlockCriticalRisk()) {
                        return finalizeResult(sanitizedText, "BLOCKED: Critical risk score (" + riskScore + ").", severity, riskScore, detections, false, start, effectiveAction, forwardToAlias);
                    }

                    return resolveDownstreamReactive(forwardToAlias)
                            .flatMap(config -> proxyClient.forwardToDownstream(config, sanitizedText)
                                    .map(llmResp -> new ProcessingResult(sanitizedText, llmResp, true, config.getAlias()))
                                    .timeout(java.time.Duration.ofSeconds(60))
                                    .onErrorResume(e -> {
                                        log.error("Downstream failure: {}", e.getMessage());
                                        return Mono.just(new ProcessingResult(sanitizedText, "Error: service temporarily unavailable — " + e.getMessage(), false, forwardToAlias));
                                    }))
                            .defaultIfEmpty(new ProcessingResult(sanitizedText, "Error: no active downstream gateway found", false, null))
                            .flatMap(res -> finalizeResult(res.sanitizedText, res.llmResponse, severity, riskScore, detections, res.wasProxied, start, effectiveAction, res.actualAlias));
                }

                // Default: Redact only (R)
                return finalizeResult(sanitizedText, null, severity, riskScore, detections, false, start, effectiveAction, null);
            });
        });
    }

    private Mono<ShieldResult> finalizeResult(String sanitizedText, String llmResponse, ThreatSeverity severity, int riskScore, List<String> detections, boolean wasProxied, long startTime, String action, String alias) {
        long latency = System.currentTimeMillis() - startTime;
        ShieldResult result = new ShieldResult(sanitizedText, llmResponse, severity, riskScore, detections, wasProxied, latency);

        // Blocking Audit Logging offloaded to boundedElastic thread pool
        return Mono.fromRunnable(() -> {
            auditLogRepository.save(AuditLog.builder()
                .action(action)
                .severity(severity.name())
                .riskScore(riskScore)
                .detectedPatterns(String.join(", ", detections))
                .inputLength(sanitizedText.length())
                .sanitizedLength(sanitizedText.length())
                .proxied(wasProxied)
                .latencyMs(latency)
                .downstreamAlias(alias)
                .build());
        }).subscribeOn(Schedulers.boundedElastic())
        .thenReturn(result);
    }

    private Mono<DownstreamConfig> resolveDownstreamReactive(String alias) {
        return Mono.fromCallable(() -> {
            if (alias != null && !alias.isBlank()) {
                return downstreamRepository.findByAliasAndEnabledTrue(alias).orElse(null);
            }
            return downstreamRepository.findFirstByEnabledTrue().orElse(null);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // -------------------------------------------------------------------------

    /**
     * Foolproof interval merging algorithm for robust redaction.
     * Prevents StringIndexOutOfBounds and corrupted data when multiple
     * detectors find overlapping PII (e.g., An Email embedded near a Phone number).
     */
    private String applyRedactions(String text, List<DetectionMatch> matches) {
        if (matches.isEmpty()) return text;

        // 1. Sort matches by Start Index (Ascending). If starts are equal, sort by End Index.
        List<DetectionMatch> sorted = new java.util.ArrayList<>(matches);
        sorted.sort(Comparator.comparingInt(DetectionMatch::start)
                .thenComparingInt(DetectionMatch::end));

        // 2. Merge overlapping intervals
        List<DetectionMatch> mergedMatches = new java.util.ArrayList<>();
        DetectionMatch current = sorted.get(0);

        for (int i = 1; i < sorted.size(); i++) {
            DetectionMatch next = sorted.get(i);
            
            // If overlapping or contiguous
            if (next.start() <= current.end()) {
                // Determine highest risk placeholder or combined placeholder
                String mergedPlaceholder = current.riskWeight() >= next.riskWeight() ? current.placeholder() : next.placeholder();
                
                current = new DetectionMatch(
                    current.detectorName().equals(next.detectorName()) ? current.detectorName() : "mixed",
                    current.patternName() + "+" + next.patternName(), // audit tracking combo
                    current.start(),
                    Math.max(current.end(), next.end()),
                    text.substring(current.start(), Math.max(current.end(), next.end())),
                    Math.max(current.riskWeight(), next.riskWeight()),
                    mergedPlaceholder
                );
            } else {
                mergedMatches.add(current);
                current = next;
            }
        }
        mergedMatches.add(current);

        // 3. Apply redactions from end-to-start (reverse order) to preserve indices
        mergedMatches.sort(Comparator.comparingInt(DetectionMatch::start).reversed());
        
        StringBuilder sb = new StringBuilder(text);
        for (DetectionMatch match : mergedMatches) {
            if (match.end() <= sb.length()) {
                sb.replace(match.start(), match.end(), match.placeholder());
            }
        }
        
        return sb.toString();
    }

    private ThreatSeverity calculateSeverity(int score) {
        if (score >= 75) return ThreatSeverity.CRITICAL;
        if (score >= 50) return ThreatSeverity.HIGH;
        if (score >= 25) return ThreatSeverity.MEDIUM;
        if (score > 0) return ThreatSeverity.LOW;
        return ThreatSeverity.CLEAN;
    }

    private record ProcessingResult(String sanitizedText, String llmResponse, boolean wasProxied, String actualAlias) {}
}

package io.dhoondlay.shield.api;

import io.dhoondlay.shield.detector.DynamicDatabaseDetector;
import io.dhoondlay.shield.entity.AuditLog;
import io.dhoondlay.shield.entity.DownstreamConfig;
import io.dhoondlay.shield.entity.ShieldPattern;
import io.dhoondlay.shield.repository.AuditLogRepository;
import io.dhoondlay.shield.repository.DownstreamRepository;
import io.dhoondlay.shield.repository.ShieldPatternRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Reactive Admin REST API.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "Manage patterns, downstream configs, and audit logs.")
@RequiredArgsConstructor
public class AdminController {

    private final ShieldPatternRepository patternRepository;
    private final DownstreamRepository downstreamRepository;
    private final AuditLogRepository auditLogRepository;
    private final DynamicDatabaseDetector detector;

    // -----------------------------------------------------------------------
    // Patterns
    // -----------------------------------------------------------------------

    @Operation(summary = "List all patterns")
    @GetMapping("/patterns")
    public Flux<ShieldPattern> listPatterns() {
        return Flux.defer(() -> Flux.fromIterable(patternRepository.findAll()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Operation(summary = "Create or update a pattern")
    @PostMapping("/patterns")
    public Mono<ShieldPattern> savePattern(@RequestBody ShieldPattern pattern) {
        return Mono.fromCallable(() -> {
            ShieldPattern saved = patternRepository.save(pattern);
            detector.invalidateCache();
            return saved;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Operation(summary = "Toggle pattern enabled/disabled")
    @PatchMapping("/patterns/{id}/toggle")
    public Mono<ResponseEntity<ShieldPattern>> togglePattern(@PathVariable Long id) {
        return Mono.fromCallable(() -> patternRepository.findById(id).map(p -> {
            p.setEnabled(!p.isEnabled());
            ShieldPattern saved = patternRepository.save(p);
            detector.invalidateCache();
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build()))
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Operation(summary = "Delete a pattern")
    @DeleteMapping("/patterns/{id}")
    public Mono<ResponseEntity<Void>> deletePattern(@PathVariable Long id) {
        return Mono.fromRunnable(() -> {
            patternRepository.deleteById(id);
            detector.invalidateCache();
        }).subscribeOn(Schedulers.boundedElastic())
        .thenReturn(ResponseEntity.noContent().build());
    }

    // -----------------------------------------------------------------------
    // Downstreams
    // -----------------------------------------------------------------------

    @Operation(summary = "List all downstream configs")
    @GetMapping("/downstreams")
    public Flux<DownstreamConfig> listDownstreams() {
        return Flux.defer(() -> Flux.fromIterable(downstreamRepository.findAllByOrderByAliasAsc()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Operation(summary = "Save a downstream config")
    @PostMapping("/downstreams")
    public Mono<DownstreamConfig> saveDownstream(@RequestBody DownstreamConfig config) {
        return Mono.fromCallable(() -> downstreamRepository.save(config))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Operation(summary = "Delete a downstream config")
    @DeleteMapping("/downstreams/{id}")
    public Mono<ResponseEntity<Void>> deleteDownstream(@PathVariable Long id) {
        return Mono.fromRunnable(() -> downstreamRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Toggle downstream enabled/disabled")
    @PatchMapping("/downstreams/{id}/toggle")
    public Mono<ResponseEntity<DownstreamConfig>> toggleDownstream(@PathVariable Long id) {
        return Mono.fromCallable(() -> downstreamRepository.findById(id).map(d -> {
            d.setEnabled(!d.isEnabled());
            return ResponseEntity.ok(downstreamRepository.save(d));
        }).orElse(ResponseEntity.notFound().build()))
        .subscribeOn(Schedulers.boundedElastic());
    }

    // -----------------------------------------------------------------------
    // Audit Logs
    // -----------------------------------------------------------------------

    @Operation(summary = "Paginated audit logs (latest first)")
    @GetMapping("/audit-logs")
    public Mono<Page<AuditLog>> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Mono.fromCallable(() -> auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // -----------------------------------------------------------------------
    // Stats
    // -----------------------------------------------------------------------

    @Operation(summary = "System stats for the dashboard")
    @GetMapping("/stats")
    public Mono<Map<String, Object>> stats() {
        return Mono.fromCallable(() -> Map.<String, Object>of(
                "totalRequests", auditLogRepository.count(),
                "totalPatterns", patternRepository.count(),
                "totalDownstreams", downstreamRepository.count()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}

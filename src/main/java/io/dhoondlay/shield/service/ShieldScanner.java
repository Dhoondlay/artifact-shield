package io.dhoondlay.shield.service;

import io.dhoondlay.shield.detector.Detector;
import io.dhoondlay.shield.model.DetectionMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates all active detectors to scan the input string.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShieldScanner {

    private final List<Detector> detectors;

    /**
     * Scans the input string against all enabled detectors in parallel.
     * Leverages multi-threading for high-throughput CPU-bound scanning.
     * @param input Raw text to scan.
     * @return Mono containing the merged list of all found matches.
     */
    public reactor.core.publisher.Mono<List<DetectionMatch>> scanParallel(String input) {
        return reactor.core.publisher.Flux.fromIterable(detectors)
                .filter(Detector::isEnabled)
                .parallel() // Divide detectors across CPU cores
                .runOn(reactor.core.scheduler.Schedulers.parallel())
                .flatMap(detector -> {
                    try {
                        return reactor.core.publisher.Flux.fromIterable(detector.detect(input));
                    } catch (Exception e) {
                        log.error("Detector {} failed to scan input: {}", detector.name(), e.getMessage());
                        return reactor.core.publisher.Flux.empty();
                    }
                })
                .sequential()
                .collectList();
    }
}

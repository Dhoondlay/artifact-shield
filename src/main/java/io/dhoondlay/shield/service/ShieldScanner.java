package io.dhoondlay.shield.service;

import io.dhoondlay.shield.detector.Detector;
import io.dhoondlay.shield.model.DetectionMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
     * Scans the input string against all enabled detectors.
     * @param input Raw text to scan.
     * @return List of all found matches.
     */
    public List<DetectionMatch> scan(String input) {
        List<DetectionMatch> allMatches = new ArrayList<>();

        for (Detector detector : detectors) {
            if (detector.isEnabled()) {
                try {
                    allMatches.addAll(detector.detect(input));
                } catch (Exception e) {
                    log.error("Detector {} failed to scan input: {}", detector.name(), e.getMessage());
                }
            }
        }

        return allMatches;
    }
}

package io.dhoondlay.shield.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Enhanced immutable DTO for pipeline results.
 */
@Schema(description = "Internal result of the redaction and proxy pipeline.")
public record ShieldResult(
        String sanitizedText,
        String llmResponse,
        ThreatSeverity severity,
        int riskScore,
        List<String> detections,
        boolean wasProxied,
        long latencyMs
) {}

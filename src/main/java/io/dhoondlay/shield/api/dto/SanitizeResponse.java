package io.dhoondlay.shield.api.dto;

import io.dhoondlay.shield.model.ThreatSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Enhanced response containing redacted text and optional downstream LLM output.
 */
@Schema(description = "Secure gateway response.")
public record SanitizeResponse(
        @Schema(description = "The redacted text sent to downstream.")
        String sanitizedText,

        @Schema(description = "Raw response from the downstream LLM (if proxied).")
        String llmResponse,

        @Schema(description = "Risk classification.")
        ThreatSeverity severity,

        @Schema(description = "Total risk score (0-100).")
        int riskScore,

        @Schema(description = "Summary of detections found.")
        List<String> detections,

        @Schema(description = "True if the request was forwarded to downstream.")
        boolean wasProxied,

        @Schema(description = "Metadata about the request performance.")
        long latencyMs
) {}

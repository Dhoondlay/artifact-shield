package io.dhoondlay.shield.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for the Artifact-Shield primary endpoint.
 *
 * <h3>Actions</h3>
 * <ul>
 *   <li><b>R</b> – Redact Only: scan, redact PII, return sanitized text + risk score. No downstream call.</li>
 *   <li><b>F</b> – Forward: sanitize then forward the cleaned prompt to the configured downstream LLM. Returns both sanitized text and LLM response.</li>
 *   <li><b>A</b> – Analyze Only: scan and report detections without redacting or forwarding. Good for dry-run testing.</li>
 * </ul>
 *
 * <h3>Downstream / SSL</h3>
 * <ul>
 *   <li>{@code forwardTo} – optional alias matching a row in {@code shield_downstream_configs}. If omitted, uses the first enabled config.</li>
 *   <li>SSL/mTLS cert paths are configured per-downstream in the admin UI or database, not per-request.</li>
 * </ul>
 */
@Schema(description = "Artifact-Shield request payload.")
public record SanitizeRequest(

        @Schema(
            description = "Raw text to process.",
            example = "My AWS key is AKIAIOSFODNN7EXAMPLE and email is john@example.com"
        )
        @NotBlank(message = "content is required")
        @Size(max = 32_000, message = "content must not exceed 32000 characters")
        String content,

        @Schema(
            description = "Action to perform. R=Redact Only | F=Forward to LLM | A=Analyze Only",
            allowableValues = {"R", "F", "A"},
            defaultValue = "R",
            example = "R"
        )
        @Pattern(regexp = "^[RFA]$", message = "action must be R, F, or A")
        String action,

        @Schema(
            description = "Alias of the downstream LLM config to use (from shield_downstream_configs). Required if action=F.",
            example = "gemini-flash",
            nullable = true
        )
        String forwardTo
) {
    /** Normalize: default action to "R" if not provided. */
    public String effectiveAction() {
        return (action == null || action.isBlank()) ? "R" : action.toUpperCase();
    }
}

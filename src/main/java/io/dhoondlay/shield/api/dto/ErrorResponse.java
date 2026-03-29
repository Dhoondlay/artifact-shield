package io.dhoondlay.shield.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error envelope returned on any 4xx or 5xx response.
 */
@Schema(description = "Standard error response body.")
public record ErrorResponse(

        @Schema(description = "HTTP status code.", example = "400")
        int status,

        @Schema(description = "Short error code.", example = "VALIDATION_ERROR")
        String error,

        @Schema(description = "Human-readable error message.", example = "prompt must not be blank")
        String message,

        @Schema(description = "UTC timestamp of the error.")
        Instant timestamp,

        @Schema(description = "Field-level validation errors (only present for 400 responses).")
        Map<String, String> fieldErrors

) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, Instant.now(), Map.of());
    }

    public static ErrorResponse of(int status, String error, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(status, error, message, Instant.now(), fieldErrors);
    }
}

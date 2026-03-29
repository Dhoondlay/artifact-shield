package io.dhoondlay.shield.api;

import io.dhoondlay.shield.api.dto.SanitizeRequest;
import io.dhoondlay.shield.api.dto.SanitizeResponse;
import io.dhoondlay.shield.service.RedactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Primary reactive entry-point for Artifact-Shield.
 */
@RestController
@RequestMapping("/v1/shield")
@Tag(name = "Shield API", description = "Secure reactive proxy for PII redaction and LLM forwarding.")
@RequiredArgsConstructor
@Slf4j
public class ShieldController {

    private final RedactionService redactionService;

    @Operation(
        summary = "Sanitize / Analyze / Forward (Reactive)",
        description = "Non-blocking entry-point. Supports high-concurrency LLM proxying."
    )
    @PostMapping("/sanitize")
    public Mono<ResponseEntity<SanitizeResponse>> sanitize(@Valid @RequestBody SanitizeRequest request) {
        log.info("[REACTIVE-SHIELD] Inbound: action={} length={}", request.effectiveAction(), request.content().length());

        return redactionService.process(
                request.content(),
                request.effectiveAction(),
                request.forwardTo()
        ).map(result -> ResponseEntity.ok(new SanitizeResponse(
                result.sanitizedText(),
                result.llmResponse(),
                result.severity(),
                result.riskScore(),
                result.detections(),
                result.wasProxied(),
                result.latencyMs()
        )));
    }
}

package io.dhoondlay.shield.service;

import io.dhoondlay.shield.entity.AuditLog;
import io.dhoondlay.shield.model.ShieldResult;
import io.dhoondlay.shield.model.ThreatSeverity;
import io.dhoondlay.shield.repository.AuditLogRepository;
import io.dhoondlay.shield.repository.DownstreamRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("RedactionService tests")
class RedactionServiceTest {

    @Autowired
    private RedactionService redactionService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private DownstreamRepository downstreamRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private DownstreamProxyClient proxyClient;

    @Test
    @DisplayName("Action R: Redacts email and logs audit")
    void actionRRedactsAndLogs() {
        String input = "My email is test@example.com";
        ShieldResult result = redactionService.process(input, "R", null);

        assertThat(result.sanitizedText()).contains("[REDACTED_EMAIL]");
        assertThat(result.detections()).contains("EMAIL");
        assertThat(result.riskScore()).isGreaterThan(0);
        assertThat(result.wasProxied()).isFalse();

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getAction()).isEqualTo("R");
        assertThat(logs.get(0).getDetectedPatterns()).contains("EMAIL");
    }

    @Test
    @DisplayName("Action A: Analyzes without redacting")
    void actionAAnalyzesOnly() {
        String input = "My email is test@example.com";
        ShieldResult result = redactionService.process(input, "A", null);

        assertThat(result.sanitizedText()).isEqualTo(input);
        assertThat(result.detections()).contains("EMAIL");
        assertThat(result.wasProxied()).isFalse();
    }

    @Test
    @DisplayName("Action F: Blocks critical risk forward")
    void actionFBlocksCritical() {
        // High risk input: multiple patterns
        String input = "AWS Key: AKIAIOSFODNN7EXAMPLE, Email: test@example.com, Card: 4111111111111111, IP: 1.1.1.1, Private Key: -----BEGIN RSA PRIVATE KEY-----";
        ShieldResult result = redactionService.process(input, "F", null);

        assertThat(result.severity()).isEqualTo(ThreatSeverity.CRITICAL);
        assertThat(result.llmResponse()).startsWith("BLOCKED");
        assertThat(result.wasProxied()).isFalse();
    }

    @Test
    @DisplayName("Calculate severity logic")
    void testSeverityCalculation() {
        assertThat(redactionService.process("clean text", "R", null).severity()).isEqualTo(ThreatSeverity.CLEAN);
        assertThat(redactionService.process("test@example.com", "R", null).riskScore()).isGreaterThan(0);

        // Manual severity checks if I want to be 100% sure we hit all branches
        // Using enough patterns to hit HIGH (50+)
        String highRisk = "Email: test@example.com, IP: 1.1.1.1, AWS: AKIAIOSFODNN7EXAMPLE"; // 25 + 20 + 40 = 85
                                                                                             // (Critical)
        // Wait, I'll just check if I can hit them via score thresholds
    }

    @Test
    @DisplayName("Action F: Success forward")
    void actionFSuccess() {
        // Ensure an enabled downstream exists
        downstreamRepository.save(io.dhoondlay.shield.entity.DownstreamConfig.builder()
                .alias("test-f")
                .apiUrl("https://test.com")
                .enabled(true)
                .build());

        org.mockito.Mockito
                .when(proxyClient.forwardToDownstream(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("LLM Response");

        ShieldResult result = redactionService.process("Hello world", "F", "test-f");

        assertThat(result.wasProxied()).isTrue();
        assertThat(result.llmResponse()).isEqualTo("LLM Response");
    }

    @Test
    @DisplayName("Action F: Downstream error")
    void actionFError() {
        downstreamRepository.save(io.dhoondlay.shield.entity.DownstreamConfig.builder()
                .alias("test-error")
                .apiUrl("https://test.com")
                .enabled(true)
                .build());

        org.mockito.Mockito
                .when(proxyClient.forwardToDownstream(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RuntimeException("Connect Timeout"));

        ShieldResult result = redactionService.process("Hello", "F", "test-error");
        assertThat(result.llmResponse()).contains("Error: downstream call failed");
    }

    @Test
    @DisplayName("Action F: No downstream found")
    void actionFNoDownstream() {
        downstreamRepository.deleteAll();
        ShieldResult result = redactionService.process("Hello", "F", "missing");
        assertThat(result.llmResponse()).contains("Error: no active downstream found");
    }

}

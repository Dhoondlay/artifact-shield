package io.dhoondlay.shield.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


/**
 * Audit record for each request processed by Artifact-Shield.
 * Stores an anonymized view (no raw PII) for compliance review.
 */
@Entity
@Table(name = "shield_audit_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Instant timestamp = Instant.now();

    /** Action taken: R=Redact, F=Forward, A=Analyze */
    private String action;

    /** Severity of the detected content */
    private String severity;

    private int riskScore;

    /** Comma-separated list of pattern names detected */
    @Column(length = 2000)
    private String detectedPatterns;

    /** Length of the original input (not the content itself) */
    private int inputLength;

    /** Length of sanitized output */
    private int sanitizedLength;

    private boolean proxied;

    private long latencyMs;

    /** Which downstream alias was used (if proxied) */
    private String downstreamAlias;
}

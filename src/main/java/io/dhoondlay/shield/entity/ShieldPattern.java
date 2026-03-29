package io.dhoondlay.shield.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity to store redaction rules dynamically in a database.
 * Users can add, enable/disable, or change regex weights at runtime.
 */
@Entity
@Table(name = "shield_patterns")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ShieldPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String detectorName; // e.g., "PII", "FINANCIAL"

    @Column(nullable = false, unique = true)
    private String patternName; // e.g., "EMAIL", "AWS_KEY"

    @Column(nullable = false, length = 1000)
    private String regex;

    @Builder.Default
    private String placeholderTemplate = "[REDACTED_{type}]";

    @Builder.Default
    private int riskWeight = 20;

    @Builder.Default
    private boolean enabled = true;
}

package io.dhoondlay.shield.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Downstream LLM configuration (Gemini, OpenAI, internal servers, etc.)
 *
 * Certificates are optional — leave null for plain HTTP connections.
 * When {@code keystorePath} is set, the proxy will use mTLS (our app identity cert).
 * When {@code truststorePath} is set, only that CA is trusted for the downstream.
 */
@Entity
@Table(name = "shield_downstream_configs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DownstreamConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique human-readable alias, used in the API "forwardTo" field. */
    @Column(nullable = false, unique = true)
    private String alias;

    /** Full LLM endpoint URL (e.g. https://generativelanguage.googleapis.com/...) */
    @Column(nullable = false)
    private String apiUrl;

    /**
     * How to attach the token: "BEARER" → "Authorization: Bearer {token}",
     * "API_KEY" → "x-goog-api-key: {token}", "CUSTOM" → raw header value.
     */
    @Builder.Default
    private String authType = "BEARER";

    /** The actual auth credential value (never logged). */
    @Column(length = 2000)
    private String authToken;

    /** Name of the request field that holds the prompt (e.g. "prompt", "user_message"). */
    @Builder.Default
    private String promptField = "prompt";

    // --- SSL / mTLS ---
    /** Path to PKCS12 keystore for our app's identity certificate (mTLS). Optional. */
    private String keystorePath;
    private String keystorePassword;

    /** Path to PKCS12 truststore to verify the downstream server. Optional. */
    private String truststorePath;
    private String truststorePassword;

    /** HTTP connection timeout in seconds (default 30). */
    @Builder.Default
    private int timeoutSeconds = 30;

    @Builder.Default
    private boolean enabled = true;
}

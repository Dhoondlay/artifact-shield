package io.dhoondlay.shield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Artifact-Shield – Privacy Proxy Gateway
 *
 * <p>Entry point for the Spring Boot application. The gateway intercepts raw text
 * prompts and redacts sensitive information before they are forwarded to any external
 * AI service. All detection logic is 100% deterministic – no AI, no cloud calls.
 *
 * @author  Dhoondlay Engineering
 * @since   1.0.0
 */
@SpringBootApplication
@EnableConfigurationProperties
public class ArtifactShieldApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtifactShieldApplication.class, args);
    }
}

package io.dhoondlay.shield.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the auto-generated OpenAPI documentation surfaced at
 * {@code /swagger-ui.html} and {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI artifactShieldOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Artifact-Shield Privacy Gateway API")
                        .version("1.0.0")
                        .description("""
                                High-performance, deterministic PII redaction gateway for AI prompts.
                                Detects and redacts credentials, financial data, and personal information
                                using regex patterns and mathematical validation (Luhn Algorithm).
                                Zero AI dependencies – all logic is 100% deterministic.
                                """)
                        .contact(new Contact()
                                .name("Dhoondlay Engineering")
                                .url("https://dhoondlay.com")
                                .email("security@dhoondlay.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://shield.dhoondlay.com").description("Production")
                ));
    }
}

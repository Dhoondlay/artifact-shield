package io.dhoondlay.shield.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Enterprise Reactive Security Configuration for Artifact-Shield.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private ShieldProperties shieldProperties;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        ShieldProperties.SecuritySettings settings = shieldProperties.getSecurity();

        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .headers(headers -> headers.frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable));

        if (settings.isCorsEnabled()) {
            http.cors(Customizer.withDefaults());
        }

        if (!settings.isEnabled()) {
            // mode: Open / Trial
            http.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
        } else {
            // mode: Enterprise / Secure
            http.authorizeExchange(exchanges -> exchanges
                .pathMatchers("/", "/index.html", "/static/**", "/favicon.ico").permitAll()
                .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/v1/shield/**").authenticated()
                .pathMatchers("/api/admin/**").authenticated()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-ID"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

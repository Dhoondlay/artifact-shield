package io.dhoondlay.shield.service;

import io.dhoondlay.shield.entity.DownstreamConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * Handles communication with the downstream LLM (Gemini/External API) in a non-blocking way.
 * Uses Spring WebClient (Project Reactor) for maximum throughput.
 */
@Service
public class DownstreamProxyClient {

    /**
     * Forwards a sanitized prompt to the configured downstream LLM.
     * @param config The downstream configuration.
     * @param sanitizedPrompt The filtered text to send.
     * @return Mono containing raw LLM response.
     */
    public Mono<String> forwardToDownstream(DownstreamConfig config, String sanitizedPrompt) {
        WebClient webClient = buildClient(config);

        String requestBody = """
                { "prompt": "%s" }
                """.formatted(sanitizedPrompt.replace("\"", "\\\""));

        return webClient.post()
                .uri(config.getApiUrl())
                .header("Authorization", config.getAuthType() + " " + config.getAuthToken())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    private WebClient buildClient(DownstreamConfig config) {
        if (config.getKeystorePath() == null && config.getTruststorePath() == null) {
            return WebClient.create();
        }

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

            // 1. Identity Certificate (Keystore)
            if (config.getKeystorePath() != null) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                try (InputStream is = Files.newInputStream(Paths.get(config.getKeystorePath()))) {
                    keyStore.load(is, config.getKeystorePassword().toCharArray());
                }
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, config.getKeystorePassword().toCharArray());
                sslContextBuilder.keyManager(kmf);
            }

            // 2. Trust downstream (Truststore)
            if (config.getTruststorePath() != null) {
                KeyStore trustStore = KeyStore.getInstance("PKCS12");
                try (InputStream is = Files.newInputStream(Paths.get(config.getTruststorePath()))) {
                    trustStore.load(is, config.getTruststorePassword().toCharArray());
                }
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore);
                sslContextBuilder.trustManager(tmf);
            }

            SslContext sslContext = sslContextBuilder.build();
            HttpClient httpClient = HttpClient.create()
                    .secure(spec -> spec.sslContext(sslContext));

            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Reactive SSL context for downstream: " + e.getMessage(), e);
        }
    }
}

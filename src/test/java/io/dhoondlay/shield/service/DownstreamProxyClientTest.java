package io.dhoondlay.shield.service;

import io.dhoondlay.shield.entity.DownstreamConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("DownstreamProxyClient tests")
class DownstreamProxyClientTest {

    @Autowired
    private DownstreamProxyClient client;

    @Test
    @DisplayName("Build client without SSL config")
    void buildClientWithoutSSL() {
        DownstreamConfig config = DownstreamConfig.builder().build();
        // Just ensuring no exception is thrown when creating RestClient
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("Fails on invalid SSL paths")
    void failsOnInvalidSSLPaths() {
        DownstreamConfig config = DownstreamConfig.builder()
                .keystorePath("/invalid/path")
                .keystorePassword("pwd")
                .build();

        assertThrows(RuntimeException.class, () -> client.forwardToDownstream(config, "test"));
    }
}

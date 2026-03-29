package io.dhoondlay.shield.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("ShieldController tests (Reactive)")
class ShieldControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser
    @DisplayName("POST /v1/shield/sanitize: Valid R action returns 200")
    void sanitizeRReturnsOk() {
        String body = """
                {
                    "content": "Please redact test@example.com",
                    "action": "R"
                }
                """;

        webTestClient.post().uri("/v1/shield/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sanitizedText").value(containsString("[REDACTED_EMAIL]"))
                .jsonPath("$.riskScore").value(greaterThan(0));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /v1/shield/sanitize: Blank content returns 400")
    void blankContentReturnsBadRequest() {
        String body = """
                {
                    "content": "",
                    "action": "R"
                }
                """;

        webTestClient.post().uri("/v1/shield/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser
    @DisplayName("POST /v1/shield/sanitize: Invalid action returns 400")
    void invalidActionReturnsBadRequest() {
        String body = """
                {
                    "content": "some text",
                    "action": "X"
                }
                """;

        webTestClient.post().uri("/v1/shield/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest();
    }
}

package io.dhoondlay.shield.api;

import io.dhoondlay.shield.entity.DownstreamConfig;
import io.dhoondlay.shield.entity.ShieldPattern;
import io.dhoondlay.shield.repository.DownstreamRepository;
import io.dhoondlay.shield.repository.ShieldPatternRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Transactional
@DisplayName("AdminController tests (Reactive)")
class AdminControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ShieldPatternRepository patternRepository;

    @Autowired
    private DownstreamRepository downstreamRepository;

    @Test
    @WithMockUser
    @DisplayName("GET /api/admin/patterns: Lists all patterns")
    void listsAllPatterns() {
        webTestClient.get().uri("/api/admin/patterns")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ShieldPattern.class)
                .value(hasSize(greaterThan(0)));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/admin/patterns: Creates new pattern")
    void savesPattern() {
        ShieldPattern pattern = ShieldPattern.builder()
                .detectorName("custom")
                .patternName("TEST_PATTERN")
                .regex("\\d{5}")
                .riskWeight(10)
                .build();

        webTestClient.post().uri("/api/admin/patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pattern)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.patternName").isEqualTo("TEST_PATTERN");
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/admin/patterns/{id}/toggle: Toggles enabled state")
    void togglesPattern() {
        ShieldPattern first = patternRepository.findAll().get(0);
        boolean initialState = first.isEnabled();

        webTestClient.patch().uri("/api/admin/patterns/" + first.getId() + "/toggle")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.enabled").isEqualTo(!initialState);
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/admin/patterns/{id}/toggle: Returns 404 for missing")
    void togglesPatternNotFound() {
        webTestClient.patch().uri("/api/admin/patterns/999999/toggle")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/admin/patterns/{id}")
    void deletesPattern() {
        ShieldPattern first = patternRepository.findAll().get(0);
        webTestClient.delete().uri("/api/admin/patterns/" + first.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/admin/downstreams: Lists all downstreams")
    void listsAllDownstreams() {
        webTestClient.get().uri("/api/admin/downstreams")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/admin/downstreams: Creates new downstream")
    void savesDownstream() {
        DownstreamConfig config = DownstreamConfig.builder()
                .alias("test-service")
                .apiUrl("https://test.com")
                .enabled(true)
                .build();

        webTestClient.post().uri("/api/admin/downstreams")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(config)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.alias").isEqualTo("test-service");
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/admin/downstreams/{id}/toggle")
    void togglesDownstream() {
        DownstreamConfig dc = downstreamRepository.findAll().get(0);
        webTestClient.patch().uri("/api/admin/downstreams/" + dc.getId() + "/toggle")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/admin/downstreams/{id}")
    void deletesDownstream() {
        DownstreamConfig dc = downstreamRepository.findAll().get(0);
        webTestClient.delete().uri("/api/admin/downstreams/" + dc.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/admin/stats: Returns system stats")
    void statsReturnsCorrectCounts() {
        webTestClient.get().uri("/api/admin/stats")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalPatterns").value(greaterThan(0))
                .jsonPath("$.totalRequests").value(greaterThanOrEqualTo(0));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/admin/audit-logs: Returns paginated logs")
    void auditLogsReturnsPaged() {
        webTestClient.get().uri("/api/admin/audit-logs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray();
    }
}

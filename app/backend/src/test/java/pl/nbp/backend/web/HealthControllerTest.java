package pl.nbp.backend.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Integration test for {@link HealthController}.
 *
 * <p>Verifies that {@code GET /api/health} returns {@code 200 OK} as required by
 * ADR-001 §5 and TAC-10.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DisplayName("GET /api/health")
class HealthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("returns 200 OK (liveness check)")
    void health_returns200() {
        ResponseEntity<Void> response = restTemplate.getForEntity("/api/health", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

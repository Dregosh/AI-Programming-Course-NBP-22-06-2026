package pl.nbp.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test that exercises the real running application.
 * Only runs when OPENROUTER_API_KEY environment variable is set.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "OPENROUTER_API_KEY", matches = ".+")
class LiveSmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpoint_returnsOk() {
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/health", Void.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}

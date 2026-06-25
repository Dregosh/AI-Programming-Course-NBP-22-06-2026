package pl.nbp.backend.llm;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.nbp.backend.domain.RequestType;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test that verifies the retry and error-propagation behaviour of
 * {@link OpenRouterResponsesAdapter}.
 *
 * <p>Uses MockWebServer to simulate transient server errors.
 */
class LlmRetryIT {

    private MockWebServer server;
    private OpenRouterResponsesAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        adapter = MockResponseHelper.makeAdapter(server);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void serverError_throwsLlmUpstreamException_afterRetry() {
        server.enqueue(new MockResponse().setResponseCode(503).setBody("Service Unavailable"));
        server.enqueue(new MockResponse().setResponseCode(503).setBody("Service Unavailable"));

        assertThatThrownBy(() ->
                adapter.analyzeImage(RequestType.RETURN, "data:image/jpeg;base64,/9j/test", "context")
        ).isInstanceOf(LlmUpstreamException.class);
    }
}

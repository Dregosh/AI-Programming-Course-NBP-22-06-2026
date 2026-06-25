package pl.nbp.backend.llm;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.nbp.backend.domain.RequestType;
import pl.nbp.backend.domain.VisualAssessment;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link OpenRouterResponsesAdapter#analyzeImage}.
 *
 * <p>Uses MockWebServer to fake the OpenRouter HTTP endpoint. No Spring context is loaded.
 */
class AnalyzeImageIT {

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
    void analyzeImage_parsesVisualAssessment() {
        server.enqueue(MockResponseHelper.makeJsonResponse(MockResponseHelper.VISUAL_ASSESSMENT_JSON));

        VisualAssessment result = adapter.analyzeImage(
                RequestType.RETURN,
                "data:image/jpeg;base64,/9j/test",
                "Typ wniosku: RETURN\nModel: iPhone 14");

        assertThat(result.isAnalyzable()).isTrue();
        assertThat(result.getConfidence()).isEqualTo(0.9);
        assertThat(result.getNotes()).isEqualTo("Test notes");
        assertThat(result.getSignsOfUse()).isTrue();
        assertThat(result.getDamageObserved()).isFalse();
        assertThat(result.getResalableAsNew()).isTrue();
    }
}

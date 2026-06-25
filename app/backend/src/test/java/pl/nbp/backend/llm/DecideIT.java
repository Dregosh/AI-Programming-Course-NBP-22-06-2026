package pl.nbp.backend.llm;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.nbp.backend.domain.Decision;
import pl.nbp.backend.domain.DecisionOutcome;
import pl.nbp.backend.domain.RequestType;
import pl.nbp.backend.domain.VisualAssessment;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link OpenRouterResponsesAdapter#decide}.
 *
 * <p>Uses MockWebServer to fake the OpenRouter HTTP endpoint. No Spring context is loaded.
 */
class DecideIT {

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
    void decide_approveMakesBindingTrue() {
        server.enqueue(MockResponseHelper.makeJsonResponse(MockResponseHelper.DECISION_JSON));

        VisualAssessment assessment = VisualAssessment.builder()
                .analyzable(true)
                .confidence(0.9)
                .notes("Good condition")
                .signsOfUse(false)
                .damageObserved(false)
                .resalableAsNew(true)
                .build();

        Decision result = adapter.decide(
                RequestType.RETURN,
                assessment,
                "Typ wniosku: RETURN\nModel: iPhone 14",
                "Polityka zwrotow §1: towar nieuszkodzony");

        assertThat(result.outcome()).isEqualTo(DecisionOutcome.APPROVE);
        assertThat(result.binding()).isTrue();
        assertThat(result.justification()).isNotBlank();
        assertThat(result.nextSteps()).isNotEmpty();
        assertThat(result.ruleReferences()).isNotEmpty();
    }
}

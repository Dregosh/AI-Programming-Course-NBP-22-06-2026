package pl.nbp.backend.llm;

import org.junit.jupiter.api.Test;
import pl.nbp.backend.domain.RequestType;

import static org.assertj.core.api.Assertions.assertThat;

class PromptFactoryTest {

    private final PromptFactory factory = new PromptFactory();

    @Test
    void analysisPromptContainsScenario_complaint() {
        String prompt = factory.buildAnalysisSystemPrompt(RequestType.COMPLAINT);
        assertThat(prompt).contains("reklamacji");
        assertThat(prompt).contains("analyzable");
        assertThat(prompt).contains("JSON");
    }

    @Test
    void analysisPromptContainsScenario_return() {
        String prompt = factory.buildAnalysisSystemPrompt(RequestType.RETURN);
        assertThat(prompt).contains("zwrotu");
        assertThat(prompt).contains("analyzable");
    }

    @Test
    void decisionPromptContainsPolicyText() {
        String policy = "§1 Polityka zwrotów: towar nieuszkodzony";
        String prompt = factory.buildDecisionSystemPrompt(RequestType.RETURN, policy);
        assertThat(prompt).contains(policy);
        assertThat(prompt).contains("APPROVE");
        assertThat(prompt).contains("REJECT");
        assertThat(prompt).contains("ESCALATE");
    }

    @Test
    void chatPromptIsInPolish() {
        String prompt = factory.buildChatSystemPrompt();
        assertThat(prompt).contains("NBP");
        assertThat(prompt).contains("polsku");
    }
}

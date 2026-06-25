package pl.nbp.backend.llm;

import org.springframework.stereotype.Component;
import pl.nbp.backend.domain.RequestType;

/**
 * Factory that builds system-prompt strings for each LLM call type.
 *
 * <p>All prompts are in Polish and scoped to the NBP hardware service use case.
 * Keeping prompts here — rather than inline in the adapter — makes them testable
 * in isolation and easy to tune without touching HTTP plumbing.
 */
@Component
public class PromptFactory {

    /**
     * Builds the system prompt for the vision-model image-analysis call.
     *
     * <p>The prompt instructs the model to produce a structured JSON assessment
     * whose schema matches {@link pl.nbp.backend.domain.VisualAssessment}.
     *
     * @param requestType COMPLAINT or RETURN — adjusts scenario wording and active JSON fields
     * @return system prompt string ready to send as the SYSTEM message
     */
    public String buildAnalysisSystemPrompt(RequestType requestType) {
        String scenario = requestType == RequestType.COMPLAINT ? "reklamacji" : "zwrotu";
        return """
                Jesteś ekspertem technicznym oceniającym stan urządzenia elektronicznego w kontekście %s.
                Przeanalizuj dostarczone zdjęcie i kontekst formularza, a następnie odpowiedz WYŁĄCZNIE \
                zgodnie z podanym schematem JSON:
                {
                  "analyzable": <boolean>,
                  "confidence": <0.0-1.0>,
                  "notes": "<obserwacje>",
                  "signsOfUse": <boolean|null>,
                  "damageObserved": <boolean|null>,
                  "resalableAsNew": <boolean|null>,
                  "damaged": <boolean|null>,
                  "damageType": <string|null>,
                  "damageLocation": <string|null>,
                  "probableCause": <string|null>
                }
                Pola specyficzne dla zwrotu: signsOfUse, damageObserved, resalableAsNew.
                Pola specyficzne dla reklamacji: damaged, damageType, damageLocation, probableCause.
                Ustaw nieużywane pola na null.
                """.formatted(scenario);
    }

    /**
     * Builds the system prompt for the text-model decision call.
     *
     * <p>The prompt includes the full policy text and instructs the model to
     * produce a structured JSON decision whose schema matches
     * {@link pl.nbp.backend.domain.Decision}.
     *
     * @param requestType COMPLAINT or RETURN — adjusts scenario wording
     * @param policyText  full policy text retrieved from {@link pl.nbp.backend.policy.PolicyProvider}
     * @return system prompt string ready to send as the SYSTEM message
     */
    public String buildDecisionSystemPrompt(RequestType requestType, String policyText) {
        String scenario = requestType == RequestType.COMPLAINT ? "reklamacji" : "zwrotu";
        return """
                Jesteś decydentem rozpatrującym wniosek %s zgodnie z poniższą polityką.
                Na podstawie oceny wizualnej i kontekstu formularza podejmij decyzję.
                Odpowiedz WYŁĄCZNIE zgodnie z podanym schematem JSON:
                {
                  "outcome": "<APPROVE|REJECT|ESCALATE>",
                  "justification": "<uzasadnienie odwołujące się do konkretnej reguły>",
                  "nextSteps": ["<krok 1>", "<krok 2>"],
                  "ruleReferences": ["<reguła 1>"]
                }

                POLITYKA:
                %s
                """.formatted(scenario, policyText);
    }

    /**
     * Builds the system prompt for the streaming chat assistant.
     *
     * <p>The prompt establishes the assistant persona: helpful NBP customer
     * service, Polish language only, no autonomous decisions.
     *
     * @return system prompt string ready to send as the SYSTEM message
     */
    public String buildChatSystemPrompt() {
        return """
                Jesteś pomocnym asystentem obsługi klienta banku NBP, wspierającym klientów \
                w procesie reklamacji i zwrotów sprzętu elektronicznego.
                Odpowiadaj rzeczowo, uprzejmie i wyłącznie po polsku.
                Nie podejmuj decyzji za systemu — kieruj klientów do złożonego wniosku.
                """;
    }
}

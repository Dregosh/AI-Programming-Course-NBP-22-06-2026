package pl.nbp.backend.llm;

import pl.nbp.backend.domain.ChatMessage;
import pl.nbp.backend.domain.Decision;
import pl.nbp.backend.domain.RequestType;
import pl.nbp.backend.domain.VisualAssessment;

import java.util.List;
import java.util.function.Consumer;

/**
 * Port (interface) for all LLM interactions in the Hardware Service Decision Copilot.
 *
 * <p>All openai-java SDK types are encapsulated behind this interface; no caller
 * outside the {@code llm} package may depend on SDK-specific classes (ADR-002
 * TAC-002-01). Implementations must throw {@link LlmUpstreamException} on any
 * LLM failure — never return fabricated results.
 */
public interface LlmClient {

    /**
     * Calls the vision model to analyse the uploaded image in the context of the service request.
     *
     * @param requestType  COMPLAINT or RETURN
     * @param imageDataUrl image encoded as {@code data:image/jpeg;base64,...}
     * @param formContext  text summary of the form fields
     * @return parsed {@link VisualAssessment}
     * @throws LlmUpstreamException if the LLM returns an error or unparseable response
     */
    VisualAssessment analyzeImage(RequestType requestType, String imageDataUrl, String formContext);

    /**
     * Calls the text model to make a structured decision based on assessment and policy.
     *
     * @param requestType  COMPLAINT or RETURN
     * @param assessment   result of the visual analysis
     * @param formContext  text summary of the form fields
     * @param policyText   full policy text for the given request type
     * @return parsed {@link Decision}
     * @throws LlmUpstreamException if the LLM returns an error or unparseable response
     */
    Decision decide(RequestType requestType, VisualAssessment assessment, String formContext, String policyText);

    /**
     * Streams a chat response token-by-token and returns the full accumulated text.
     *
     * <p>Each streamed token is delivered via {@code onDelta} as it arrives. The full
     * concatenated text is returned once streaming is complete.
     *
     * @param history  full conversation history (excluding SYSTEM messages; those come from
     *                 {@link PromptFactory})
     * @param onDelta  consumer called for each streamed token
     * @return the full response text
     * @throws LlmUpstreamException if the LLM returns an error during streaming
     */
    String streamChat(List<ChatMessage> history, Consumer<String> onDelta);
}

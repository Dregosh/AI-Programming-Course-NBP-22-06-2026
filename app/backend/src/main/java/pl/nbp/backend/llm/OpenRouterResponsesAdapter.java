package pl.nbp.backend.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.errors.InternalServerException;
import com.openai.errors.OpenAIIoException;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputImage;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputText;
import com.openai.models.responses.ResponseStreamEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.nbp.backend.domain.ChatMessage;
import pl.nbp.backend.domain.Decision;
import pl.nbp.backend.domain.DecisionOutcome;
import pl.nbp.backend.domain.MessageRole;
import pl.nbp.backend.domain.RequestType;
import pl.nbp.backend.domain.VisualAssessment;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Adapter that implements {@link LlmClient} using the openai-java SDK targeting
 * the OpenRouter Responses API.
 *
 * <p>All openai-java SDK types are confined to this class (ADR-002 TAC-002-01).
 * No SDK-specific type leaks outside the {@code llm} package.
 *
 * <p>Non-streaming calls use a single-retry strategy for transient errors
 * ({@code 5xx}, {@link InternalServerException}, {@link OpenAIIoException},
 * timeout). On any failure that cannot be retried — or on a second consecutive
 * failure — this adapter throws {@link LlmUpstreamException}.
 */
@Component
public class OpenRouterResponsesAdapter implements LlmClient {

    private final OpenAIClient client;
    private final LlmConfig config;
    private final PromptFactory promptFactory;
    private final ObjectMapper objectMapper;

    /**
     * Primary Spring-managed constructor. Builds the HTTP client from {@link LlmConfig}.
     *
     * @param config        openrouter.* configuration properties
     * @param promptFactory factory for system-prompt strings
     * @param objectMapper  Jackson mapper for structured-output parsing
     */
    @Autowired
    public OpenRouterResponsesAdapter(LlmConfig config, PromptFactory promptFactory, ObjectMapper objectMapper) {
        this.config = config;
        this.promptFactory = promptFactory;
        this.objectMapper = objectMapper;
        this.client = OpenAIOkHttpClient.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey() != null && !config.getApiKey().isBlank()
                        ? config.getApiKey() : "no-key")
                .build();
    }

    /**
     * Package-private constructor for tests — accepts a pre-built {@link OpenAIClient}
     * pointing at a {@link okhttp3.mockwebserver.MockWebServer}.
     *
     * @param client        pre-built test client
     * @param config        configuration (base URL / models must match the mock server)
     * @param promptFactory factory for system-prompt strings
     * @param objectMapper  Jackson mapper for structured-output parsing
     */
    OpenRouterResponsesAdapter(OpenAIClient client, LlmConfig config,
                               PromptFactory promptFactory, ObjectMapper objectMapper) {
        this.client = client;
        this.config = config;
        this.promptFactory = promptFactory;
        this.objectMapper = objectMapper;
    }

    /** {@inheritDoc} */
    @Override
    public VisualAssessment analyzeImage(RequestType requestType, String imageDataUrl, String formContext) {
        String instructions = promptFactory.buildAnalysisSystemPrompt(requestType);

        ResponseInputContent textContent = ResponseInputContent.ofInputText(
                ResponseInputText.builder().text(formContext).build());
        ResponseInputContent imageContent = ResponseInputContent.ofInputImage(
                ResponseInputImage.builder()
                        .imageUrl(imageDataUrl)
                        .detail(ResponseInputImage.Detail.AUTO)
                        .build());

        EasyInputMessage msg = EasyInputMessage.builder()
                .role(EasyInputMessage.Role.USER)
                .contentOfResponseInputMessageContentList(List.of(textContent, imageContent))
                .build();

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(config.getVisionModel())
                .instructions(instructions)
                .inputOfResponse(List.of(ResponseInputItem.ofEasyInputMessage(msg)))
                .build();

        String json = executeWithRetry(params);
        try {
            return objectMapper.readValue(json, VisualAssessment.class);
        } catch (Exception e) {
            throw new LlmUpstreamException("Failed to parse VisualAssessment: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Decision decide(RequestType requestType, VisualAssessment assessment,
                           String formContext, String policyText) {
        String instructions = promptFactory.buildDecisionSystemPrompt(requestType, policyText);

        String inputText;
        try {
            inputText = "Ocena wizualna:\n"
                    + objectMapper.writeValueAsString(assessment)
                    + "\n\nKontekst formularza:\n" + formContext;
        } catch (Exception e) {
            throw new LlmUpstreamException("Failed to serialize VisualAssessment: " + e.getMessage(), e);
        }

        EasyInputMessage msg = EasyInputMessage.builder()
                .role(EasyInputMessage.Role.USER)
                .content(inputText)
                .build();

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(config.getTextModel())
                .instructions(instructions)
                .inputOfResponse(List.of(ResponseInputItem.ofEasyInputMessage(msg)))
                .build();

        String json = executeWithRetry(params);
        try {
            DecisionDto dto = objectMapper.readValue(json, DecisionDto.class);
            return new Decision(
                    DecisionOutcome.valueOf(dto.outcome),
                    false, // binding is derived by Decision's compact constructor
                    dto.justification,
                    dto.nextSteps != null ? dto.nextSteps : List.of(),
                    dto.ruleReferences != null ? dto.ruleReferences : List.of()
            );
        } catch (Exception e) {
            throw new LlmUpstreamException("Failed to parse Decision: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String streamChat(List<ChatMessage> history, Consumer<String> onDelta) {
        String instructions = promptFactory.buildChatSystemPrompt();

        List<ResponseInputItem> items = history.stream()
                .filter(m -> m.role() != MessageRole.SYSTEM)
                .map(m -> {
                    EasyInputMessage.Role role = switch (m.role()) {
                        case USER -> EasyInputMessage.Role.USER;
                        case ASSISTANT -> EasyInputMessage.Role.ASSISTANT;
                        default -> EasyInputMessage.Role.USER;
                    };
                    return ResponseInputItem.ofEasyInputMessage(
                            EasyInputMessage.builder()
                                    .role(role)
                                    .content(m.content())
                                    .build());
                })
                .collect(Collectors.toList());

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(config.getTextModel())
                .instructions(instructions)
                .inputOfResponse(items)
                .build();

        ResponseAccumulator accumulator = ResponseAccumulator.create();
        try (StreamResponse<ResponseStreamEvent> stream = client.responses().createStreaming(params)) {
            stream.stream()
                    .peek(accumulator::accumulate)
                    .forEach(event -> {
                        if (event.isOutputTextDelta()) {
                            onDelta.accept(event.asOutputTextDelta().delta());
                        }
                    });
        } catch (Exception e) {
            throw new LlmUpstreamException("Stream chat failed: " + e.getMessage(), e);
        }

        try {
            return accumulator.response().output().get(0).message().get()
                    .content().get(0).asOutputText().text();
        } catch (Exception e) {
            throw new LlmUpstreamException("Failed to extract streamed text: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Executes a non-streaming Responses API call with one automatic retry on
     * transient errors (5xx, I/O errors, timeouts).
     *
     * @param params the request parameters
     * @return extracted text from the first output message
     * @throws LlmUpstreamException on persistent or non-transient failure
     */
    private String executeWithRetry(ResponseCreateParams params) {
        try {
            return extractText(client.responses().create(params));
        } catch (Exception e) {
            if (isTransient(e)) {
                try {
                    return extractText(client.responses().create(params));
                } catch (Exception e2) {
                    throw new LlmUpstreamException(
                            "LLM upstream failed after retry: " + e2.getMessage(), e2);
                }
            }
            throw new LlmUpstreamException("LLM upstream failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the plain text from the first output message of a {@link Response}.
     *
     * @param response the API response
     * @return text content string
     */
    private String extractText(Response response) {
        return response.output().get(0).message().get()
                .content().get(0).asOutputText().text();
    }

    /**
     * Returns {@code true} for exceptions that warrant an automatic retry:
     * server-side errors (5xx), I/O errors, and timeout conditions.
     *
     * @param e the exception to classify
     * @return {@code true} if the error is transient
     */
    private boolean isTransient(Exception e) {
        return e instanceof InternalServerException
                || e instanceof OpenAIIoException
                || (e.getMessage() != null && e.getMessage().contains("timeout"));
    }

    // -------------------------------------------------------------------------
    // Internal DTO
    // -------------------------------------------------------------------------

    /**
     * Internal DTO for deserializing the structured JSON decision from the LLM.
     * Kept private — Decision domain type is constructed from this after validation.
     */
    private static class DecisionDto {
        /** One of APPROVE, REJECT, ESCALATE. */
        public String outcome;
        /** Polish-language justification referencing a policy rule. */
        public String justification;
        /** Ordered list of next steps for the customer. */
        public List<String> nextSteps;
        /** Policy rule references cited in the decision. */
        public List<String> ruleReferences;
    }
}

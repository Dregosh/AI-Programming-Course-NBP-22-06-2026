package pl.nbp.backend.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Test utility that builds canned MockWebServer responses and pre-wired adapter instances
 * for the OpenRouterResponsesAdapter integration tests.
 */
class MockResponseHelper {

    static final String VISUAL_ASSESSMENT_JSON =
            "{\"analyzable\":true,\"confidence\":0.9,\"notes\":\"Test notes\",\"signsOfUse\":true,"
            + "\"damageObserved\":false,\"resalableAsNew\":true,\"damaged\":null,"
            + "\"damageType\":null,\"damageLocation\":null,\"probableCause\":null}";

    static final String DECISION_JSON =
            "{\"outcome\":\"APPROVE\",\"justification\":\"Sprzet jest w dobrym stanie\","
            + "\"nextSteps\":[\"Poczekaj na potwierdzenie\"],\"ruleReferences\":[\"\\u00a71\"]}";

    /**
     * Wraps {@code textContent} in a well-formed OpenRouter Responses API JSON response body
     * and returns it as a 200 OK {@link MockResponse}.
     *
     * @param textContent the text to embed as the output_text value
     * @return a MockWebServer response ready to enqueue
     */
    static MockResponse makeJsonResponse(String textContent) {
        String body = "{"
                + "\"id\":\"resp_test\","
                + "\"object\":\"response\","
                + "\"created_at\":1234567890,"
                + "\"model\":\"openai/gpt-4o-mini\","
                + "\"output\":[{"
                + "  \"type\":\"message\","
                + "  \"id\":\"msg_test\","
                + "  \"role\":\"assistant\","
                + "  \"status\":\"completed\","
                + "  \"content\":[{"
                + "    \"type\":\"output_text\","
                + "    \"text\":" + jsonStringLiteral(textContent) + ","
                + "    \"annotations\":[]"
                + "  }]"
                + "}],"
                + "\"parallel_tool_calls\":true,"
                + "\"tool_choice\":\"auto\","
                + "\"tools\":[],"
                + "\"usage\":{\"input_tokens\":10,\"output_tokens\":20,\"total_tokens\":30}"
                + "}";
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    /**
     * Creates an {@link OpenRouterResponsesAdapter} wired to the given {@link MockWebServer}.
     *
     * @param server the mock server to target
     * @return a configured adapter ready for testing
     */
    static OpenRouterResponsesAdapter makeAdapter(MockWebServer server) {
        LlmConfig config = new LlmConfig();
        config.setBaseUrl(server.url("/").toString());
        config.setTextModel("openai/gpt-4o-mini");
        config.setVisionModel("openai/gpt-4o-mini");
        config.setApiKey("test-key");

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .baseUrl(server.url("/").toString())
                .apiKey("test-key")
                .maxRetries(0)   // disable SDK-level retries; our adapter handles its own retry
                .build();

        PromptFactory promptFactory = new PromptFactory();
        ObjectMapper mapper = new ObjectMapper();
        return new OpenRouterResponsesAdapter(client, config, promptFactory, mapper);
    }

    /**
     * Wraps a plain string in JSON string quotes, escaping backslashes and double-quotes.
     *
     * @param s the string to encode
     * @return a JSON string literal including surrounding double-quotes
     */
    private static String jsonStringLiteral(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

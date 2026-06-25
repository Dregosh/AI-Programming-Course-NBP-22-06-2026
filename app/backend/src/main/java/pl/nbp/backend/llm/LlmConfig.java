package pl.nbp.backend.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds {@code openrouter.*} properties from {@code application.yml} to a
 * strongly-typed configuration object.
 *
 * <p>Properties are resolved at startup; missing required values cause the
 * application context to fail fast rather than NullPointerExceptions at runtime.
 */
@Component
@ConfigurationProperties(prefix = "openrouter")
public class LlmConfig {

    /** Base URL of the OpenRouter API (defaults to {@code https://openrouter.ai/api/v1}). */
    private String baseUrl;

    /** Model identifier used for text-only (decision) calls. */
    private String textModel;

    /** Model identifier used for vision (image analysis) calls. */
    private String visionModel;

    /** API key passed as {@code Authorization: Bearer <key>}. */
    private String apiKey;

    /**
     * Returns the OpenRouter API base URL.
     *
     * @return base URL string
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the OpenRouter API base URL.
     *
     * @param baseUrl base URL string
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the model identifier for text-only LLM calls.
     *
     * @return text model identifier
     */
    public String getTextModel() {
        return textModel;
    }

    /**
     * Sets the model identifier for text-only LLM calls.
     *
     * @param textModel text model identifier
     */
    public void setTextModel(String textModel) {
        this.textModel = textModel;
    }

    /**
     * Returns the model identifier for vision (multimodal) LLM calls.
     *
     * @return vision model identifier
     */
    public String getVisionModel() {
        return visionModel;
    }

    /**
     * Sets the model identifier for vision (multimodal) LLM calls.
     *
     * @param visionModel vision model identifier
     */
    public void setVisionModel(String visionModel) {
        this.visionModel = visionModel;
    }

    /**
     * Returns the API key used for authenticating with OpenRouter.
     *
     * @return API key string
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key used for authenticating with OpenRouter.
     *
     * @param apiKey API key string
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}

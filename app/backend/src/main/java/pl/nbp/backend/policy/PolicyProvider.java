package pl.nbp.backend.policy;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import pl.nbp.backend.domain.RequestType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads and caches the plain-text policy documents for each {@link RequestType}.
 *
 * <p>Both policy files are read from the classpath at application startup via
 * {@link PostConstruct}. The cached strings are then served on every call to
 * {@link #getPolicyText(RequestType)} without additional I/O.
 */
@Component
public class PolicyProvider {

    private String returnPolicyText;
    private String complaintPolicyText;

    /**
     * Reads both policy Markdown files from {@code classpath:/policies/} and
     * caches their contents. Called automatically by Spring after the bean
     * is constructed.
     *
     * @throws IOException if either policy file cannot be read from the classpath
     */
    @PostConstruct
    void load() throws IOException {
        returnPolicyText = new ClassPathResource("policies/return-policy.md")
                .getContentAsString(StandardCharsets.UTF_8);
        complaintPolicyText = new ClassPathResource("policies/complaint-policy.md")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Returns the full policy text applicable to the given request type.
     *
     * @param requestType the type of service request (RETURN or COMPLAINT)
     * @return the policy document text; never {@code null} after successful startup
     */
    public String getPolicyText(RequestType requestType) {
        return switch (requestType) {
            case RETURN -> returnPolicyText;
            case COMPLAINT -> complaintPolicyText;
        };
    }
}

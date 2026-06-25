package pl.nbp.backend.web.dto;

import pl.nbp.backend.domain.DecisionOutcome;

import java.util.List;

/**
 * Response DTO representing an AI-generated decision.
 *
 * @param outcome        the decision outcome (APPROVE / REJECT / ESCALATE)
 * @param binding        {@code true} only when {@code outcome} is APPROVE
 * @param justification  textual justification referencing the applicable policy rule
 * @param nextSteps      ordered list of actions the customer should take
 * @param ruleReferences list of policy rule identifiers cited by the decision
 */
public record DecisionDto(
        DecisionOutcome outcome,
        boolean binding,
        String justification,
        List<String> nextSteps,
        List<String> ruleReferences) {
}

package pl.nbp.backend.domain;

import java.util.List;

/**
 * The structured decision produced by the decision agent.
 *
 * <p>The {@link #binding()} value is <strong>always derived server-side</strong>:
 * it is {@code true} if and only if {@link #outcome()} is {@link DecisionOutcome#APPROVE}.
 * Any value supplied by the caller in the constructor is overridden by the compact constructor.
 *
 * <p>Per ADR-000 §5 (TAC-04): the outcome is always exactly one of
 * {@code APPROVE}, {@code REJECT}, or {@code ESCALATE}.
 *
 * @param outcome        the decision outcome (APPROVE / REJECT / ESCALATE)
 * @param binding        {@code true} only when {@code outcome} is {@code APPROVE};
 *                       caller-supplied value is always overridden
 * @param justification  textual justification referencing the applicable policy rule
 * @param nextSteps      ordered list of actions the customer should take
 * @param ruleReferences list of policy rule identifiers cited by the decision
 */
public record Decision(
        DecisionOutcome outcome,
        boolean binding,
        String justification,
        List<String> nextSteps,
        List<String> ruleReferences) {

    /**
     * Compact constructor that enforces the server-side binding rule.
     *
     * <p>Regardless of the {@code binding} value passed by the caller, {@code binding}
     * is set to {@code true} if and only if {@code outcome == APPROVE}.
     */
    public Decision {
        binding = (outcome == DecisionOutcome.APPROVE);
    }
}

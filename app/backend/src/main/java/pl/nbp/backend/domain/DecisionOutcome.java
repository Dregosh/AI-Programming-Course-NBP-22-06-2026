package pl.nbp.backend.domain;

/**
 * The outcome of the AI-driven service decision.
 *
 * <ul>
 *   <li>{@link #APPROVE} — request is approved; decision is binding.</li>
 *   <li>{@link #REJECT} — request is rejected; decision is preliminary.</li>
 *   <li>{@link #ESCALATE} — insufficient information to decide; routed to a specialist.</li>
 * </ul>
 *
 * <p>{@code binding} is derived server-side: {@code true} iff the outcome is {@link #APPROVE}.
 * This value must never be trusted from the LLM response.
 */
public enum DecisionOutcome {

    /** The service request is approved. The decision is binding. */
    APPROVE,

    /** The service request is rejected. The decision is preliminary. */
    REJECT,

    /**
     * The case requires specialist review; the AI cannot produce a confident decision.
     * The decision is preliminary.
     */
    ESCALATE
}

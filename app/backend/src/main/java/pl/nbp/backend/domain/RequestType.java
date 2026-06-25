package pl.nbp.backend.domain;

/**
 * The type of service request submitted by the customer.
 *
 * <ul>
 *   <li>{@link #COMPLAINT} — a fault claim; requires a reason description.</li>
 *   <li>{@link #RETURN} — a no-fault return request within the return window.</li>
 * </ul>
 */
public enum RequestType {

    /** Fault claim: the customer reports a defect or malfunction. */
    COMPLAINT,

    /** No-fault return: the customer returns the item within the allowed window. */
    RETURN
}

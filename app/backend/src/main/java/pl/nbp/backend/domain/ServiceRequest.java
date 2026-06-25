package pl.nbp.backend.domain;

import java.time.LocalDate;

/**
 * Represents the customer's service request as submitted via the form.
 *
 * <p>The raw uploaded image is handled transiently during the decision pipeline and is
 * <em>not</em> stored in this record after analysis.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>{@code modelName} must not be blank.</li>
 *   <li>{@code purchaseDate} must not be in the future.</li>
 *   <li>When {@code requestType} is {@link RequestType#COMPLAINT}, {@code reason} must not be blank.</li>
 * </ul>
 *
 * @param requestType  the type of request (complaint or return)
 * @param category     the equipment category
 * @param modelName    the device model name (non-blank)
 * @param purchaseDate the date the device was purchased (not in the future)
 * @param reason       the customer's stated reason; required for {@link RequestType#COMPLAINT}
 * @throws IllegalArgumentException if any validation rule is violated
 */
public record ServiceRequest(
        RequestType requestType,
        EquipmentCategory category,
        String modelName,
        LocalDate purchaseDate,
        String reason) {

    /**
     * Compact constructor that enforces all validation rules.
     *
     * @throws IllegalArgumentException if {@code modelName} is blank, {@code purchaseDate} is in the
     *                                  future, or the request is a complaint with a blank reason
     */
    public ServiceRequest {
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("modelName must not be blank");
        }
        if (purchaseDate != null && purchaseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("purchaseDate must not be in the future");
        }
        if (requestType == RequestType.COMPLAINT && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("reason is required for a COMPLAINT request");
        }
    }
}

package pl.nbp.backend.domain;

/**
 * The result of the multimodal visual analysis step.
 *
 * <p>A single unified model covers both the RETURN and COMPLAINT scenarios.
 * Fields specific to one scenario are {@code null} when not applicable.
 *
 * <p>Common fields ({@link #analyzable}, {@link #confidence}, {@link #notes}) are always present.
 *
 * <p>RETURN-specific fields: {@link #signsOfUse}, {@link #damageObserved}, {@link #resalableAsNew}.
 *
 * <p>COMPLAINT-specific fields: {@link #damaged}, {@link #damageType}, {@link #damageLocation},
 * {@link #probableCause}.
 *
 * <p>Use {@link Builder} to construct instances.
 *
 * <pre>{@code
 * VisualAssessment assessment = VisualAssessment.builder()
 *     .analyzable(true)
 *     .confidence(0.92)
 *     .notes("Slight scratches visible on the back cover")
 *     .signsOfUse(true)
 *     .damageObserved(false)
 *     .resalableAsNew(false)
 *     .build();
 * }</pre>
 */
public class VisualAssessment {

    // --- Common fields ---

    /**
     * Whether the image could be meaningfully analyzed.
     * {@code false} when the image is blurry, off-topic, or otherwise inconclusive.
     */
    private boolean analyzable;

    /**
     * The model's confidence in the assessment, in the range {@code [0.0, 1.0]}.
     */
    private double confidence;

    /** Free-text observations from the vision model. */
    private String notes;

    // --- RETURN scenario fields ---

    /** Whether the item shows visible signs of use (scratches, wear, etc.). */
    private Boolean signsOfUse;

    /** Whether any damage is observable in the image. */
    private Boolean damageObserved;

    /** Whether the item appears suitable for resale as new. */
    private Boolean resalableAsNew;

    // --- COMPLAINT scenario fields ---

    /** Whether the item appears damaged. */
    private Boolean damaged;

    /** Type of damage observed (e.g., "screen crack", "liquid damage"). */
    private String damageType;

    /** Location of the damage on the device (e.g., "front panel", "charging port"). */
    private String damageLocation;

    /** The probable cause of the damage as inferred by the vision model. */
    private String probableCause;

    /**
     * No-arg constructor required for Jackson deserialization.
     */
    public VisualAssessment() {
    }

    private VisualAssessment(Builder builder) {
        this.analyzable = builder.analyzable;
        this.confidence = builder.confidence;
        this.notes = builder.notes;
        this.signsOfUse = builder.signsOfUse;
        this.damageObserved = builder.damageObserved;
        this.resalableAsNew = builder.resalableAsNew;
        this.damaged = builder.damaged;
        this.damageType = builder.damageType;
        this.damageLocation = builder.damageLocation;
        this.probableCause = builder.probableCause;
    }

    /**
     * Returns a new {@link Builder} instance.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // --- Getters ---

    /** @return whether the image was analyzable */
    public boolean isAnalyzable() {
        return analyzable;
    }

    /** @return model confidence in the range [0.0, 1.0] */
    public double getConfidence() {
        return confidence;
    }

    /** @return free-text observations, or {@code null} */
    public String getNotes() {
        return notes;
    }

    /** @return whether the item shows signs of use (RETURN scenario), or {@code null} */
    public Boolean getSignsOfUse() {
        return signsOfUse;
    }

    /** @return whether damage was observed (RETURN scenario), or {@code null} */
    public Boolean getDamageObserved() {
        return damageObserved;
    }

    /** @return whether the item is resalable as new (RETURN scenario), or {@code null} */
    public Boolean getResalableAsNew() {
        return resalableAsNew;
    }

    /** @return whether the item appears damaged (COMPLAINT scenario), or {@code null} */
    public Boolean getDamaged() {
        return damaged;
    }

    /** @return type of observed damage (COMPLAINT scenario), or {@code null} */
    public String getDamageType() {
        return damageType;
    }

    /** @return location of observed damage (COMPLAINT scenario), or {@code null} */
    public String getDamageLocation() {
        return damageLocation;
    }

    /** @return probable cause of damage (COMPLAINT scenario), or {@code null} */
    public String getProbableCause() {
        return probableCause;
    }

    // --- Setters (needed for Jackson) ---

    /** @param analyzable whether the image was analyzable */
    public void setAnalyzable(boolean analyzable) {
        this.analyzable = analyzable;
    }

    /** @param confidence model confidence [0.0, 1.0] */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    /** @param notes free-text observations */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /** @param signsOfUse whether the item shows signs of use */
    public void setSignsOfUse(Boolean signsOfUse) {
        this.signsOfUse = signsOfUse;
    }

    /** @param damageObserved whether damage was observed */
    public void setDamageObserved(Boolean damageObserved) {
        this.damageObserved = damageObserved;
    }

    /** @param resalableAsNew whether the item is resalable as new */
    public void setResalableAsNew(Boolean resalableAsNew) {
        this.resalableAsNew = resalableAsNew;
    }

    /** @param damaged whether the item appears damaged */
    public void setDamaged(Boolean damaged) {
        this.damaged = damaged;
    }

    /** @param damageType type of damage observed */
    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    /** @param damageLocation location of damage on the device */
    public void setDamageLocation(String damageLocation) {
        this.damageLocation = damageLocation;
    }

    /** @param probableCause probable cause of damage */
    public void setProbableCause(String probableCause) {
        this.probableCause = probableCause;
    }

    /**
     * Builder for {@link VisualAssessment}.
     */
    public static final class Builder {

        private boolean analyzable;
        private double confidence;
        private String notes;
        private Boolean signsOfUse;
        private Boolean damageObserved;
        private Boolean resalableAsNew;
        private Boolean damaged;
        private String damageType;
        private String damageLocation;
        private String probableCause;

        private Builder() {
        }

        /**
         * @param analyzable whether the image was analyzable
         * @return this builder
         */
        public Builder analyzable(boolean analyzable) {
            this.analyzable = analyzable;
            return this;
        }

        /**
         * @param confidence model confidence in the range [0.0, 1.0]
         * @return this builder
         */
        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        /**
         * @param notes free-text observations from the vision model
         * @return this builder
         */
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        /**
         * @param signsOfUse whether the item shows visible signs of use (RETURN scenario)
         * @return this builder
         */
        public Builder signsOfUse(Boolean signsOfUse) {
            this.signsOfUse = signsOfUse;
            return this;
        }

        /**
         * @param damageObserved whether any damage is observable (RETURN scenario)
         * @return this builder
         */
        public Builder damageObserved(Boolean damageObserved) {
            this.damageObserved = damageObserved;
            return this;
        }

        /**
         * @param resalableAsNew whether the item is suitable for resale as new (RETURN scenario)
         * @return this builder
         */
        public Builder resalableAsNew(Boolean resalableAsNew) {
            this.resalableAsNew = resalableAsNew;
            return this;
        }

        /**
         * @param damaged whether the item appears damaged (COMPLAINT scenario)
         * @return this builder
         */
        public Builder damaged(Boolean damaged) {
            this.damaged = damaged;
            return this;
        }

        /**
         * @param damageType the type of damage observed (COMPLAINT scenario)
         * @return this builder
         */
        public Builder damageType(String damageType) {
            this.damageType = damageType;
            return this;
        }

        /**
         * @param damageLocation the location of damage on the device (COMPLAINT scenario)
         * @return this builder
         */
        public Builder damageLocation(String damageLocation) {
            this.damageLocation = damageLocation;
            return this;
        }

        /**
         * @param probableCause the probable cause of damage (COMPLAINT scenario)
         * @return this builder
         */
        public Builder probableCause(String probableCause) {
            this.probableCause = probableCause;
            return this;
        }

        /**
         * Builds and returns a {@link VisualAssessment} with the configured values.
         *
         * @return a new {@code VisualAssessment}
         */
        public VisualAssessment build() {
            return new VisualAssessment(this);
        }
    }
}

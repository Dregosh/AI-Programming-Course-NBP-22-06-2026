package pl.nbp.backend.domain;

/**
 * The category of electronic equipment covered by the service request.
 *
 * <p>Used to drive prompt selection and policy lookup.
 */
public enum EquipmentCategory {

    /** Mobile smartphone. */
    SMARTPHONE,

    /** Laptop computer. */
    LAPTOP,

    /** Tablet device. */
    TABLET,

    /** Headphones (wired or wireless). */
    HEADPHONES,

    /** Smartwatch or fitness tracker. */
    SMARTWATCH,

    /** Any equipment that does not fit the named categories. */
    OTHER
}

package pl.nbp.backend.image;

/**
 * Thrown when an image has an unsupported format.
 * Only JPEG, PNG, and WebP are accepted; format is detected by magic bytes,
 * not by the claimed content-type.
 */
public class UnsupportedImageTypeException extends RuntimeException {

    /**
     * Constructs a new exception with the given detail message.
     *
     * @param message description of the unsupported format problem
     */
    public UnsupportedImageTypeException(String message) {
        super(message);
    }
}

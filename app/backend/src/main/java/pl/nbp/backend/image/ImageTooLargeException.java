package pl.nbp.backend.image;

/**
 * Thrown when an uploaded image exceeds the maximum allowed size (10 MB).
 */
public class ImageTooLargeException extends RuntimeException {

    /**
     * Constructs a new exception with the given detail message.
     *
     * @param message description of the size violation
     */
    public ImageTooLargeException(String message) {
        super(message);
    }
}

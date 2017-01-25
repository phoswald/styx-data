package styx.data.exception;

public class InvalidWriteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidWriteException(String message) {
        super(message);
    }
}

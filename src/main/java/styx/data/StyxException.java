package styx.data;

public class StyxException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StyxException() { }

    public StyxException(String message) {
        super(message);
    }

    public StyxException(String message, Throwable cause) {
        super(message, cause);
    }
}

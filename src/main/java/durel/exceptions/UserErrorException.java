package durel.exceptions;

public class UserErrorException extends Exception {
    public UserErrorException() {
        super();
    }

    public UserErrorException(String message) {
        super(message);
    }

    public UserErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

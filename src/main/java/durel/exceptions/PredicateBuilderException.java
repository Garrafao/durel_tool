package durel.exceptions;

public class PredicateBuilderException extends Exception {
    public PredicateBuilderException() {
        super();
    }

    public PredicateBuilderException(String message) {
        super(message);
    }

    public PredicateBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}

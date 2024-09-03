package durel.exceptions;

public class MissingRightsException extends Exception {
    public MissingRightsException() {
        super("You do not have the necessary rights to perform this action.");
    }

    public MissingRightsException(String message) {
        super(message);
    }
}
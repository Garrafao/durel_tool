package durel.dto.responses;

import lombok.Getter;
import lombok.Setter;

/**
 * It holds the response message for the front-end.
 */
@Getter
@Setter
public class ResponseMessage {

    private String message;

    public ResponseMessage(String message) {
        this.message = message;
    }
}
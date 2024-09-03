package durel.dto.requests.security;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Represents an authentication request object used for user authentication. This should only be used by the computational annotators.
 */
@Data
@AllArgsConstructor
@Slf4j
public class AuthenticationRequest implements Serializable {

    private String username;
    @ToString.Exclude
    private String password;
}

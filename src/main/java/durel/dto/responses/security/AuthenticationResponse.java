package durel.dto.responses.security;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
public record AuthenticationResponse(String jwt) implements Serializable {

}

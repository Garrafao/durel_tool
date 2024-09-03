package durel.dto.requests.registration;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class RegistrationRequest implements Serializable {

    @NotEmpty(message = "{register.username.empty}")
    private String username;

    @NotEmpty(message = "{register.email.empty}")
    @Email(message = "{register.email.pattern}")
    private String email;

    @ToString.Exclude
    @NotEmpty(message = "{register.password.empty}")
    private String password;

    @ToString.Exclude
    @NotEmpty(message = "{register.password.empty}")
    private String passwordRep;

    @NotEmpty(message = "{register.role.empty}")
    private String role;

    @AssertTrue
    private boolean privacyCheck;

    @AssertTrue
    private boolean ageCheck;
}
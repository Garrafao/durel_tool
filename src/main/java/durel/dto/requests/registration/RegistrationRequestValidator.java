package durel.dto.requests.registration;

import durel.services.user.UserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

/**
 * The RegistrationDataValidator class is responsible for validating the registration data
 * beyond what is possible with validation annotations. It implements the Validator interface.
 */
@Component
@Slf4j
public class RegistrationRequestValidator implements Validator {

    private final MessageSource messageSource;

    private final UserService userService;

    @Autowired
    public RegistrationRequestValidator(MessageSource messageSource, UserService userService) {
        this.messageSource = messageSource;
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(RegistrationRequest.class);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        RegistrationRequest registrationRequest = (RegistrationRequest) target;
        Locale locale = LocaleContextHolder.getLocale();
        validatePasswords(registrationRequest, errors, locale);
        validateUsername(registrationRequest, errors, locale);
        userExists(registrationRequest, errors, locale);
    }

    private void validatePasswords(RegistrationRequest registrationRequest, Errors errors, Locale locale) {
        if(!registrationRequest.getPassword().equals(registrationRequest.getPasswordRep())){
            errors.rejectValue("passwordRep", "matchingError", messageSource.getMessage("register.password.dontMatch", new Object[]{}, locale));
        }
    }

    private void validateUsername(RegistrationRequest registrationRequest, Errors errors, Locale locale) {
        if (registrationRequest.getUsername().length() >= 20) {
            errors.rejectValue("username", "lengthError", messageSource.getMessage("register.username.length", new Object[]{}, locale));
        }
    }

    private void userExists(RegistrationRequest registrationRequest, Errors errors, Locale locale) {
        if (userService.checkIfUserExists(registrationRequest.getUsername())) {
            errors.rejectValue("username", "userAlreadyExists",messageSource.getMessage("register.username.used", new Object[]{}, locale));
        }
    }
}

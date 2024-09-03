package durel.dto.requests.projects;

import durel.services.LanguageService;
import durel.services.ProjectService;
import durel.services.user.UserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Locale;

@Component
@Slf4j
public class ProjectUpdateRequestValidator extends DefaultProjectRequestValidator {

    private final UserService userService;

    @Autowired
    public ProjectUpdateRequestValidator(LanguageService languageService, ProjectService projectService, MessageSource messageSource, UserService userService) {
        super(languageService, projectService, messageSource);
        this.userService = userService;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ProjectUpdateRequest.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        super.validate(target, errors);
        ProjectUpdateRequest projectUpdateRequest = (ProjectUpdateRequest) target;

        Locale locale = LocaleContextHolder.getLocale();
        String projectName = projectUpdateRequest.getNewProjectName();
        super.checkProjectName(projectName, errors, locale);
        checkSelectedUsers(projectUpdateRequest, errors, locale);
    }

    private void checkSelectedUsers(@NonNull ProjectUpdateRequest projectUpdateRequest, Errors errors, Locale locale) {
        String[] selectedUsers = projectUpdateRequest.getSelectedUsers();
        try {
            for (String selectedUser : selectedUsers) {
                userService.getUserByUsername(selectedUser);
            }
        } catch (UsernameNotFoundException e) {
            addError(errors, "selectedUsers", "selected-user.invalid", locale);
        }
    }
}
package durel.dto.requests.projects;

import durel.dto.responses.LanguageDTO;
import durel.services.LanguageService;
import durel.services.ProjectService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

@Component
@Slf4j
public abstract class DefaultProjectRequestValidator implements Validator {

    private final LanguageService languageService;

    private final ProjectService projectService;

    final MessageSource messageSource;

    @Autowired
    public DefaultProjectRequestValidator(LanguageService languageService, ProjectService projectService, MessageSource messageSource) {
        this.languageService = languageService;
        this.projectService = projectService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return DefaultProjectRequest.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        DefaultProjectRequest defaultProjectRequest = (DefaultProjectRequest) target;

        Locale locale = LocaleContextHolder.getLocale();
        String projectName = defaultProjectRequest.getProjectName();
        checkProjectName(projectName, errors, locale);
        checkLanguage(defaultProjectRequest, errors, locale);
    }

    protected void checkProjectName(@NonNull String projectName, @NonNull Errors errors, Locale locale) {
        // Check projectName length
        if (projectName.length() >= 30) {
            addError(errors, "projectName", "project-name.length-too-long", locale);
        }

        // Check projectName for hyphens or spaces
        else if (projectName.contains("-") || projectName.contains(" ")) {
            addError(errors, "projectName", "project-name.invalid-characters", locale);
        }

        // Check for forbidden project names
        else if (projectName.toLowerCase().contains("delete") ||
                projectName.toLowerCase().contains("update") || projectName.toLowerCase().contains("*")) {
            addError(errors, "projectName", "project-name.forbidden", locale);
        }

        // Check if project with same name already exists
        else if (projectService.existsByID(projectName)) {
            addError(errors, "projectName", "project-name.in-use", locale);
        }
    }

    private void checkLanguage(DefaultProjectRequest defaultProjectRequest, @NonNull Errors errors, Locale locale) {
        String languageID = defaultProjectRequest.getLanguageID();
        if (languageID == null || languageID.isEmpty()) {
            addError(errors, "languageID", "selected-language.required", locale);
        } else if (!languageService.getLanguages().stream().map(LanguageDTO::getCode).toList().contains(languageID)) {
            addError(errors, "languageID", "selected-language.invalid", locale);
        }
    }

    void addError(Errors errors, String fieldName, String code, Locale locale) {
        errors.rejectValue(fieldName, code, messageSource.getMessage(code, new Object[]{}, locale));
    }
}
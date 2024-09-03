package durel.dto.requests.projects;

import durel.services.LanguageService;
import durel.services.ProjectService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Locale;

@Component
@Slf4j
public class ProjectUploadRequestValidator extends DefaultProjectRequestValidator {

    @Autowired
    public ProjectUploadRequestValidator(LanguageService languageService, ProjectService projectService, MessageSource messageSource) {
        super(languageService, projectService, messageSource);
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ProjectUploadRequest.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        super.validate(target, errors);
        ProjectUploadRequest projectUploadRequest = (ProjectUploadRequest) target;
        Locale locale = LocaleContextHolder.getLocale();
        checkDataType(projectUploadRequest, errors, locale);
    }

    private void checkDataType(ProjectUploadRequest projectUploadRequest, Errors errors, Locale locale) {
        if (!(projectUploadRequest.getDataType().equals("annotations") ||
                projectUploadRequest.getDataType().equals("instances") ||
                projectUploadRequest.getDataType().equals("uses"))) {
            addError(errors, "dataType", "dataType.invalid", locale);
        }
    }
}
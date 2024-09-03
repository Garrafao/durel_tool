package durel.dto.requests.common;

import durel.domain.model.Project;
import durel.services.ProjectService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.management.InstanceNotFoundException;
import java.util.Locale;

/**
 * The WordRequestDTOValidator class is responsible for validating the WordRequestDTO
 * beyond what is possible with validation annotations. It implements the Validator interface.
 */
@Component
@Slf4j
public class SelectLemmaRequestValidator implements Validator {

    private final MessageSource messageSource;

    private final ProjectService projectService;

    @Autowired
    public SelectLemmaRequestValidator(MessageSource messageSource, ProjectService projectService) {
        this.messageSource = messageSource;
        this.projectService = projectService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SelectLemmaRequest.class);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        SelectLemmaRequest request = (SelectLemmaRequest) target;
        Locale locale = LocaleContextHolder.getLocale();
        validateProjectLanguageIDAndLemmas(request, errors, locale);
    }

    private void validateProjectLanguageIDAndLemmas(SelectLemmaRequest request, Errors errors, Locale locale) {
        Project project;
        try {
            if (request.getProjectName() == null || request.getProjectName().isEmpty()) {
                errors.rejectValue("projectName", "project.nonexistence", messageSource.getMessage("wordRequest.nonexistance.project", new Object[]{}, locale));
            } else {
                project = projectService.getProject(request.getProjectName());
                for (String lemma: request.getLemmas()) {
                    if (lemma == null || lemma.isEmpty()) {
                        continue;
                    }
                    if (project.getLemmas().stream().noneMatch(word -> word.getLemma().equals(lemma))) {
                        errors.rejectValue("lemmas", "word.nonexistence", messageSource.getMessage("wordRequest.nonexistance.lemma", new Object[]{}, locale));
                    }
                }
                if (!request.getLanguageID().equals(project.getLanguage().getCode())) {
                    errors.rejectValue("languageID", "language.mismatch", messageSource.getMessage("wordRequest.mismatch.language", new Object[]{}, locale));
                }
            }
        } catch (InstanceNotFoundException | IllegalArgumentException e) {
            errors.rejectValue("projectName", "project.nonexistence", messageSource.getMessage("wordRequest.nonexistance.project", new Object[]{}, locale));
        }
    }

}
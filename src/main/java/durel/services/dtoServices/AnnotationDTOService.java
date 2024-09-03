package durel.services.dtoServices;

import durel.domain.model.annotation.UserAnnotation;
import durel.domain.model.Use;
import durel.dto.responses.AnnotationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Service
@Slf4j
public class AnnotationDTOService {

    public AnnotationDTO constructAnnotationDTOfromAnnotation(@NotNull UserAnnotation userAnnotation) {

        Use use1 = userAnnotation.getUses().stream().toList().get(0);
        Use use2 = userAnnotation.getUses().stream().toList().get(1);
        return AnnotationDTO.builder()
                .lemma(determineLemma(use1))
                .identifierUse1(determineUseIdentifier(use1))
                .contextUse1(determineUseContext(use1))
                .identifierUse2(determineUseIdentifier(use2))
                .contextUse2(determineUseContext(use2))
                .annotator(determineUsername(userAnnotation))
                .timestamp(userAnnotation.getDt())
                .judgment(userAnnotation.getJudgment())
                .comment(userAnnotation.getComment())
                .build();
    }

    private String encounteredNull(String methodName){
        log.warn("Unexpected null value encountered in {}", methodName);
        return "";
    }

    private String determineUseIdentifier(Use use) {
        return  use != null ? String.valueOf(use.getId()) : encounteredNull("determineUseIdentifier");
    }

    private String determineUseContext(Use use) {
        return use != null ? use.getContext() : encounteredNull("determineUseContext");
    }

    private String determineUsername(@NotNull UserAnnotation userAnnotation) {
        return userAnnotation.getAnnotator() != null ?
                userAnnotation.getAnnotator().getUsername() : encounteredNull("determineUsername");
    }

    private String determineLemma(Use use) {
        return use != null && use.getLemma() != null ?
                use.getLemma().getLemma() : encounteredNull("determineLemma");
    }
}
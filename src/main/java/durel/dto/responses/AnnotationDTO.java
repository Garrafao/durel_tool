package durel.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Builder
@Getter
@Slf4j
public class AnnotationDTO {

    private float judgment;
    private String lemma;
    private String annotator;
    private String identifierUse1;
    private String identifierUse2;
    private String contextUse1;
    private String contextUse2;
    private String comment;
    private Date timestamp;
}

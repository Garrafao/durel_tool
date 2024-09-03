package durel.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Getter
@Slf4j
public class ProjectDTO {
    private final boolean random;
    private final String name;
    private final String creator;
    private final String lang;
    @NotNull
    private final List<String> annotators;
    private final boolean visibility;
    private final String created;
    private final String lastEdited;
    private final Integer noOfWords;
    private final Integer noOfAnnotators;
}

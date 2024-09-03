package durel.dto.requests.projects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public abstract class DefaultProjectRequest {
    @NotNull(message = "{project-name.notNull}")
    private String projectName;
    @NotNull(message = "{languageID.notNull}")
    private String languageID;
}
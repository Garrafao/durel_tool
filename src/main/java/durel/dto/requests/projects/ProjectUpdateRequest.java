package durel.dto.requests.projects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProjectUpdateRequest extends DefaultProjectRequest{

    @NotNull
    private boolean visible;
    @NotEmpty
    private String newProjectName;
    @NotNull
    private String[] selectedUsers;
}

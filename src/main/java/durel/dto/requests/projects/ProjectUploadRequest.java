package durel.dto.requests.projects;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ProjectUploadRequest extends DefaultProjectRequest {
    private final String dataType;

    public ProjectUploadRequest(String dataType, String projectName, String languageID) {
        super(projectName, languageID);
        this.dataType = (dataType != null) ? dataType : "uses";
    }
}
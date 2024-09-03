package durel.services.dtoServices;

import durel.domain.model.Project;
import durel.dto.responses.ProjectDTO;
import durel.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProjectDTOService {

    private final UserService userService;

    public ProjectDTOService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a ProjectDTO with settings information based on the given Project.
     *
     * @param project The Project object to create the DTO from. Must not be null.
     * @return The created ProjectDTO object.
     */
    public ProjectDTO constructProjectStatisticsDTO(@NotNull Project project) {
        List<String> activeAnnotators = determineActiveAnnotators(project);
        return ProjectDTO.builder()
                .name(determineProjectName(project))
                .lang(determineLanguage(project))
                .creator(determineCreator(project))
                .annotators(activeAnnotators)
                .created(project.getDt().toString())
                .lastEdited(determineLastEdited(project))
                .noOfAnnotators(activeAnnotators.size())
                .noOfWords(determineNumberOfWords(project))
                .random(project.isAllPossiblePairs())
                .build();
    }

    /**
     * Creates a ProjectDTO with statistical information based on the given Project object.
     *
     * @param project The Project object to create the SettingsViewProject from.
     * @return The created ProjectDTO object.
     */
    public ProjectDTO constructProjectSettingsDTO(@NotNull Project project) {
        return ProjectDTO.builder()
                .name(determineProjectName(project))
                .lang(determineLanguage(project))
                .creator(determineCreator(project))
                .annotators(determineUsersWithAccess(project))
                .visibility(project.isPublic())
                .random(project.isAllPossiblePairs())
                .build();
    }

    private String determineCreator(Project project) {
        if (project.getCreator() != null) {
            return project.getCreator().getUsername();
        } else {
            log.warn("Cannot determine creator of project {}", project.getProjectName());
            return "";
        }
    }

    private String determineLanguage(Project project) {
        if (project.getLanguage() != null) {
            return project.getLanguage().getName();
        } else {
            log.warn("Cannot determine language of project {}", project.getProjectName());
            return "";
        }
    }

    private String determineProjectName(Project project) {
        if (project.getProjectName() != null) {
            return project.getProjectName();
        } else {
            log.warn("Cannot determine project name");
            return "";
        }
    }

    private List<String> determineUsersWithAccess(Project project) {
        if (project.getAnnotators() != null) {
            return userService.setOfUsersToListOfUsernames(project.getAnnotators());
        } else {
            log.warn("Cannot determine users with access to project {}", project.getProjectName());
            return new ArrayList<>();
        }
    }

    private List<String> determineActiveAnnotators(Project project) {
        if (project.getAnnotators() != null) {
            return userService.setOfUsersToListOfUsernames(
                    userService.findAllActiveAnnotatorsOfProject(project));
        } else {
            log.warn("Cannot determine active annotators of project {}", project.getProjectName());
            return new ArrayList<>();
        }
    }

    private int determineNumberOfWords(Project project) {
        if (project.getLemmas() != null) {
            return project.getLemmas().size();
        } else {
            log.warn("Cannot determine number of words in project {}", project.getProjectName());
            return 0;
        }
    }

    private String determineLastEdited(Project project) {
        return ""; // TODO
    }
}

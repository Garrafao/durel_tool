package durel.services;

import durel.dto.responses.ProjectDTO;
import durel.services.dataManagement.uploadData.AnnotationData;
import durel.services.dataManagement.uploadData.PairedUploadData;
import durel.services.dataManagement.uploadData.UseData;
import durel.domain.model.User;
import durel.domain.model.Project;
import durel.domain.model.Use;
import durel.domain.repository.ProjectDAO;
import durel.exceptions.MissingRightsException;
import durel.exceptions.SystemErrorException;
import durel.exceptions.UserErrorException;
import durel.services.dtoServices.ProjectDTOService;
import durel.services.user.UserService;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The ProjectService class provides functionality related to project objects.
 * @see Project
 */
@Service
public class ProjectService {

    /**
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    // DAO ------------------------------------------------------------------------------------------------
    private final ProjectDAO projectDAO;

    // OTHER SERVICES -------------------------------------------------------------------------------------
    private final AnnotationService annotationService;
    private final UseService useService;
    private final UserService userService;
    private final LanguageService languageService;
    private final PairService pairService;
    private final ProjectDTOService projectDTOService;
    private final WordService wordService;

    @Autowired
    public ProjectService(ProjectDAO projectDAO, UserService userService, AnnotationService annotationService,
                          UseService useService,
                          LanguageService languageService, PairService pairService, ProjectDTOService projectDTOService,
                          WordService wordService) {
        this.projectDAO = projectDAO;
        this.annotationService = annotationService;
        this.useService = useService;
        this.userService = userService;
        this.languageService = languageService;
        this.pairService = pairService;
        this.projectDTOService = projectDTOService;
        this.wordService = wordService;
    }

    // GETTING PROJECTS FROM THE DATABASE -------------------------------------------------------


    /**
     * Retrieves all projects from the database.
     *
     * @return A list of all projects.
     */
    private List<Project> getAllProjects ( ) {
        return projectDAO.findAll() ;
    }

    /**
     * Retrieves a Project entity from the database based on the given project name.
     *
     * @param projectName The name of the project.
     * @return The Project entity corresponding to the given project name.
     * @throws InstanceNotFoundException If the project with the given name does not exist.
     */
    public Project getProject (@NonNull String projectName) throws InstanceNotFoundException {
        return projectDAO.findById(projectName)
                .orElseThrow(() ->
                        new InstanceNotFoundException("The project with name: " + projectName + " does not exist"));
    }

    /**
     * Returns a list of projects that are visible to the user based on the provided username.
     * Visible projects include owned projects, projects the user has been granted access to,
     * and public projects.
     *
     * @param username The username for which to retrieve the visible projects.
     * @return A list of visible projects for the given username.
     */
    public List<Project> getVisibleProjects(String username) {
        // Check if current user is ADMIN.
        if (userService.isAdmin(username)) {
            // Return all projects.
            return getAllProjects();
        } else {
            // Otherwise fetch and return only visible projects to the user.
            return projectDAO.findByCreator_UsernameOrAnnotators_UsernameOrIsPublicTrueOrderByProjectNameAsc(username, username).stream().toList();
        }
    }

    /**
     * Retrieves a list of projects that are owned by the user with the given username.
     *
     * @param username The username of the user.
     * @return A list of projects owned by the user.
     */
    public List<Project> getOwnedProjects(String username) {
        // Check if current user is ADMIN.
        if (userService.isAdmin(username)) {
            // Return all projects.
            return getAllProjects();
        } else {
            // Otherwise fetch and return only owned projects to the user.
            return projectDAO.findByCreator_UsernameOrderByProjectNameAsc(username);
        }
    }

    // GETTING VIEW PROJECTS ------------------------------------------------------------------------------

    /**
     * Retrieves a list of projects owned by the user with the given username
     * and creates a list of SettingsViewProject objects based on the Projects.
     *
     * @param username The username of the user
     * @return A list of SettingsViewProject objects representing the owned projects
     */
    public List<ProjectDTO> getOwnedProjectsForSettings(String username) {
        List<ProjectDTO> settingsViewProjects = new ArrayList<>() ;
        for (Project project : getOwnedProjects(username)) {
            settingsViewProjects.add(projectDTOService.constructProjectSettingsDTO(project));
        }
        return settingsViewProjects;
    }

    /**
     * Retrieves a list of projects owned by the user with the given username
     * and creates a list of StatisticsViewProject objects based on the Projects.
     *
     * @param username The username of the user.
     * @return A list of StatisticsViewProject objects representing the owned projects.
     */
    public List<ProjectDTO> getOwnedProjectsForStatistics(String username) {
        List<ProjectDTO> statisticsViewProjects = new ArrayList<>();
        for (Project project : getOwnedProjects(username)) {
            statisticsViewProjects.add(projectDTOService.constructProjectStatisticsDTO(project));
        }
        return statisticsViewProjects;
    }

    // GETTING INFORMATION ABOUT PROJECTS -------------------------------------------------------

    /**
     * Checks if the user is the owner of the project or if they have the ADMIN role.
     *
     * @param projectName The name of the project.
     * @param principal The user Principal.
     * @return {@code true} if the user is the owner or has the ADMIN role, {@code false} otherwise.
     */
    public boolean userIsOwnerOrAdmin(String projectName, Principal principal) {
        try {
            String owner = getProject(projectName).getCreator().getUsername();
            boolean admin = userService.isAdmin(principal.getName());
            return principal.getName().equals(owner) | admin;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if a project exists in the database based on the given project name.
     *
     * @param projectName The name of the project to check.
     * @return {@code true} if the project exists, {@code false} otherwise.
     */
    public boolean existsByID(String projectName) {
        return projectDAO.existsById(projectName);
    }

    /**
     * Retrieves the annotators of a project based on the given project name.
     *
     * @param projectName The name of the project.
     * @return An ArrayList of strings with the names of the annotators of the project.
     * @throws InstanceNotFoundException If the project with the given name does not exist.
     */
    public List<String> getAnnotatorsOfProject(String projectName) throws InstanceNotFoundException {
        return userService.setOfUsersToListOfUsernames(getProject(projectName).getAnnotators());
    }

    /**
     * Returns a list of the names of projects that belong to a specific user and language.
     *
     * @param username The username of the user.
     * @param language The language.
     * @return A list of project names that belong to the user and have the specified language.
     */
    public ArrayList<String> getProjectNamesByUserAndLanguage(String username, String language) {
        return (ArrayList<String>) filterProjectsByLanguage(getVisibleProjects(username), language).stream().
                map(Project::getProjectName).collect(Collectors.toList());
    }

    /**
     * Filters a list of projects based on the provided language.
     *
     * @param projects A list of projects to filter.
     * @param language The language to filter by.
     * @return A filtered list of projects that match the specified language.
     */
    private List<Project> filterProjectsByLanguage(List<Project> projects, String language) {
        return projects.stream()
                .filter(project -> project.getLanguage().getCode().equals(language))
                .collect(Collectors.toList());
    }

    // UPLOADING AND MODIFYING PROJECTS ----------------------------------------------------------------------

    /**
     * Updates the details of a project.
     *
     * @param projectName The name of the project to update.
     * @param newLang     The new language for the project.
     * @param newVisibility The new visibility status for the project.
     * @param newGrants  The list of new annotators for the project.
     */
    @Modifying
    @Transactional
    public void updateProjectDetails(Principal principal, String projectName, String newLang, boolean newVisibility,
                                     List<String> newGrants){
        if (userIsOwnerOrAdmin(projectName, principal)) {
            try {
                Project project = getProject(projectName);
                project.setLanguage(languageService.getLanguage(newLang));
                project.setPublic(newVisibility);
                project.setAnnotators(userService.listOfUsernamesToSetOfUsers(newGrants));
                projectDAO.save(project);
            } catch (InstanceNotFoundException e) {
                logger.info("Tried to update non-existing project, this shouldn't happen.");
            } catch (EntityNotFoundException e) {
                logger.info("Non-existing language, this shouldn't happen.");
            }
        }
    }

    /**
     * Handles the upload of data for a project.
     *
     * @param principal      The principal representing the current user.
     * @param projectName    The name of the project.
     * @param lang           The language of the project. Can be null if creating a new project.
     * @param usesFiles      The list of use data files.
     * @param instancesFiles The list of paired upload data files.
     * @param random         A flag indicating whether the project is random.
     *
     * @throws MissingRightsException If the user does not have the necessary rights for the action.
     * @throws IOException            If there is an error during the upload process.
     */
    @Transactional
    public void handleUpload(@NotNull Principal principal, @NotNull String projectName, String lang,
                             @NotNull List<List<UseData>> usesFiles, List<List<PairedUploadData>> instancesFiles,
                             boolean random) throws MissingRightsException, IOException, SystemErrorException, UserErrorException {
        Project project;
        try {
            project = getProject(projectName);
            if (!userIsOwnerOrAdmin(projectName, principal)) {
                throw new MissingRightsException("User does not have the necessary rights to upload to the project " + projectName);
            }
        } catch (InstanceNotFoundException e){
            if (lang == null) {
                throw new IOException("There should be a language given if a new project is to be created.");
            }
            project = createProject(principal.getName(), projectName, lang, random);
        }
        // Iterate files (one file corresponds to one word). Each word has multiple sentences (one row per sentence).
        addNewWordsFromData(project, usesFiles, instancesFiles);
        // Save the project in the database.
        projectDAO.save(project);
    }

    /**
     * Creates a new project with the given parameters.
     *
     * @param username     The username of the project owner.
     * @param projectName  The name of the project.
     * @param lang         The language of the project.
     * @param random       A flag indicating whether the project is random.
     * @return The created Project entity.
     */
    private Project createProject(@NotNull String username, @NotNull String projectName, @NotNull String lang,
                              boolean random) {
        // Fetch owner of the new project.
        User user = userService.getUserByUsername(username);
        // Create a new project entity (empty)
        Project project = new Project(projectName, user, languageService.getLanguage(lang), false, random);
        logger.info("Project {} created.", project.getProjectName());
        return projectDAO.save(project);
    }

    /**
     * Adds new words from given data to the project.
     *
     * @param project       The project entity to which the words will be added.
     * @param usesFiles     The list of use data files containing the words.
     * @param pairedFiles   The list of paired upload data files containing .
     */
    private void addNewWordsFromData(@NotNull Project project, @NotNull List<List<UseData>> usesFiles,
                                    List<List<PairedUploadData>> pairedFiles) throws SystemErrorException, UserErrorException {
        for (List<UseData> usesFile : usesFiles) {
            // Create a word entity for each file, containing all sentences inside.
            project.addLemma(wordService.createAndSaveSentencesAndWord(project, usesFile));
        }
        logger.info("Uses {} created.", project.getProjectName());
        if (pairedFiles != null) {
            ConcurrentHashMap<String, Use> idToSentence = new ConcurrentHashMap<>(useService.getMapOfAllSentenceCSVIdsToSentencesInProject(project));
            for (List<PairedUploadData> pairedFile : pairedFiles) {
                pairService.createAndSavePairsFromPairedData(pairedFile, idToSentence, project);
                if (pairedFile.get(0) instanceof AnnotationData) {
                    annotationService.saveListOfAnnotationsAndUpdateAllSequences(pairedFile, idToSentence, project);
                }
            }
        }
        logger.info("Instances {} created.", project.getProjectName());
        projectDAO.save(project);
    }

    /**
     * Adds new annotations to a project.
     *
     * @param annotationData The list of lists of PairedUploadData containing the annotation data.
     * @param projectName The name of the project.
     */
    @Transactional
    public void addNewAnnotationsToProject(@NotNull List<List<PairedUploadData>> annotationData,
                                           @NotNull String projectName) throws SystemErrorException, UserErrorException {
        try {
            Project project = getProject(projectName);
            ConcurrentHashMap<String, Use> idToSentence = new ConcurrentHashMap<>(useService.getMapOfAllSentenceCSVIdsToSentencesInProject(project));
            for (List<PairedUploadData> pairedFile : annotationData){
                annotationService.saveListOfAnnotationsAndUpdateAllSequences(pairedFile, idToSentence, project);
            }
        } catch (InstanceNotFoundException e) {
            logger.info("Tried to add annotations to non-existing project, this shouldn't happen.");
        }
    }

    /**
     * Deletes a project from the database by its project name.
     *
     * @param projectName The name of the project to be deleted.
     */
    @Transactional
    public void deleteProjectByProjectName(@NotNull String projectName) {
        try {
            Project project = getProject(projectName);
            project.getAnnotators().clear();
            pairService.deleteByProjectName(project);
            projectDAO.deleteById(projectName);
        } catch (InstanceNotFoundException e) {
            logger.info("Tried to delete non-existing project, this shouldn't happen.");
        }
    }
}

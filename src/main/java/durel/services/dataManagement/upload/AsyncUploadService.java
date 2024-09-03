package durel.services.dataManagement.upload;

import durel.services.dataManagement.uploadData.AnnotationData;
import durel.services.dataManagement.uploadData.InstanceData;
import durel.services.dataManagement.uploadData.PairedUploadData;
import durel.services.dataManagement.uploadData.UseData;
import durel.domain.model.Language;
import durel.domain.model.Project;
import durel.exceptions.MissingRightsException;
import durel.exceptions.SystemErrorException;
import durel.exceptions.UserErrorException;
import durel.services.ProjectService;
import durel.services.TaskService;
import durel.services.TutorialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AsyncUploadService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncUploadService.class);

    private final ProjectService projectService;
    private final UseFileUploadService useFileUploadService;
    private final AnnotationFileUploadService annotationFileUploadService;
    private final InstancesFileUploadService instancesFileUploadService;
    private final UploadProgressService uploadProgressService;
    private final TaskService taskService;
    private final TutorialService tutorialService;

    @Autowired
    public AsyncUploadService(ProjectService projectService, UseFileUploadService useFileUploadService,
                              AnnotationFileUploadService annotationFileUploadService, InstancesFileUploadService instancesFileUploadService,
                              UploadProgressService uploadProgressService, TaskService taskService, TutorialService tutorialService) {
        this.projectService = projectService;
        this.useFileUploadService = useFileUploadService;
        this.annotationFileUploadService = annotationFileUploadService;
        this.instancesFileUploadService = instancesFileUploadService;
        this.uploadProgressService = uploadProgressService;
        this.taskService = taskService;
        this.tutorialService = tutorialService;
    }

    @Async
    @Transactional
    public void uploadProject(Principal principal, List<String> usesPaths, List<String> otherPaths, String projectName, String lang, String type, Boolean existingProject) throws InstanceNotFoundException {
        String methodName = existingProject ? "uploadWordsToExistingProject" : "uploadProject";
        logger.info("Starting {} method for projectName {}", methodName, projectName);
        Project project = existingProject ? projectService.getProject(projectName) : null;
        try {
            // The order of these calls is important do not change without checking!
            logger.info("Parsing uses for projectName {}", projectName);
            List<List<UseData>> uses = useFileUploadService.parseAndCheckFilesMultithreading(usesPaths, false);
            if (existingProject) {
                useFileUploadService.checkWordsInProject(uses, project);
            }
            // Wrong judgment or instances files are a breaking condition. To save time, they have to be parsed before
            // the uses files can be uploaded, even though the uses upload does not depend on them.
            List<List<PairedUploadData>> pairedData = getPairedData(type, otherPaths, uses, projectName, false);
            uploadProgressService.updateUploadProgress(projectName, "Loading into the database. Please refresh the page to see progress.");
            logger.info("Uploading data for projectName {}", projectName);
            boolean random = !existingProject && type.equals("uses");
            projectService.handleUpload(principal, projectName, lang, uses, pairedData, random);
            uploadProgressService.updateUploadProgress(projectName, "Uploaded successfully.");
        } catch (IOException | MissingRightsException | SystemErrorException | UserErrorException e) {
            logger.error("Error occurred in {} method for projectName {}: {}", methodName, projectName, e.getMessage());
            uploadProgressService.updateUploadProgress(projectName, e.getMessage());
        }
        logger.info("Ending {} method for projectName {}", methodName, projectName);
    }

    private List<List<PairedUploadData>> getPairedData(String type, List<String> otherPaths,
                                                       List<List<UseData>> uses, String projectName,
                                                       boolean multipleAllowed) throws IOException {
        List<List<PairedUploadData>> pairedData = new ArrayList<>();
        if (type.equals("annotations")) {
            logger.info("Parsing annotations for projectName {}", projectName);
            List<List<AnnotationData>> annotations = annotationFileUploadService.parseAndCheckFilesMultithreading(otherPaths, uses, multipleAllowed);
            for (List<AnnotationData> annotationData : annotations) {
                pairedData.add(new ArrayList<>(annotationData));
            }
        } else if (type.equals("instances")) {
            logger.info("Parsing instances for projectName {}", projectName);
            List<List<InstanceData>> instances = instancesFileUploadService.parseAndCheckFilesMultithreading(otherPaths, uses, multipleAllowed);
            for (List<InstanceData> instanceData : instances) {
                pairedData.add(new ArrayList<>(instanceData));
            }
        }
        return pairedData;
    }

    @Async
    @Transactional
    public void uploadAnnotationsToExistingProject(int id, String username, String projectName, List<String> paths) {
        logger.info("Starting uploadAnnotationsToExistingProject method for taskID {}", id);
        try {
            taskService.updateTask(id, "PARSING_FILES");
            List<List<PairedUploadData>> pairedData = getPairedData("annotations", paths, null, projectName, true);
            taskService.updateTask(id, "UPLOADING_RESULTS");
            projectService.addNewAnnotationsToProject(pairedData, projectName);
        } catch (IOException | SystemErrorException | UserErrorException e) {
            logger.error("Error occurred in uploadAnnotationsToExistingProject method for taskID {}: {}", id, e.getMessage());
            taskService.updateTask(id, "TASK_FAILED");
            uploadProgressService.deleteUploadProgress(username);
            return;
        }
        uploadProgressService.deleteUploadProgress("AnnotatorServer", projectName);
        taskService.updateTask(id, "TASK_COMPLETED");
        logger.info("Ending uploadAnnotationsToExistingProject method for taskID {}", id);
    }

    @Transactional
    public void uploadNewTutorial(List<String> usesPaths, List<String> goldAnnotationPaths, Language lang) throws IOException {
        logger.info("Starting uploadNewTutorial method for language {}", lang.getName());
        try {
            List<List<UseData>> uses = useFileUploadService.parseAndCheckFilesMultithreading(usesPaths, true);
            List<List<AnnotationData>> goldAnnotations = annotationFileUploadService.parseAndCheckFilesMultithreading(goldAnnotationPaths, uses, true);
            uploadProgressService.updateUploadProgress(lang.getName(), "Loading into the database. Please refresh the page to see progress.");
            tutorialService.saveTutorial(uses.get(0), goldAnnotations.get(0), lang);
        } catch (Exception e) {
            logger.error("Error occurred in uploadNewTutorial method for language {}: {}", lang.getName(), e.getMessage());
            uploadProgressService.updateUploadProgress(lang.getName(), e.getMessage());
        }
        uploadProgressService.updateUploadProgress(lang.getName(), "Uploaded successfully.");
        logger.info("Ending uploadNewTutorial method for language {}", lang.getName());
    }
}

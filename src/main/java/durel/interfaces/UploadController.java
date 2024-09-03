package durel.interfaces;

import durel.dto.requests.projects.ProjectUploadRequest;
import durel.dto.requests.projects.ProjectUploadRequestValidator;
import durel.dto.responses.ResponseMessage;
import durel.domain.model.Language;
import durel.services.LanguageService;
import durel.services.ProjectService;
import durel.services.TutorialService;
import durel.services.dataManagement.upload.AsyncUploadService;
import durel.services.dataManagement.upload.UploadProgressService;
import durel.services.user.UserService;
import durel.utils.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.management.InstanceNotFoundException;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The UploadController handles various upload requests related to projects and annotations.
 */
@Controller
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    private final UploadProgressService uploadProgressService;

    private final ProjectService projectService;

    private final AsyncUploadService asyncUploadService;

    private final UserService userService;

    private final TutorialService tutorialService;

    private final LanguageService languageService;

    private final ProjectUploadRequestValidator projectUploadRequestValidator;

    @Autowired
    public UploadController(UploadProgressService uploadProgressService,
                            ProjectService projectService, AsyncUploadService asyncUploadService,
                            UserService userService, TutorialService tutorialService, LanguageService languageService, ProjectUploadRequestValidator projectUploadRequestValidator) {
        this.uploadProgressService = uploadProgressService;
        this.projectService = projectService;
        this.asyncUploadService = asyncUploadService;
        this.userService = userService;
        this.tutorialService = tutorialService;
        this.languageService = languageService;
        this.projectUploadRequestValidator = projectUploadRequestValidator;
    }

    @InitBinder("newProject")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(projectUploadRequestValidator);
    }

    @Transactional
    @PostMapping(value = "/upload/annotations", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ResponseMessage> uploadAnnotations(@RequestParam("task_id") int id,
                                                      @RequestPart("files") MultipartFile[] annotations,
                                                      @RequestParam("projectName") String projectName,
                                                      Principal principal) {
        if (!projectService.existsByID(projectName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("No project of this name!"));
        }
        try {
            uploadProgressService.createNewUploadProgress(projectName, principal.getName());
            List<String> paths = FileManager.temporarilyStoreFiles(annotations);
            asyncUploadService.uploadAnnotationsToExistingProject(id, principal.getName(), projectName, paths);
        }
        catch (IOException e) {
            uploadProgressService.deleteUploadProgress(principal.getName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Uploading annotations."));
    }

    /**
     * Uploads a new project with pairs or gold annotations into the database.
     * @param principal User
     * @param usesFiles List of files containing the sentences of the project.
     * @param secondFiles gold annotations to be uploaded
     * @param uploadRequest   the project upload request
     * @param bindingResult   the binding result for validation errors
     * @return a ModelAndView object based on the selected data type
     */
    // TODO MaxUploadSizeExceededException
    @Transactional
    @PostMapping(value = "/uploadProject", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ModelAndView uploadUsesAndAnnotationsOrInstances(Principal principal, ModelMap model,
                                                            @RequestPart("files") MultipartFile[] usesFiles,
                                                            @RequestPart(value = "files2", required = false) MultipartFile[] secondFiles,
                                                            @ModelAttribute("newProject") @Valid ProjectUploadRequest uploadRequest,
                                                            BindingResult bindingResult) {
        String message;
        if (bindingResult.hasErrors()){
            List<ObjectError> errors = bindingResult.getAllErrors();
            message = errors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "));
            model.addAttribute("message", message);
        }
        else if (secondFiles != null && usesFiles.length != secondFiles.length) {
            message = "The number of use files and instance/annotation files does not match." ;
            model.addAttribute("message", message);
        }
        else {
            uploadProgressService.createNewUploadProgress(uploadRequest.getProjectName(), principal.getName());
            try {
                List<String> files1 = FileManager.temporarilyStoreFiles(usesFiles);
                List<String> files2;
                if (secondFiles == null) {
                    files2 = null;
                }
                else {
                    files2 = FileManager.temporarilyStoreFiles(secondFiles);
                }
                asyncUploadService.uploadProject(principal, files1, files2, uploadRequest.getProjectName(),
                        uploadRequest.getLanguageID(), uploadRequest.getDataType(), false);
            } catch (IOException e) {
                model.addAttribute("message", "Failed to parse files: " + e.getMessage());
            } catch (InstanceNotFoundException e) {
                logger.error("Got InstanceNotFoundException when it shouldn't have been raised.");
                model.addAttribute("message", "Error on upload, please contact durel@ims.uni-stuttgart.de: " + e.getMessage());
            }
        }
        return switch (uploadRequest.getDataType()) {
            case "annotations" -> new ModelAndView("redirect:/upload/annotations", model);
            case "instances" -> new ModelAndView("redirect:/upload/instances", model);
            default -> new ModelAndView("redirect:/upload/uses", model);
        };
    }

    @Transactional
    @PostMapping(value = "/uploadTutorial", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ModelAndView uploadTutorial(Principal principal, ModelMap model,
                                       @RequestPart("files") MultipartFile[] usesFiles,
                                       @RequestPart("files2") MultipartFile[] secondFiles,
                                       @RequestParam("languageID") String lang) {
        if (!userService.isAdmin(principal.getName())) {
            return new ModelAndView("redirect:/upload");
        }
        else {
            String message;
            if (usesFiles.length != 1 || secondFiles.length != 1) {
                message = "The number of use files and annotation files should be 1." ;
                model.addAttribute("message", message);
            }
            try {
                Language language = languageService.getLanguage(lang);
                if (tutorialService.hasTutorial(language)) {
                    throw new IOException(language + " already has a tutorial.");
                }
                uploadProgressService.createNewUploadProgress(language.getName(), principal.getName());
                List<String> usesPath = FileManager.temporarilyStoreFiles(usesFiles);
                List<String> goldAnnotationPath = FileManager.temporarilyStoreFiles(secondFiles);
                asyncUploadService.uploadNewTutorial(usesPath, goldAnnotationPath, language);
            } catch (IOException | EntityNotFoundException e) {
                model.addAttribute("message", "Failed to parse files: " + e.getMessage());
            }
            return new ModelAndView("redirect:/upload/tutorial", model);
        }
    }

    @Transactional
    @PostMapping(value = "/uploadWords", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ModelAndView uploadNewWords(Principal principal, ModelMap model,
                                       @RequestPart("files") MultipartFile[] usesFiles,
                                       @RequestPart(value = "files2", required = false) MultipartFile[] secondFiles,
                                       @RequestParam(value = "dataType", required = false, defaultValue = "uses") String dataType,
                                       @RequestParam("projectNameUpload") String projectName) {
        String message = "";
        if (!Objects.equals(secondFiles[0].getOriginalFilename(), "") && usesFiles.length != secondFiles.length) {
            message = "The number of use file and instance/annotations file does not match.";
        } else if (!projectService.existsByID(projectName)) {
            message = "The project you selected does no longer exist in the database.";
        } else if (uploadProgressService.existsUploadProgress(projectName)) {
            message = "The project is currently being updated, please try again later.";
        } else {
            uploadProgressService.createNewUploadProgress(projectName, principal.getName());
            try {
                List<String> files1 = FileManager.temporarilyStoreFiles(usesFiles);
                List<String> files2;
                if (Objects.equals(secondFiles[0].getOriginalFilename(), "")) {
                    files2 = null;
                }
                else {
                    files2 = FileManager.temporarilyStoreFiles(secondFiles);
                }
                asyncUploadService.uploadProject(principal, files1, files2, projectName, null, dataType, true);
            } catch (IOException e) {
                message = "Failed to parse files: " + e.getMessage();
            } catch (InstanceNotFoundException e) {
                logger.error("Got InstanceNotFoundException when it shouldn't have been raised.");
                model.addAttribute("message", "Error on upload, please contact durel@ims.uni-stuttgart.de: " + e.getMessage());
            }
        }
        model.addAttribute("message", message);
        return new ModelAndView("redirect:/myProjects/words", model);
    }
}

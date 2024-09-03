package durel.interfaces;

import durel.domain.model.Project;
import durel.domain.model.Lemma;
import durel.services.DeletionProgressService;
import durel.services.ProjectService;
import durel.services.WordService;
import durel.services.dataManagement.download.DownloadProcessService;
import durel.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.management.InstanceNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    private final DeletionProgressService deletionProgressService;

    private final UserService userService;

    private final WordService wordService;

    private final DownloadProcessService downloadProcessService;

    public ProjectController(ProjectService projectService, DeletionProgressService deletionProgressService, UserService userService,
                             WordService wordService, DownloadProcessService downloadProcessService) {
        this.projectService = projectService;
        this.deletionProgressService = deletionProgressService;
        this.userService = userService;
        this.wordService = wordService;
        this.downloadProcessService = downloadProcessService;
    }

    /**
     * Updates the details of a project
     */
    @PostMapping(value = "/update", params = {"projectName", "languageID", "visible", "annotators"})
    public ModelAndView updateProject(@RequestParam("projectName") String projectName,
                                @RequestParam("languageID") String languageID,
                                @RequestParam("visible") String visible,
                                @RequestParam("annotators") List<String> annotators,
                                      Principal principal, ModelMap model) {
        try {
            String owner = projectService.getProject(projectName).getCreator().getUsername();
            boolean admin = userService.isAdmin(principal.getName());
            if ((principal.getName().equals(owner) | admin)) {
                if (deletionProgressService.deletionProgressDoesNotExist(projectName)) {
                    projectService.updateProjectDetails(principal,
                            projectName,
                            languageID,
                            Boolean.parseBoolean(visible),
                            annotators);
                    model.addAttribute("message", "The project has successfully been updated.");
                } else {
                    model.addAttribute("message", "The update failed because the project is already being deleted.");
                }
            } else {
                model.addAttribute("message", "The update failed because you are not the owner of the project.");
            }
        } catch (InstanceNotFoundException e) {
            model.addAttribute("message", "The update failed because the project does not exist.");
        }
        return new ModelAndView("redirect:/myProjects", model);
    }

    /**
     * Downloads the content of a project, including all user annotations.
     */
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> downloadFiles(@RequestParam("projectName") String projectName) {
        Project project;
        try {
            // Retrieve project entity.
            project = projectService.getProject(projectName);
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.ok().body(null);
        }

        // Create a zip file containing all annotations.
        String zipPath = downloadProcessService.downloadProject(project);

        // Set up headers of the response so that the browser can download the content.
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + projectName + ".zip");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        // TODO Duplicate
        // Set up the file to be transferred.
        File file = new File(zipPath);
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        // Send the file to be downloader by the user.
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);

    }

    /**
     * Returns list of all available words of a given project.
     *
     * @return List of words.
     */
    @GetMapping("/loadProjects")
    @ResponseBody
    public ArrayList<String> loadProjects(@RequestParam String lang, Principal principal) {
        return projectService.getProjectNamesByUserAndLanguage(principal.getName(), lang);
    }

    /**
     * Returns a list of all annotators that have been granted access to a given project.
     */
    @GetMapping("/loadGrants")
    @ResponseBody
    public List<String> getAnnotatorsOfProject(@RequestParam String projectName) {
        try { //TODO: Check if this returns all annotators or all people with grants.
            return projectService.getAnnotatorsOfProject(projectName);
        } catch (InstanceNotFoundException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Returns list of all available words of a given project.
     *
     * @return List of words.
     */
    @GetMapping("/loadWords")
    @ResponseBody
    public ArrayList<String> loadWords(@RequestParam String projectName) {
        try {
            List<Lemma> lemmata = wordService.getWordsOfProject(projectName);
            // Transform the into a plain list.
            return wordService.fromWords2List(lemmata);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return null;
        }
    }
}

/**
 * This class produces the views for all pages that can be accessed via the dashboard. Endpoints are listed in the order
 * in which they are listed in the navbar on the webpage.
 */
package durel.interfaces;

import durel.dto.requests.projects.ProjectUpdateRequest;
import durel.dto.requests.common.SelectLemmaRequest;
import durel.dto.requests.statistics.WUGsPipelineRequest;
import durel.services.*;
import durel.services.dataManagement.upload.UploadProgressService;
import durel.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

@Controller
public class DashboardController {

    private final LanguageService languageService;

    private final UserService userService;

    private final ProjectService projectService;

    private final TaskService taskService;

    private final DeletionProgressService deletionProgressService;

    private final UploadProgressService uploadProgressService;

    @Autowired
    public DashboardController(LanguageService languageService, UserService userService, ProjectService projectService, TaskService taskService, DeletionProgressService deletionProgressService, UploadProgressService uploadProgressService) {
        this.languageService = languageService;
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.deletionProgressService = deletionProgressService;
        this.uploadProgressService = uploadProgressService;
    }

    // Annotation ------------------------------------------------------------------------------------------------------

    @GetMapping("/tutorial")
    public String setUpTutorialMenu(Model model, Principal principal) {
        model.addAttribute("tutorialLanguages", languageService.getTutorialLanguages());
        if (userService.checkUserTutorial(principal.getName())) {
            return "pages/tutorial-startpage-passed";
        }
        return "pages/tutorial-startpage";
    }

    @GetMapping("/annotation")
    public String setUpAnnotationMenu(Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        model.addAttribute("languages", languageService.getLanguages());
        model.addAttribute("requestWordData", new SelectLemmaRequest());
        return "pages/annotation-startpage";
    }

    // Task pages ----------------------------------------------------------------------------------------------------

    @GetMapping("/task")
    public String setUpViewTasksInfo(Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        return "pages/task/info";
    }

    @GetMapping("/task/create")
    public String setUpCreateTasksMenu(Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        model.addAttribute("languages", languageService.getLanguages());
        model.addAttribute("requestWordData", new SelectLemmaRequest());
        return "pages/task/create";
    }

    @GetMapping("/task/status")
    public String setUpViewTasks(Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        model.addAttribute("tasks", taskService.getTasks(principal.getName()));
        model.addAttribute("startedTasks", taskService.countTaskByStatus("TASK_STARTED"));
        model.addAttribute("pendingTasks", taskService.countTaskByStatus("TASK_PENDING"));
        return "pages/task/status";
    }

    // Data pages -----------------------------------------------------------------------------------------------------

    @GetMapping("/data")
    public String setUpConcordancesMenu(Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        model.addAttribute("requestWordData", new SelectLemmaRequest());
        model.addAttribute("languages", languageService.getLanguages());
        return "pages/data/info";
    }

    @GetMapping("/data/uses")
    public ModelAndView dataUsesRedirect(ModelMap model) {
        return new ModelAndView("redirect:/data", model);
    }

    @GetMapping("/data/annotations")
    public ModelAndView dataAnnotationsRedirect(ModelMap model) {
        return new ModelAndView("redirect:/data", model);
    }

    // Statistics pages ----------------------------------------------------------------------------------------------

    @GetMapping("/statistics")
    public String setUpStatisticsOverviewMenu(Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        model.addAttribute("languages", languageService.getLanguages());
        return "pages/statistics/info";
    }

    @GetMapping("/statistics/counts")
    public String setUpCountStatisticsMenu(Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        model.addAttribute("yourProjects", projectService.getOwnedProjectsForStatistics(principal.getName()));
        model.addAttribute("languages", languageService.getLanguages());
        return "pages/statistics/counts";
    }

    @GetMapping("/statistics/agreement")
    public String setUpAgreementStatisticsMenu(Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        model.addAttribute("yourProjects", projectService.getOwnedProjectsForStatistics(principal.getName()));
        return "pages/statistics/agreement";
    }

    @GetMapping("/statistics/showWUG")
    public String setUpVisualizationMenu(Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        model.addAttribute("languages", languageService.getLanguages());
        model.addAttribute("WUGsPipelineParameters", new WUGsPipelineRequest());
        return "pages/statistics/WUGStartPage";
    }

    // Project management ----------------------------------------------------------------------------------------------

    @GetMapping("/myProjects")
    public ModelAndView setUpMyProjectsMenu(ModelMap model) {
        model.addAttribute("message", "");
        return new ModelAndView("redirect:/myProjects", model);
    }

    @GetMapping(value = "/myProjects", params = "message")
    public String setUpMyProjectsMenu(@ModelAttribute("message") String message,
                                      Principal principal, Model model) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        if (!message.isEmpty()) {
            message += "<br>";
        }
        message += deletionProgressService.checkDeletionProgressStatus(principal.getName());
        model.addAttribute("message", message);
        model.addAttribute("languages", languageService.getLanguages());
        model.addAttribute("changedProject", new ProjectUpdateRequest());

        // Information for granting access to projects.
        if (userService.isAdmin(principal.getName())) {
            model.addAttribute("users", userService.getAllUsernames() ) ;
        } else {
            model.addAttribute("users", userService.getAllOtherUsernames(principal.getName()) ) ;
        }

        // Projects that can be managed in the project manager.
        model.addAttribute("yourProjects", projectService.getOwnedProjectsForSettings(principal.getName()));
        return "pages/manageProjects";
    }

    @GetMapping("/myProjects/words")
    public ModelAndView setUpModifyWordsMenu(ModelMap model) {
        model.addAttribute("message", "");
        return new ModelAndView("redirect:/myProjects/words", model);
    }

    @GetMapping(value = "/myProjects/words", params = "message")
    public String setUpModifyWordsMenu(@ModelAttribute("message") String message,
                                       Principal principal, Model model) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        if (!message.isEmpty()) {
            message += "<br>";
        }
        message += deletionProgressService.checkDeletionProgressStatus(principal.getName());
        if (!message.isEmpty()) {
            message += "<br>";
        }
        message += uploadProgressService.checkUploadProgressStatus(principal.getName());
        model.addAttribute("message", message);
        // Projects that can be managed in the project manager.
        model.addAttribute("yourProjects", projectService.getOwnedProjectsForSettings(principal.getName()));
        return "pages/manageWords";
    }

    // Upload pages ----------------------------------------------------------------------------------------------------

    @GetMapping("/upload")
    public String setUpUploadOverviewMenu(Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        return "pages/upload/info";
    }

    @GetMapping("/upload/uses")
    public ModelAndView setUpUploadUsesMenu(ModelMap model) {
        model.addAttribute("message", "");
        return new ModelAndView("redirect:/upload/uses", model);
    }

    @GetMapping(value = "/upload/uses", params = "message")
    public String setUpUploadUsesMenu(Model model, Principal principal, @ModelAttribute("message") String message) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        if (message.isEmpty()) {
            message = uploadProgressService.checkUploadProgressStatus(principal.getName());
        }
        model.addAttribute("message", message);
        model.addAttribute("languages", languageService.getLanguages());
        return "pages/upload/uses";
    }

    @GetMapping("/upload/instances")
    public ModelAndView setUpUploadInstancesMenu(ModelMap model) {
        model.addAttribute("message", "");
        return new ModelAndView("redirect:/upload/instances", model);
    }

    @GetMapping(value = "/upload/instances", params = "message")
    public String setUpUploadInstancesMenu(@ModelAttribute("message") String message,
                                           Model model, Principal principal) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        if (message.isEmpty()) {
            message = uploadProgressService.checkUploadProgressStatus(principal.getName());
        }
        model.addAttribute("message", message);
        model.addAttribute("languages", languageService.getLanguages());
        return "pages/upload/instances";
    }

    @GetMapping("/upload/annotations")
    public ModelAndView setUpUploadAnnotationsMenu(ModelMap model) {
        model.addAttribute("message", "");
        return new ModelAndView("redirect:/upload/annotations", model);
    }

    @GetMapping(value = "/upload/annotations", params = "message")
    public String setUpUploadAnnotationsMenu(Model model, Principal principal, @ModelAttribute("message") String message) {
        if (!userService.checkUserTutorial(principal.getName())) {
            return "redirect:/tutorial";
        }
        if (message.isEmpty()) {
            message = uploadProgressService.checkUploadProgressStatus(principal.getName());
        }
        model.addAttribute("message", message);
        model.addAttribute("languages", languageService.getLanguages());
        return "pages/upload/annotations";
    }

    @GetMapping("/upload/tutorial")
    public ModelAndView setUpUploadTutorialMenu(ModelMap model) {
        model.addAttribute("message", "");
        return new ModelAndView("redirect:/upload/tutorial", model);
    }

    @GetMapping(value = "/upload/tutorial", params = "message")
    public String setUpUploadTutorialMenu(@ModelAttribute("message") String message,
                                           Model model, Principal principal) {
        if (!userService.isAdmin(principal.getName())) {
            return "redirect:/upload";
        }
        if (message.isEmpty()) {
            message = uploadProgressService.checkUploadProgressStatus(principal.getName());
        }
        model.addAttribute("message", message);
        model.addAttribute("languages", languageService.getLanguages());
        return "pages/upload/tutorial";
    }

    @GetMapping("/tutorial/delete")
    public ModelAndView setUpDeleteTutorialMenu(ModelMap model) {
        model.addAttribute("message", "");
        return new ModelAndView("redirect:/tutorial/delete", model);
    }

    @GetMapping(value = "/tutorial/delete", params = "message")
    public String setUpDeleteTutorialMenu(@ModelAttribute("message") String message,
                                          Model model, Principal principal) {
        if (!userService.isAdmin(principal.getName())) {
            return "redirect:/tutorial";
        }
        if (message.isEmpty()) {
            message = deletionProgressService.checkDeletionProgressStatus(principal.getName());
        }
        model.addAttribute("message", message);
        model.addAttribute("tutorialLanguages", languageService.getTutorialLanguages());
        return "pages/deleteTutorial";
    }
}

package durel.interfaces;

import durel.domain.model.Language;
import durel.services.*;
import durel.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;

@Controller
public class DeletionController {

    private final UserService userService;

    private final TutorialService tutorialService;

    private final LanguageService languageService;

    private final DeletionProgressService deletionProgressService;

    private final WordService wordService;

    private final ProjectService projectService;

    private final DeletionService deletionService;

    @Autowired
    public DeletionController(UserService userService, TutorialService tutorialService, LanguageService languageService, DeletionProgressService deletionProgressService, WordService wordService, ProjectService projectService, DeletionService deletionService) {
        this.userService = userService;
        this.tutorialService = tutorialService;
        this.languageService = languageService;
        this.deletionProgressService = deletionProgressService;
        this.wordService = wordService;
        this.projectService = projectService;
        this.deletionService = deletionService;
    }

    /**
     * Deletes a word from the database.
     * Cascade triggers the deletion of sentences, sequences and annotations referencing the word.
     * This cannot be undone.
     *
     * @param lemma       word to delete
     * @param principal   User
     * @param projectName project identifier
     * @return web response.
     */
    @PostMapping(value = "/deleteWord")
    public ModelAndView deleteWord(final @Valid @ModelAttribute("word-select") String lemma,
                             final @Valid @ModelAttribute("projectNameDelete") String projectName,
                             Principal principal, final ModelMap model) {
        try {
            if (deletionProgressService.deletionProgressDoesNotExist(projectName + "," + lemma) &&
                    deletionProgressService.deletionProgressDoesNotExist(projectName) &&
                    wordService.getLemmaObjectByProjectNameAndLemma(projectName, lemma) != null) {
                boolean userIsOwnerOrAdmin = projectService.userIsOwnerOrAdmin(projectName, principal);
                if (userIsOwnerOrAdmin) {
                    deletionProgressService.createNewDeletionProgress(projectName + "," + lemma, principal.getName());
                    deletionService.deleteWord(projectName, lemma);
                }
                else {
                    model.addAttribute("message", "The deletion failed because you are not the owner of the project.");
                }
            }
            else {
                model.addAttribute("message", lemma + " is being deleted or has already been deleted.");
            }
        }
        catch (Exception e) {
            model.addAttribute("message", "Failed to update: " + e);
        }
        return new ModelAndView("redirect:/myProjects/words", model);
    }

    /**
     * Deletes a project from the database.
     * Cascade triggers the deletion of words, sentences, sequences and annotations referencing the project.
     * This cannot be undone.
     *
     * @param principal   User
     * @param projectName project identifier
     * @return web response.
     */
    @PostMapping("/deleteProject")
    public ModelAndView deleteProject(final @Valid @ModelAttribute("projectNameDelete") String projectName,
                                      Principal principal, ModelMap model) {
        try {
            if (deletionProgressService.deletionProgressDoesNotExist(projectName) && projectService.existsByID(projectName)) {
                boolean userIsOwnerOrAdmin = projectService.userIsOwnerOrAdmin(projectName, principal);
                if (userIsOwnerOrAdmin) {
                    deletionProgressService.createNewDeletionProgress(projectName, principal.getName());
                    deletionService.deleteProject(projectName);
                }
                else {
                    model.addAttribute("message", "The deletion failed because you are not the owner of the project.");
                }
            }
        }
        catch (Exception e) {
            model.addAttribute("message", "Failed to update project: " + e);
        }
        return new ModelAndView("redirect:/myProjects", model);
    }

    /**
     * Deletes a tutorial for a selected language if the logged-in user is an admin.
     *
     * @param principal   The principal object representing the logged-in user.
     * @param modelMap    The model map to hold attributes for the view.
     * @param lang        The selected language.
     * @return The ModelAndView object for redirection to the tutorial delete page.
     */
    @PostMapping("/deleteTutorial")
    public ModelAndView deleteTutorial(Principal principal, ModelMap modelMap,
                                       @RequestParam("languageID") String lang) {
        if (!userService.isAdmin(principal.getName())) {
            return new ModelAndView("redirect:/tutorial");
        }
        else {
            try {
                Language language = languageService.getLanguage(lang);
                if (!tutorialService.hasTutorial(language)) {
                    throw new IOException(language + " doesn't have a tutorial.");
                }
                deletionProgressService.createNewDeletionProgress(language.getName(), principal.getName());
                deletionService.deleteTutorial(language);
            } catch (IOException | EntityNotFoundException e) {
                modelMap.addAttribute("message", "Failed to delete tutorial " + lang);
            }
            return new ModelAndView("redirect:/tutorial/delete", modelMap);
        }
    }
}

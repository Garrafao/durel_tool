package durel.interfaces;

import durel.dto.requests.common.SelectLemmaRequest;
import durel.domain.model.Lemma;
import durel.services.UseService;
import durel.services.WordService;
import durel.services.annotation.AnnotationQueryService;
import durel.services.LanguageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@Controller
public class DataController {
    
    private final UseService useService;

    private final AnnotationQueryService annotationQueryService;

    private final LanguageService languageService;

    private final WordService wordService;

    @Autowired
    public DataController(UseService useService, AnnotationQueryService annotationQueryService, LanguageService languageService, WordService wordService) {
        this.useService = useService;
        this.annotationQueryService = annotationQueryService;
        this.languageService = languageService;
        this.wordService = wordService;
    }

    @RequestMapping(value = "/data")
    public ModelAndView entryPointDataPages(ModelMap model,
                                            @Valid @ModelAttribute(value = "requestWordData") SelectLemmaRequest selectLemmaRequest,
                                            final @Valid @ModelAttribute("dataType") String dataType, BindingResult bindingResult)  {
        if (!bindingResult.hasErrors() && !selectLemmaRequest.getProjectName().isEmpty() && selectLemmaRequest.getLemmas().length == 1) {
            model.addAttribute("project-select", selectLemmaRequest.getProjectName());
            model.addAttribute("word-select", selectLemmaRequest.getLemmas()[0]);
            switch (dataType) {
                case "uses" -> {
                    return new ModelAndView("redirect:/uses", model);
                }
                case "annotations" -> {
                    return new ModelAndView("redirect:/annotations", model);
                }
                case "instances" -> log.info("Show instances is not implemented yet.");
            }
        }
        return new ModelAndView("redirect:/data", model);
    }

    @RequestMapping(value = "/uses", method = {RequestMethod.POST, RequestMethod.GET}, params = {"project-select", "word-select"})
    public String setUpUsesPage(final @Valid @ModelAttribute("project-select") String selectedProject,
                                final @Valid @ModelAttribute("word-select") String selectedLemma,
                                final Model model) {
        try {
            Lemma lemma = wordService.getLemmaObjectByProjectNameAndLemma(selectedProject, selectedLemma);
            useService.setCurrentUseDTOs(lemma);
        }
        catch (Exception e) {
            model.addAttribute("languages", languageService.getLanguages());
            model.addAttribute("error", e);
            return "pages/data/info";
        }
        return setUpUsesPage(model);
    }

    @RequestMapping(value = "/uses", method = {RequestMethod.POST, RequestMethod.GET})
    public String setUpUsesPage(final Model model) {
        model.addAttribute("uses", useService.getCurrentUseDTOs());
        return "pages/data/uses";
    }

    @RequestMapping(value = "/annotations", method = {RequestMethod.POST, RequestMethod.GET}, params = {"project-select", "word-select"})
    public String setUpAnnotationsPage(final @Valid @ModelAttribute("project-select") String projectSelect,
                                       final @Valid @ModelAttribute("word-select") String wordSelect,
                                       final Model model, final Principal principal) {
        try {
            Lemma lemma = wordService.getLemmaObjectByProjectNameAndLemma(projectSelect, wordSelect);
            annotationQueryService.updateAnnotationViewDataList(lemma, principal.getName());
        }
        catch (Exception e) {
            model.addAttribute("languages", languageService.getLanguages());
            model.addAttribute("error", e);
            return "pages/data/uses";
        }
        return setUpAnnotationsPage(model);
    }

    @RequestMapping(value = "/annotations", method = {RequestMethod.POST, RequestMethod.GET})
    public String setUpAnnotationsPage(final Model model) {
        model.addAttribute("annotations", annotationQueryService.getCurrentAnnotations());
        return "pages/data/annotations";
    }
}

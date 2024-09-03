package durel.interfaces;

import durel.dto.responses.UseDTO;
import durel.services.dtoServices.UseDTOService;
import durel.services.annotationProcess.TutorialProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
public class TutorialController {

    private final TutorialProcessService tutorialProcessService;

    private final UseDTOService useDTOService;

    @Autowired
    public TutorialController(TutorialProcessService tutorialProcessService, UseDTOService useDTOService) {
        this.tutorialProcessService = tutorialProcessService;
        this.useDTOService = useDTOService;
    }

    @PostMapping(value = "/startTutorial")
    public ModelAndView startNewTutorial(final @Valid @ModelAttribute("languageID") String languageID, ModelMap model) {
        tutorialProcessService.startTutorial(languageID);
        return new ModelAndView("redirect:/tutorial/current", model);
    }

    @PostMapping(value = "/submitTutorialJudgment")
    public ModelAndView submitTutorialJudgment(final @Valid @ModelAttribute("judgment") String judgment, ModelMap model) {
        tutorialProcessService.saveJudgment(Float.parseFloat(judgment), "");
        return new ModelAndView("redirect:/tutorial/current", model);
    }

    @GetMapping(value = "/tutorial/current")
    public ModelAndView setUpNextTutorialPage(Principal principal, ModelMap model) {
        try {
            List<UseDTO> nextSentencePair = this.tutorialProcessService.getNextUsePair();
            model.addAttribute("tutorial_length", tutorialProcessService.noOfSentencePairs());
            model.addAttribute("passed", false);
            model.addAttribute("notPassed", false);

            if (nextSentencePair.isEmpty()) {
                model.addAttribute("currentPair", tutorialProcessService.getCurrentSequenceIndex());
                model.addAttribute("sentence1", useDTOService.constructEmptyUseDTO());
                model.addAttribute("sentence2", useDTOService.constructEmptyUseDTO());
                boolean passed = tutorialProcessService.computeTutorial(principal.getName());
                if (passed) {
                    model.addAttribute("passed", true);
                }
                else {
                    model.addAttribute("notPassed", true);
                }
                model.addAttribute("finished", true);
            }
            else {
                model.addAttribute("currentPair", tutorialProcessService.getCurrentSequenceIndex()+1);
                model.addAttribute("sentence1", nextSentencePair.get(0));
                model.addAttribute("sentence2", nextSentencePair.get(1));
                model.addAttribute("finished", false);
            }
        } catch (Exception e) {
            return new ModelAndView("redirect:/tutorial", model);
        }
        return new ModelAndView("pages/tutorial", model);
    }
}

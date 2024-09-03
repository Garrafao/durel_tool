package durel.interfaces;

import durel.dto.requests.common.SelectLemmaRequest;
import durel.dto.requests.common.SelectLemmaRequestValidator;
import durel.dto.responses.UseDTO;
import durel.services.dtoServices.UseDTOService;
import durel.services.annotationProcess.AnnotationProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * The {@code AnnotationProcessController} class is responsible for handling HTTP requests and controlling the annotation process.
 * <p>
 * The class contains a single handler method, {@code setUpNextAnnotationPage}, which is triggered by POST requests to {@code "/annotation"}.
 * The method performs an action based on the received parameters:
 * <ul>
 *   <li>If there are no binding errors, and the {@code projectName} is not empty, and there is exactly one lemma, it calls the
 *   {@link AnnotationProcessService#startAnnotation(String, String, String)} method of {@code AnnotationProcessService} to start the annotation process.
 *   <b>This is the case when a user starts or continues the annotation of the lemma coming from the {@link DashboardController#setUpAnnotationMenu(Model, Principal)} page.</b> </li>
 *   <li>If the {@code judgment} parameter is one of the valid values ("0", "1", "2", "3", "4"), it calls the
 *   {@link AnnotationProcessService#saveJudgment(float, String)} method of {@code AnnotationProcessService} to save the judgment and comment.
 *   <b>This is the case when a user is in the process of annotating a sequence, and is sending back the judgment for the previous use pair.</b></li>
 * </ul>
 * <p>
 * The method then retrieves the next pair of {@link UseDTO UseDTO} objects from {@code AnnotationProcessService} and adds them to the model along with other relevant attributes.
 * If there are no more pairs to annotate, it sets the {@code "finished"} attribute to {@code true}.</li>
 * <p>
 * The method returns a {@link org.springframework.web.servlet.ModelAndView ModelAndView} object with the {@code "pages/annotation"} view and the model as parameters.
 * <p>
 * If an exception occurs during the execution of the method, it adds the error to the model and redirects to the {@link DashboardController#setUpAnnotationMenu(Model, Principal)} page.
 */
@Controller
@Slf4j
public class AnnotationProcessController {

    private final AnnotationProcessService annotationProcessService;

    private final UseDTOService useDTOService;

    private final SelectLemmaRequestValidator selectLemmaRequestValidator;

    @Autowired
    public AnnotationProcessController(AnnotationProcessService annotationProcessService, UseDTOService useDTOService, SelectLemmaRequestValidator selectLemmaRequestValidator) {
        this.annotationProcessService = annotationProcessService;
        this.useDTOService = useDTOService;
        this.selectLemmaRequestValidator = selectLemmaRequestValidator;
    }

    @InitBinder("requestWordData")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(selectLemmaRequestValidator);
    }

    /**
     * Sets up the next annotation page based on the provided parameters.
     * <p>
     * This method is annotated with the @Transactional annotation, indicating that it should be executed within a transaction.
     *
     * @param judgment The judgment parameter received as a request parameter. It is optional and has a default value of "-1".
     * @param comment The comment parameter received as a request parameter. It is optional and has a default value of an empty string.
     * @param selectLemmaRequest The selectWordRequest parameter received as a request attribute. It is a validated model attribute of type SelectWordRequest.
     * @param bindingResult The bindingResult parameter received for validation purposes.
     * @param model The model parameter, used to add attributes for the view.
     * @param principal The principal parameter, representing the currently authenticated user.
     * @return A ModelAndView object representing the next annotation page.
     */
    @Transactional
    @PostMapping(value = "/annotation")
    public ModelAndView setUpNextAnnotationPage(final @RequestParam(value = "judgment", required = false, defaultValue = "-1") String judgment,
                                                final @RequestParam(value = "comment", required = false, defaultValue = "") String comment,
                                                final @Valid @ModelAttribute(value = "requestWordData") SelectLemmaRequest selectLemmaRequest,
                                                BindingResult bindingResult, final ModelMap model, Principal principal) {
        try {
            if (!bindingResult.hasErrors() && !selectLemmaRequest.getProjectName().isEmpty() && selectLemmaRequest.getLemmas().length == 1) {
                annotationProcessService.startAnnotation(principal.getName(), selectLemmaRequest.getProjectName(), selectLemmaRequest.getLemmas()[0]);
            }
            else if (judgment.equals("0") || judgment.equals("1") || judgment.equals("2") ||
                    judgment.equals("3") || judgment.equals("4")) {
                annotationProcessService.saveJudgment(Integer.parseInt(judgment), comment);
            }
            List<UseDTO> nextUsePair = annotationProcessService.getNextUsePair();
            model.addAttribute("seq_length", annotationProcessService.noOfSentencePairs());

            if (nextUsePair.isEmpty()) {
                model.addAttribute("currentPair", annotationProcessService.getCurrentSequenceIndex());
                model.addAttribute("sentence1", useDTOService.constructEmptyUseDTO());
                model.addAttribute("sentence2", useDTOService.constructEmptyUseDTO());
                model.addAttribute("finished", true);
            }
            else {
                model.addAttribute("currentPair", annotationProcessService.getCurrentSequenceIndex()+1);
                model.addAttribute("sentence1", nextUsePair.get(0));
                model.addAttribute("sentence2", nextUsePair.get(1));
                model.addAttribute("finished", false);
            }
        } catch (Exception e) {
            model.addAttribute("error", e);
            return new ModelAndView("redirect:/annotation", model);
        }
        return new ModelAndView("pages/annotation", model);
    }
}

package durel.interfaces;

import durel.dto.requests.common.SelectLemmaRequest;
import durel.dto.requests.common.SelectLemmaRequestValidator;
import durel.dto.responses.ResponseMessage;
import durel.domain.model.ComputationalAnnotationTask;
import durel.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * Class that handles all request from server and forms all responses.
 */
@Controller
public class TaskController {

    private final TaskService taskService;

    private final SelectLemmaRequestValidator selectLemmaRequestValidator;

    public TaskController(TaskService taskService, SelectLemmaRequestValidator selectLemmaRequestValidator) {
        this.taskService = taskService;
        this.selectLemmaRequestValidator = selectLemmaRequestValidator;
    }

    @InitBinder("requestWordData")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(selectLemmaRequestValidator);
    }

    /**
     * Retrieves the next task to be processed.
     *
     * @return ResponseEntity<Task> - The response entity containing the next task if available, otherwise returns null.
     */
    @GetMapping("/tasks/next")
    @ResponseBody
    public ResponseEntity<ComputationalAnnotationTask> getNextTask() {
        ComputationalAnnotationTask computationalAnnotationTask = taskService.getNextTask();
        if (computationalAnnotationTask == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else return ResponseEntity.status(HttpStatus.OK).body(computationalAnnotationTask);
    }

    @PatchMapping("/tasks/{task-id}/update-status/{status}")
    public ResponseEntity<ResponseMessage> uploadAnnotations(@PathVariable("task-id") int id,
                                                             @PathVariable("status") String status) {
        String message = "";
        taskService.updateTask(id, status);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));

    }

    @PostMapping(value = "/task")
    public ModelAndView createTask(
            Principal principal,
            final @RequestParam(name="thresholdValues[0]", defaultValue = "") String thresholdValue1,
            final @RequestParam(name="thresholdValues[1]", defaultValue = "") String thresholdValue2,
            final @RequestParam(name="thresholdValues[2]", defaultValue = "") String thresholdValue3,
            @ModelAttribute("annotation-mode") @Valid String mode,
            @ModelAttribute("requestWordData") @Valid SelectLemmaRequest selectLemmaRequest,
            BindingResult bindingResult, final ModelMap model) {
        if (!bindingResult.hasErrors()) {
            List<Float> thresholdValues = taskService.processThresholdValues(thresholdValue1, thresholdValue2, thresholdValue3);
            taskService.processTaskInsertion(selectLemmaRequest, principal.getName(), mode, thresholdValues);
        }
        return new ModelAndView("redirect:/task/status", model);
    }
}

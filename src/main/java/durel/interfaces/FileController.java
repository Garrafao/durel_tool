package durel.interfaces;

import durel.domain.model.ComputationalAnnotationTask;
import durel.domain.model.Lemma;
import durel.services.ProjectService;
import durel.services.TaskService;
import durel.services.WordService;
import durel.services.dataManagement.annotatorData.ExtendedInstancesExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.management.InstanceNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final ProjectService projectService;
    private final WordService wordService;
    private final ExtendedInstancesExportService extendedInstancesExportService;
    private final TaskService taskService;

    @Autowired
    public FileController(ProjectService projectService, WordService wordService, ExtendedInstancesExportService extendedInstancesExportService, TaskService taskService) {
        this.projectService = projectService;
        this.wordService = wordService;
        this.extendedInstancesExportService = extendedInstancesExportService;
        this.taskService = taskService;
    }

    @GetMapping("/annotatorInstances/{task-id}")
    @ResponseBody
    public String getInstancesWithWord(@PathVariable("task-id") int taskID) {
        ComputationalAnnotationTask computationalAnnotationTask = taskService.getTaskById(taskID);
        List<String> batches = processRequest(computationalAnnotationTask.getProjectName(), computationalAnnotationTask.getLemma());
        taskService.setTaskBatches(computationalAnnotationTask, batches.size());
        return batches.toString();
    }

    private Set<Lemma> retrieveWordList(String projectName, String lemma) throws InstanceNotFoundException {
        Set<Lemma> lemmata;
        if (lemma == null) {
            lemmata = projectService.getProject(projectName).getLemmas();
        } else {
            lemmata = new HashSet<>();
            lemmata.add(wordService.getLemmaObjectByProjectNameAndLemma(projectName, lemma));
        }
        return lemmata;
    }

    private List<String> processRequest(String projectName, String lemma) {

        List<String> batches = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(extendedInstancesExportService.getHeader());
        int lineCounter = 0;
        try {
            for (Lemma word : retrieveWordList(projectName, lemma)) {
                for (String line : extendedInstancesExportService.exportExtendedInstances(word)) {
                    stringBuilder.append(line);
                    lineCounter++;
                    if (lineCounter >= 1000) {
                        batches.add(stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(extendedInstancesExportService.getHeader());
                        lineCounter = 0;
                    }
                }
            }
            batches.add(stringBuilder.toString());
        } catch (InstanceNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return batches;
    }
}

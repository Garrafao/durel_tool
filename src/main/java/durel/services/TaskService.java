package durel.services;

import durel.domain.model.User;
import durel.domain.model.ComputationalAnnotationTask;
import durel.domain.repository.TaskDAO;
import durel.dto.requests.common.SelectLemmaRequest;
import durel.services.user.UserService;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskService {
    private final TaskDAO taskDAO;

    private final UserService userService;

    private final ProjectService projectService;

    public TaskService(TaskDAO taskDAO, UserService userService, ProjectService projectService) {
        this.taskDAO = taskDAO;
        this.userService = userService;
        this.projectService = projectService;
    }

    public List<ComputationalAnnotationTask> getTasks(String username) {
        if (userService.isAdmin(username)) {
            return taskDAO.findAll();
        }
        return taskDAO.findByCreator_UsernameOrderByIdAsc(username);
    }

    public int countTaskByStatus(String status) {
        return taskDAO.countByStatus(status);
    }

    public List<Float> processThresholdValues(String thresholdValue1, String thresholdValue2, String thresholdValue3) {
        return Stream.of(thresholdValue1, thresholdValue2, thresholdValue3)
                .map(s -> {
                    try {
                        return Float.parseFloat(s);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted().collect(Collectors.toList());
    }

    public void processTaskInsertion(SelectLemmaRequest selectLemmaRequest, String username, String mode, List<Float> thresholdValues) {
        if (selectLemmaRequest.getLemmas().length == 0) {
            insertTask(selectLemmaRequest.getProjectName(), username, null, mode, thresholdValues);
        } else {
            for (String lemma : selectLemmaRequest.getLemmas()) {
                insertTask(selectLemmaRequest.getProjectName(), username, lemma, mode, thresholdValues);
            }
        }
    }

    public ComputationalAnnotationTask insertTask(String projectName, String username, String lemma, String mode, List<Float> thresholdValues) {
        User user = userService.getUserByUsername(username);
        try {
            projectService.getProject(projectName);
        } catch (InstanceNotFoundException e) {
            return null;
        }
        ComputationalAnnotationTask computationalAnnotationTask = new ComputationalAnnotationTask("TASK_PENDING", mode, projectName, lemma, user, thresholdValues);
        return taskDAO.save(computationalAnnotationTask);
    }

    public ComputationalAnnotationTask updateTask(int id, String status) {
        ComputationalAnnotationTask computationalAnnotationTask = taskDAO.findById(id);
        computationalAnnotationTask.setStatus(status);
        return taskDAO.save(computationalAnnotationTask);
    }

    @Transactional
    public ComputationalAnnotationTask getNextTask() {
        List<ComputationalAnnotationTask> tasksToDo = taskDAO.findByStatus("TASK_PENDING");
        List<ComputationalAnnotationTask> tasksStarted = taskDAO.findByStatus("TASK_STARTED");
        if (!tasksToDo.isEmpty() && tasksStarted.size() < 3) {
            ComputationalAnnotationTask computationalAnnotationTask = tasksToDo.get(0);
            return updateTask(computationalAnnotationTask.getId(), "TASK_STARTED");
        } else {
            return null;
        }
    }

    @Transactional
    public void setTaskBatches(ComputationalAnnotationTask computationalAnnotationTask, int total_batches) {
        computationalAnnotationTask.setTotal_batches(total_batches);
        taskDAO.save(computationalAnnotationTask);
    }

    public ComputationalAnnotationTask getTaskById(int id) {
        return taskDAO.findById(id);
    }
}

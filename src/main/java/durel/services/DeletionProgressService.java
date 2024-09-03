package durel.services;

import durel.domain.model.DeletionProgress;
import durel.domain.repository.DeletionProgressDAO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeletionProgressService {

    private final DeletionProgressDAO deletionProgressDAO;

    public DeletionProgressService(DeletionProgressDAO deletionProgressDAO) {
        this.deletionProgressDAO = deletionProgressDAO;
    }

    public boolean deletionProgressDoesNotExist(String entityName) {
        return !deletionProgressDAO.existsByEntityName(entityName);
    }

    public String checkDeletionProgressStatus(String username) {
        List<DeletionProgress> deletionProgresses = deletionProgressDAO.getByCreator(username);
        StringBuilder message = new StringBuilder();
        List<DeletionProgress> progressesToDelete = new ArrayList<>();
        for (DeletionProgress deletionProgress: deletionProgresses) {
            String progress = deletionProgress.getProgress();
            message.append(deletionProgress.getEntityName()).append(": ").append(progress).append("<br>");
            if (progress.equals("Deletion succeeded.")) {
                progressesToDelete.add(deletionProgress);
            }
        }
        deletionProgressDAO.deleteAll(progressesToDelete);
        return message.toString();
    }

    public void createNewDeletionProgress(String entityName, String username) {
        deletionProgressDAO.save(new DeletionProgress(entityName, "Deletion in progress.", username));
    }
}

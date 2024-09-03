package durel.services.dataManagement.upload;

import durel.domain.model.UploadProgress;
import durel.domain.repository.UploadProgressDAO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UploadProgressService {

    private final UploadProgressDAO uploadProgressDAO;

    public UploadProgressService(UploadProgressDAO uploadProgressDAO) {
        this.uploadProgressDAO = uploadProgressDAO;
    }

    public String checkUploadProgressStatus(String username) {
        List<UploadProgress> uploadProgresses = uploadProgressDAO.getByCreator(username);
        StringBuilder message = new StringBuilder();
        List<UploadProgress> progressesToDelete = new ArrayList<>();
        for (UploadProgress uploadProgress : uploadProgresses) {
            String progress = uploadProgress.getProgress();
            message.append(uploadProgress.getProject()).append(": ").append(progress).append("<br>");
            if (!progress.equals("Parsing files. Please refresh the page to see progress.") && !progress.equals("Loading into the database. Please refresh the page to see progress.")) {
                progressesToDelete.add(uploadProgress);
            }
        }
        uploadProgressDAO.deleteAll(progressesToDelete);
        return message.toString();
    }

    public void createNewUploadProgress(String projectName, String username) {
        uploadProgressDAO.save(new UploadProgress(projectName, "Parsing files. Please refresh the page to see progress.", username));
    }

    public void deleteUploadProgress(String username) {
        uploadProgressDAO.deleteAll(uploadProgressDAO.getByCreator(username));
    }

    public void deleteUploadProgress(String username, String projectName) {
        uploadProgressDAO.deleteByProjectAndCreator(projectName, username);
    }

    public void updateUploadProgress(String projectName, String newStatus) {
        uploadProgressDAO.getByProject(projectName).setProgress(newStatus);
    }

    public boolean existsUploadProgress(String projectName) {
        return uploadProgressDAO.existsByProject(projectName);
    }
}

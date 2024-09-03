package durel.domain.repository;

import durel.domain.model.UploadProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadProgressDAO extends JpaRepository<UploadProgress, Integer>{
    boolean existsByProject(String project);

    List<UploadProgress> getByCreator(String username);

    UploadProgress getByProject(String project);

    void deleteByProjectAndCreator(String project, String creator);
}

package durel.domain.repository;

import durel.domain.model.DeletionProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeletionProgressDAO extends JpaRepository<DeletionProgress, Integer>{

    boolean existsByEntityName(String project);

    DeletionProgress getByEntityName(String project);

    List<DeletionProgress> getByCreator(String username);
}

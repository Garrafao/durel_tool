package durel.domain.repository;

import java.util.List;

import durel.domain.model.ComputationalAnnotationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskDAO extends JpaRepository<ComputationalAnnotationTask, Integer>{
    List<ComputationalAnnotationTask> findByStatus(String status);

    ComputationalAnnotationTask findById(int id);

    List<ComputationalAnnotationTask> findBySelectedCompAnnotator(String annotatorType);

    List<ComputationalAnnotationTask> findByCreator_UsernameOrderByIdAsc(String username);

    int countByStatus(String status);
}

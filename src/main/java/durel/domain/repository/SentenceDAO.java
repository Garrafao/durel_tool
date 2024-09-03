package durel.domain.repository;

import durel.domain.model.Project;
import durel.domain.model.Use;
import durel.domain.model.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface SentenceDAO extends JpaRepository<Use,Integer> {

    List<Use> findByLemma_Id(int id);

    Use findByLemma_IdAndCsvId(int id, String csvId);

    int countByLemma(Lemma lemma);

    List<Use> findByLemmaOrderByIdAsc(Lemma lemma);

    int countByLemma_Project(Project project);

    Stream<Use> findByLemma_Project(Project project);
}

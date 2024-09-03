package durel.domain.repository;

import durel.domain.model.Project;
import durel.domain.model.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaDAO extends JpaRepository<Lemma, Integer> {

    long deleteByProjectAndLemma(Project project, String word);

    Optional<Lemma> findByProject_ProjectNameAndLemma(String projectName, String word);

    List<Lemma> findByProject_ProjectNameOrderByLemmaAsc(String projectName);
}

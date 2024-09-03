package durel.domain.repository;

import durel.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SequenceDAO extends JpaRepository<AnnotationSequence, AnnotationSequenceId> {

    Optional<AnnotationSequence> findByLemmaAndUser(Lemma lemma, User username);

    List<AnnotationSequence> findAllByLemma_Project(Project project);
}

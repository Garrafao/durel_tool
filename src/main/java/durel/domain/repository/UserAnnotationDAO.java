package durel.domain.repository;

import durel.domain.model.annotation.UserAnnotation;
import durel.domain.model.UsePairAndAnnotator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAnnotationDAO extends JpaRepository<UserAnnotation, UsePairAndAnnotator> {

    Optional<UserAnnotation> findByIdOrId(UsePairAndAnnotator id1, UsePairAndAnnotator id2);

}

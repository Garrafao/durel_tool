package durel.domain.repository;

import durel.domain.model.Language;
import durel.domain.model.TutorialUse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorialUseDAO extends JpaRepository<TutorialUse, Integer> {

    List<TutorialUse> findByTutorial_LangAndPairIdOrderByIdAsc(Language lang, int pairID);
}

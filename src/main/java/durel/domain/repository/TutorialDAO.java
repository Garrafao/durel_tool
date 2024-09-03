package durel.domain.repository;

import durel.domain.model.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorialDAO extends JpaRepository<Tutorial,Integer> {

    Tutorial findByLang_Code(String code);
}

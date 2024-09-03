package durel.domain.repository;

import durel.domain.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageDAO extends JpaRepository<Language, String> {

    List<Language> findByTutorialNotNull();

    List<Language> findByIsLocaleTrue();

}

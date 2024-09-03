package durel.domain.repository;

import durel.domain.model.User;
import durel.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserDAO extends JpaRepository<User, String> {

    User findByUsername(String username);

    List<User> findByUsernameNotLike(String username);

    boolean existsByUsername(String username);

    Set<User> findDistinctByAnnotationSequences_Lemma_ProjectOrderByUsernameAsc(Project project);
}

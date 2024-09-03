package durel.domain.repository;

import durel.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProjectDAO extends JpaRepository<Project, String> {

    List<Project> findByCreator_UsernameOrderByProjectNameAsc(String username);

    // This has to be a set, otherwise there will be duplicates!!
    Set<Project> findByCreator_UsernameOrAnnotators_UsernameOrIsPublicTrueOrderByProjectNameAsc(String creator, String annotator);

}

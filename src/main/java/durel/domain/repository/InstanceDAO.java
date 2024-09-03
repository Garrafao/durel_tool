package durel.domain.repository;

import durel.domain.model.Instance;
import durel.domain.model.Project;
import durel.domain.model.Use;
import durel.domain.model.UsePair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanceDAO extends JpaRepository<Instance, Integer>{

    long deleteByProject(Project project);

    long deleteByProjectAndLemma(Project project, String lemma);

    List<Instance> findByProject_ProjectName(String projectName);

    List<Instance> findByProjectAndLemma(Project project, String lemma);

    int countByProject(Project project);

    int countByProjectAndLemma(Project project, String lemma);

    boolean existsByUsePair(UsePair<Use> pair);
}

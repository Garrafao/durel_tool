package durel.services;

import durel.domain.model.DeletionProgress;
import durel.domain.model.Language;
import durel.domain.model.Lemma;
import durel.domain.repository.DeletionProgressDAO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class DeletionService {

    private final WordService wordService;

    private final ProjectService projectService;

    private final PairService pairService;

    private final TutorialService tutorialService;

    private final DeletionProgressDAO deletionProgressDAO;

    public DeletionService(WordService wordService, ProjectService projectService, PairService pairService, TutorialService tutorialService, DeletionProgressDAO deletionProgressDAO) {
        this.wordService = wordService;
        this.projectService = projectService;
        this.pairService = pairService;
        this.tutorialService = tutorialService;
        this.deletionProgressDAO = deletionProgressDAO;
    }


    @Async
    @Transactional
    public void deleteProject(String projectName) {
        projectService.deleteProjectByProjectName(projectName);
        DeletionProgress deletionProgress = deletionProgressDAO.getByEntityName(projectName);
        deletionProgress.setProgress("Deletion succeeded.");
    }

    @Async
    @Transactional
    public void deleteWord(String projectName, String lemma) {
        Lemma word = wordService.getLemmaObjectByProjectNameAndLemma(projectName, lemma);
        if (word != null) {
            pairService.deleteByProjectNameAndLemma(word.getProject(), word.getLemma());
            wordService.deleteWordByProjectAndLemma(word.getProject(), word.getLemma());
            DeletionProgress deletionProgress = deletionProgressDAO.getByEntityName(projectName + "," + lemma);
            deletionProgress.setProgress("Deletion succeeded.");
        } else {
            DeletionProgress deletionProgress = deletionProgressDAO.getByEntityName(projectName + "," + lemma);
            deletionProgress.setProgress("Deletion failed.");
        }
    }

    @Transactional
    public void deleteTutorial(Language language) {
        tutorialService.deleteTutorial(language);
        DeletionProgress deletionProgress = deletionProgressDAO.getByEntityName(language.getName());
        deletionProgress.setProgress("Deletion succeeded.");
    }
}

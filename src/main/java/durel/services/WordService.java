package durel.services;

import durel.services.dataManagement.uploadData.UseData;
import durel.domain.model.Project;
import durel.domain.model.Use;
import durel.domain.model.Lemma;
import durel.domain.repository.LemmaDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WordService {

    private static final Logger logger = LoggerFactory.getLogger(WordService.class);

    private final LemmaDAO lemmaDAO;

    @Autowired
    public WordService(LemmaDAO lemmaDAO) {
        this.lemmaDAO = lemmaDAO;
    }

    /**
     * Get all word entities belonging to a project.
     */
    public List<Lemma> getWordsOfProject(String projectName) {
        return lemmaDAO.findByProject_ProjectNameOrderByLemmaAsc(projectName) ;
    }

    /**
     * Fetches a word entity from the database given a projectName and lemma.
     */
    public Lemma getLemmaObjectByProjectNameAndLemma(String projectName, String lemma) {
        Optional<Lemma> word = lemmaDAO.findByProject_ProjectNameAndLemma(projectName, lemma);
        return word.orElse(null);
    }

    /**
     * Converts a list of words into a list of strings (with the name of the words).
     */
    public ArrayList<String> fromWords2List(List<Lemma> lemmata) {
        ArrayList<String> l = new ArrayList<>();
        for (Lemma w : lemmata){
            l.add(w.getLemma())  ;
        }
        return l ;
    }

    public Lemma createWord(Project project, List<UseData> usesFile) {
        if (project == null || usesFile == null || usesFile.isEmpty()) {
            // Handle error appropriately
            throw new IllegalArgumentException("The project cannot be null and usesFile cannot be empty");
        }
        return new Lemma(project, usesFile.get(0).getLemma());
    }

    @Transactional
    public void saveWord(Lemma lemma) {
        lemmaDAO.save(lemma);
    }

    @Transactional
    public Lemma createAndSaveSentencesAndWord(Project project, List<UseData> usesFile) {
        Lemma lemma = createWord(project, usesFile);
        for (UseData use : usesFile) {
            if (use != null) {
                Use sentence = new Use(lemma, use.getPos(), use.getDate(),
                        use.getGrouping(), use.getIdentifier(), use.getDescription(),
                        use.getContext(), use.getTokenIndex(), use.getSentenceIndex());
                lemma.addSentence(sentence);
            } else {
                logger.warn("A null UseData was found when creating sentences for the word");
            }
        }
        saveWord(lemma);
        logger.info("Word saved: {}", lemma.getLemma());
        return lemma;
    }

    @Transactional
    public void deleteWordByProjectAndLemma(Project project, String lemma) {
        lemmaDAO.deleteByProjectAndLemma(project, lemma);
    }
}

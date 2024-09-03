package durel.services;

import durel.services.dataManagement.uploadData.AnnotationData;
import durel.services.dataManagement.uploadData.UseData;
import durel.domain.model.Language;
import durel.domain.model.Tutorial;
import durel.domain.model.TutorialUse;
import durel.domain.repository.TutorialDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TutorialService {
    private static final Logger logger = LoggerFactory.getLogger(TutorialService.class);

    private final TutorialDAO tutorialDAO;

    private final TutorialSentenceService tutorialSentenceService;

    private final TutorialAnnotationService tutorialAnnotationService;

    @Autowired
    public TutorialService(TutorialDAO tutorialDAO, TutorialSentenceService tutorialSentenceService, TutorialAnnotationService tutorialAnnotationService) {
        this.tutorialDAO = tutorialDAO;
        this.tutorialSentenceService = tutorialSentenceService;
        this.tutorialAnnotationService = tutorialAnnotationService;
    }

    public boolean hasTutorial(Language language) {
        return tutorialDAO.findByLang_Code(language.getCode()) != null;
    }

    /**
     * Inserts a tutorial in the database.
     * @param sentences List of sentences of the tutorial.
     * @param tutorialJudgments Gold annotations.
     * @param lang Language of the tutorial
     */
    @Transactional
    public void saveTutorial(List<UseData> sentences, List<AnnotationData> tutorialJudgments, Language lang) {
        // Insert into the tutorial table.
        Tutorial tutorial = new Tutorial();
        tutorial.setLang(lang);
        tutorial = tutorialDAO.save(tutorial);
        Map<String, UseData> sentenceMap = new HashMap<>();
        for (UseData sentence : sentences) {
            sentenceMap.put(sentence.getIdentifier(), sentence);
        }
        // Iterate sentences and gold annotations (in pairs).
        for (int i = 0; i<tutorialJudgments.size(); i++) {
            AnnotationData judgment = tutorialJudgments.get(i);
            UseData firstSentence = sentenceMap.get(judgment.getIdentifierOne());
            UseData secondSentence = sentenceMap.get(judgment.getIdentifierTwo());
            TutorialUse leftSentence = tutorialSentenceService.insertTutorialSentence(firstSentence, tutorial, i);
            TutorialUse rightSentence = tutorialSentenceService.insertTutorialSentence(secondSentence, tutorial, i);
            tutorialAnnotationService.insertGoldAnnotation(tutorialJudgments.get(i), leftSentence, rightSentence);
        }
        logger.info("Added all sentences and gold annotations for new tutorial " + lang.getName() + ".");
    }

    @Transactional
    public void deleteTutorial(Language language) {
        Tutorial tutorial = tutorialDAO.findByLang_Code(language.getCode());
        tutorialDAO.delete(tutorial);
    }
}
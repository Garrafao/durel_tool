package durel.services.annotationProcess;

import durel.dto.responses.UseDTO;
import durel.session.TutorialSessionData;
import durel.domain.repository.TutorialDAO;
import durel.services.Korrelation;
import durel.services.TutorialAnnotationService;
import durel.services.TutorialSentenceService;
import durel.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TutorialProcessService implements DefaultAnnotationProcessService {

    // DAO
    private final TutorialDAO tutorialDAO;

    // Services
    private final UserService userService;

    private final TutorialAnnotationService tutorialAnnotationService;

    private final TutorialSentenceService tutorialSentenceService;

    // Session Data Container
    private final TutorialSessionData tutorialSessionData;

    @Autowired
    public TutorialProcessService(TutorialDAO tutorialDAO, TutorialSentenceService tutorialSentenceService, UserService userService, TutorialAnnotationService tutorialAnnotationService, TutorialSessionData tutorialSessionData) {
        this.tutorialDAO = tutorialDAO;
        this.tutorialSentenceService = tutorialSentenceService;
        this.userService = userService;
        this.tutorialAnnotationService = tutorialAnnotationService;
        this.tutorialSessionData = tutorialSessionData;
    }

    @Override
    public int getCurrentSequenceIndex() {
        return tutorialSessionData.getCurrentSentencePair();
    }

    /**
     * Saves the new judgment to the session data.
     */
    @Override
    public void saveJudgment(float judgment, String comment) {
        tutorialSessionData.saveJudgment((int) judgment);
    }

    /**
     * Fetches the next sentence pair from the tutorialSentenceService.
     * @return the next pair of tutorial sentences as VSentences. If there is no new sentence pair, an empty list is returned.
     */
    @Override
    public List<UseDTO> getNextUsePair() {
        if (tutorialSessionData.getCurrentSentencePair() < noOfSentencePairs()) {
            return tutorialSentenceService.getPairOfTutorialSentence(tutorialSessionData.getCurrentTutorial().getLang(), tutorialSessionData.getCurrentSentencePair());
        }
        else {
            // No new sentence pair.
            return new ArrayList<>();
        }
    }

    /**
     * Fetches the number of sentence pairs in a tutorial
     */
    @Override
    public int noOfSentencePairs() {
        return tutorialSessionData.getCurrentTutorial().getTutorialUses().size()/2 ;
    }

    /**
     * Initializes the tutorial session data for a new tutorial of the given language.
     */
    public void startTutorial(String languageCode) {
        tutorialSessionData.setCurrentTutorial(tutorialDAO.findByLang_Code(languageCode));
        tutorialSessionData.setCurrentSentencePair(0);
        tutorialSessionData.setJudgments(new int[noOfSentencePairs()]);
    }

    /**
     * Computes the correlation between the gold and user annotations. If the correlation is higher than 0.6, the
     * tutorial is passed and the Annotator entity gets updated.
     */
    public boolean computeTutorial(String username) {

        // Put gold standard annotations in String Arrays that are distinctive by sentence ID pairs
        ArrayList<Integer[]> gold = new ArrayList<>();
        ArrayList<Integer[]> annotator = new ArrayList<>();

        // Iterate sentence pairs.
        for (int i = 0; i < noOfSentencePairs(); i++ ) {
            List<UseDTO> nextPairOfTutorialSentence = tutorialSentenceService.getPairOfTutorialSentence(tutorialSessionData.getCurrentTutorial().getLang(), i);
            int s1 = nextPairOfTutorialSentence.get(0).getId();
            int s2 = nextPairOfTutorialSentence.get(1).getId();

            Integer[] goldDoc = {s1, s2, Math.round(tutorialAnnotationService.getGoldAnnotationByIDs(s1, s2).getJudgment())};
            gold.add(goldDoc);

            Integer[] annoDoc = {s1, s2, tutorialSessionData.getJudgments()[i]};
            annotator.add(annoDoc);
        }
        // compute Spearman's rank correlation coefficient of the two annotation lists
        Korrelation corr = new Korrelation(gold, annotator);
        double c = corr.computeCorrelation();

        // Tutorial is passed if coefficient is bigger than 0.6
        if (c > 0.6) {
            userService.assignTutorialToUser(username, tutorialSessionData.getCurrentTutorial(), c);
            return true;
        }
        return false;
    }
}

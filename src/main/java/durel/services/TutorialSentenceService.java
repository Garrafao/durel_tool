package durel.services;

import durel.dto.responses.UseDTO;
import durel.services.dataManagement.uploadData.UseData;
import durel.domain.model.Language;
import durel.domain.model.Tutorial;
import durel.domain.model.TutorialUse;
import durel.domain.repository.TutorialUseDAO;
import durel.services.dtoServices.UseDTOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TutorialSentenceService {

    private final TutorialUseDAO tutorialUseDAO;

    private final UseDTOService useDTOService;

    @Autowired
    TutorialSentenceService(TutorialUseDAO tutorialUseDAO, UseDTOService useDTOService) {
        this.tutorialUseDAO = tutorialUseDAO;
        this.useDTOService = useDTOService;
    }

    /**
     * Fetches a pair of sentences to be annotated given a pairID.
     * @param lang the tutorial language
     * @param pairID the id of the next tutorial sentence pair
     * @return the next pair of the tutorial
     */
    public List<UseDTO> getPairOfTutorialSentence(Language lang, int pairID) {
        List<TutorialUse> nextPair = tutorialUseDAO.findByTutorial_LangAndPairIdOrderByIdAsc(lang, pairID);
        // Send them back to the front-end.
        List<UseDTO> nextPair2 = new ArrayList<>();
        nextPair2.add(fromTutorialSentenceToVSentence(nextPair.get(0)));
        nextPair2.add(fromTutorialSentenceToVSentence(nextPair.get(1)));

        return nextPair2;
    }

    /**
     * Inserts a new tutorial sentence into the tutorial sentence table
     */
    @Transactional
    public TutorialUse insertTutorialSentence(@NotNull UseData sentence, @NotNull Tutorial tutorial, int i) throws NumberFormatException, DataAccessException {
        TutorialUse tutorialUse = new TutorialUse();
        tutorialUse.setTutorial(tutorial);
        tutorialUse.setWord(sentence.getLemma());
        tutorialUse.setContext(sentence.getContext());
        tutorialUse.setIndexesTargetToken(sentence.getTokenIndex());
        tutorialUse.setIndexesTargetSentence(sentence.getSentenceIndex());
        tutorialUse.setPairId(i);
        return tutorialUseDAO.save(tutorialUse);
    }

    /**
     * Converts a model tutorialSentence into a view Sentence.
     * @param sentence a tutorial sentence
     * @return a VSentence containing the data of the tutorial sentence
     */
    public UseDTO fromTutorialSentenceToVSentence(@NotNull TutorialUse sentence) {
        return useDTOService.constructSentenceDTOfromTutorialSentence(sentence);
    }
}

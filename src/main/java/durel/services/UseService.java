package durel.services;

import durel.dto.responses.UseDTO;
import durel.services.dtoServices.UseDTOService;
import durel.session.DataSessionData;
import durel.domain.model.Project;
import durel.domain.model.Use;
import durel.domain.model.Lemma;
import durel.domain.repository.SentenceDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UseService {

    private final SentenceDAO sentenceDAO;

    private final UseDTOService useDTOService;

    private final DataSessionData dataSessionData;

    @Autowired
    public UseService(SentenceDAO sentenceDAO, UseDTOService useDTOService, DataSessionData dataSessionData) {
        this.sentenceDAO = sentenceDAO;
        this.useDTOService = useDTOService;
        this.dataSessionData = dataSessionData;
    }

    /**
     * Fetches all sentence ids of a given word.
     */
    @Transactional
    public List<String> getSentenceCsvIDsOfWord(Lemma lemma) {
        List<String> ids = new ArrayList<>();
        for (Use use : sentenceDAO.findByLemmaOrderByIdAsc(lemma))
        {
            ids.add(use.getCsvId());
        }
        return ids;
    }

    /**
     * Fetches all sentence ids of a given word.
     */
    @Transactional
    public List<Integer> getSentenceIDsOfWord(Lemma lemma) {
        List<Integer> ids = new ArrayList<>();
        for (Use use : sentenceDAO.findByLemmaOrderByIdAsc(lemma))
        {
            ids.add(use.getId());
        }
        return ids;
    }

    @Transactional
    public Map<String, Use> getMapOfAllSentenceCSVIdsToSentencesInProject(Project project) {
        return sentenceDAO.findByLemma_Project(project).collect(Collectors.toMap(Use::getCsvId, obj -> obj));
    }

    /**
     * Fetches a sentence entity from the database given an id.
     */
    @Transactional
    public Use getSentence(Integer sentenceID) {
        return sentenceDAO.findById(sentenceID).orElse(null) ;
    }

    @Transactional
    public int getNoOfSentencesOfWord(Lemma lemma) { return sentenceDAO.countByLemma(lemma); }

    @Transactional
    public int getNoOfSentencesOfProject(Project project) { return sentenceDAO.countByLemma_Project(project); }

    /**
     * Returns the list of view sentences for the concordance table
     */
    @Transactional
    public void setCurrentUseDTOs(Lemma lemma) throws InstanceNotFoundException {
        ArrayList<UseDTO> useDTOS = new ArrayList<>();
        List<Use> uses = sentenceDAO.findByLemma_Id(lemma.getId());
        if (uses.isEmpty()) {
            throw new InstanceNotFoundException();
        }
        for (Use use : uses) {
            useDTOS.add(getSentenceDTO(use));
        }
        this.dataSessionData.setUseDTOs(useDTOS);
    }

    public List<UseDTO> getCurrentUseDTOs() {
        return dataSessionData.getUseDTOs();
    }

    /**
     * Fetches a view sentence from given an id.
     */
    public UseDTO getSentenceDTO(Use use) {
        return useDTOService.constructSentenceDTOfromAnnotationSentence(use);
    }

    /**
     * Checks if the specified sentences exist and are associated with the given word.
     *
     * @param lemma       The Word object associated with the sentences. Can be null.
     * @param use1  The first sentence. Can be null.
     * @param use2  The second sentence. Can be null.
     * @throws IllegalArgumentException   If the sentences do not match the specified word, or if the provided pair does not belong to one word.
     * @throws InstanceNotFoundException  If the first or second sentence is invalid.
     */
    void checkSentencePairing(Lemma lemma, Use use1, Use use2)
            throws IllegalArgumentException, InstanceNotFoundException {
        // Check if the retrieved sentences are actually the sentences that we need.
        if (use1 == null || use2 == null) {
            throw new InstanceNotFoundException("Sentence not found! " + lemma.getLemma());
        } else if (lemma != null && (!use1.getLemma().equals(lemma) || !use2.getLemma().equals(lemma))) {
            throw new IllegalArgumentException("Found sentences " + use1 + ", " + use2 + ", but words don't match given word " + lemma.getLemma());
        } else if (!use1.getLemma().equals(use2.getLemma())) {
            throw new IllegalArgumentException("Pair does not belong to one word! " + use1 + ", " + use2);
        }
    }
}

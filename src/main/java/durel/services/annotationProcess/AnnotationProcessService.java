package durel.services.annotationProcess;

import durel.dto.responses.UseDTO;
import durel.services.user.UserService;
import durel.session.AnnotationSessionData;
import durel.session.RandomAnnotationSequence;
import durel.domain.model.*;
import durel.exceptions.SystemErrorException;
import durel.exceptions.UserErrorException;
import durel.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;

@Service
public class AnnotationProcessService implements DefaultAnnotationProcessService {

    /**
     * AnnotationProcessService logger.
     *
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(AnnotationProcessService.class);

    // Services
    private final AnnotationService annotationService;

    private final UseService useService;

    private final PairService pairService;

    private final WordService wordService;

    private final UserService userService;

    private final SequenceService sequenceService;

    // SessionData
    private final AnnotationSessionData annotationSessionData;

    @Autowired
    public AnnotationProcessService(AnnotationService annotationService, UseService useService,
                                    PairService pairService, WordService wordService, UserService userService,
                                    SequenceService sequenceService, AnnotationSessionData annotationSessionData) {
        this.annotationService = annotationService;
        this.useService = useService;
        this.pairService = pairService;
        this.wordService = wordService;
        this.userService = userService;
        this.sequenceService = sequenceService;
        this.annotationSessionData = annotationSessionData;
    }

    // Managing the SessionData ---------------------------------------------------------------------------------------


    /**
     * Returns the current sequence index, i.e., how far the annotation has progressed.
     *
     * @return The index of the current sequence. If the current sequence is null, returns 0. Otherwise, returns the index of the current sequence.
     */
    @Override
    public int getCurrentSequenceIndex() {
        if (annotationSessionData.getCurrentAnnotationSequence() == null) {
            return 0;
        }
        return annotationSessionData.getCurrentAnnotationSequence().getIndex();
    }

    /**
     * Calculate the number of sentence pairs based on the current project and word. If the current project in the
     * annotationSessionData is random, this is n*(n-1)/2 (n being the number of sentences). If the current project is not
     * random, we count the number of pairs in the pairs table.
     *
     * @return The number of sentence pairs.
     */
    @Override
    public int noOfSentencePairs() {
        if (annotationSessionData.getCurrentProject().isAllPossiblePairs()) {
            int noSentencesOfWord = useService.getNoOfSentencesOfWord(annotationSessionData.getCurrentLemma());
            return (noSentencesOfWord*(noSentencesOfWord-1))/2;
        }
        else {
            return pairService.getPairsByProjectAndLemma(annotationSessionData.getCurrentProject(), annotationSessionData.getCurrentLemma().getLemma()).size();
        }
    }

    // Business logic for human annotation tasks -----------------------------------------------------------------------

    /**
     * Starts/continues the annotations of a word coming from the annotation startpage
     * and sets the session data accordingly. If the word is annotated for the first time,
     * a new annotation sequence is created.
     *
     * @param username       The username of the annotator.
     * @param projectSelect  The name of the project.
     * @param wordSelect     The word to be annotated.
     */
    public void startAnnotation(String username, String projectSelect, String wordSelect) {
        // Note that a word can be uniquely identified by its id (PK), or by the pair projectName and Word. Check the database details to learn more.
        Lemma lemma = wordService.getLemmaObjectByProjectNameAndLemma(projectSelect, wordSelect);
        User annotator = userService.getUserByUsername(username);
        AnnotationSequence annotationSequence = sequenceService.getExistingOrNewSeq(annotator, lemma);
        setSessionData(lemma, annotator, annotationSequence);
    }

    /**
     * Sets the session data for the annotation process.
     *
     * @param lemma     The Word object representing the word to be annotated.
     * @param annotator The Annotator object representing the annotator.
     * @param annotationSequence      The Seq object representing the sequence.
     */
    private void setSessionData(Lemma lemma, User annotator, AnnotationSequence annotationSequence) {
        setSessionAnnotationSequence(lemma, annotationSequence);
        annotationSessionData.setCurrentAnnotator(annotator);
        annotationSessionData.setCurrentProject(lemma.getProject());
        annotationSessionData.setCurrentLemma(lemma);
        annotationSessionData.setCurrentAnnotationSequence(annotationSequence);
    }

    /**
     * Sets the session annotation sequence for a given word.
     * If the project is random, it creates a new random annotation sequence based on the list of sentence IDs of the word.
     * If the project is not random, it creates a new random annotation sequence based on the pairs of sentences in the project.
     *
     * @param lemma The Word object representing the word to be annotated.
     */
    private void setSessionAnnotationSequence(Lemma lemma, AnnotationSequence annotationSequence) {
        if (lemma.getProject().isAllPossiblePairs()) {
            List<Integer> sentenceIDsOfWord = useService.getSentenceIDsOfWord(lemma);
            annotationSessionData.setRandomAnnotationSequence(new RandomAnnotationSequence(sentenceIDsOfWord, annotationSequence.getSeed()));
        } else {
            List<Instance> instances = pairService.getPairsByProjectAndLemma(lemma.getProject(), lemma.getLemma());
            annotationSessionData.setRandomAnnotationSequence(new RandomAnnotationSequence(annotationSequence.getSeed(), instances));
        }
    }

    @Override
    public List<UseDTO> getNextUsePair() {
        // Retrieve the session data.
        AnnotationSequence annotationSequence = annotationSessionData.getCurrentAnnotationSequence();

        // If we have annotated all pairs of sentences, then we send a fail status to the controller.
        if (isAllPairsAnnotated(annotationSequence)) {
            return new ArrayList<>();
        }
        // If not, we retrieve the next pair of sentences from the database.
        List<UseDTO> nextSentences = getNextVSentences(annotationSequence);
        updateSessionData(nextSentences);
        return nextSentences;
    }

    /**
     * Checks whether all pairs of sentences have been annotated.
     *
     * @param annotationSequence The current sequence of annotations.
     * @return true if all pairs of sentences have been annotated, false otherwise.
     */
    private boolean isAllPairsAnnotated(@NotNull AnnotationSequence annotationSequence) {
        RandomAnnotationSequence randomAnnotationSequence = annotationSessionData.getRandomAnnotationSequence();
        // Check whether we have annotated all pairs of sentences
        return annotationSequence.getIndex() >= randomAnnotationSequence.getAnnotations().size();
    }

    /**
     * Retrieves the next pair of VSentences based on the given Seq.
     *
     * @param annotationSequence The Seq object used to determine the next pair of sentences.
     * @return The List of VSentence objects representing the next pair of sentences.
     */
    private List<UseDTO> getNextVSentences(@NotNull AnnotationSequence annotationSequence) {
        // Create VSentences from the next pair of sentences
        RandomAnnotationSequence randomAnnotationSequence = annotationSessionData.getRandomAnnotationSequence();
        Integer[] annotationIds = shuffleIds(randomAnnotationSequence.next(annotationSequence.getIndex()));
        UseDTO leftUseDTO = getVSentence(annotationIds[0]);
        UseDTO rightUseDTO = getVSentence(annotationIds[1]);
        return Arrays.asList(leftUseDTO, rightUseDTO);
    }

    /**
     * Retrieves a VSentence object based on the provided annotation ID.
     *
     * @param annotationId The ID of the annotation.
     * @return The VSentence object.
     */
    private UseDTO getVSentence(@NotNull Integer annotationId) {
        Use use = useService.getSentence(annotationId);
        return useService.getSentenceDTO(use);
    }

    /**
     * Shuffle the array of annotation IDs randomly.
     *
     * @param annotationIds The array of annotation IDs to be shuffled.
     * @return The shuffled array of annotation IDs.
     */
    private Integer[] shuffleIds(@NotNull Integer[] annotationIds) {
        Collections.shuffle(Arrays.asList(annotationIds));
        return annotationIds;
    }

    /**
     * Updates the session data with the new sentences' IDs.
     *
     * @param sentences The list of sentences to update the session data with.
     * @throws DataIntegrityViolationException If the length of the list of sentences is not equal to 2.
     */
    private void updateSessionData(List<UseDTO>sentences) throws DataIntegrityViolationException{
        if (sentences != null && sentences.size() == 2) {
            // Update session data with the new sentences' IDs
            annotationSessionData.setCurrentFirstSentenceID(sentences.get(0).getId());
            annotationSessionData.setCurrentSecondSentenceID(sentences.get(1).getId());
        } else {
            DataIntegrityViolationException e = new DataIntegrityViolationException("Unexpected length of list of next sentences. Should be 2.");
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Saves the judgment and comment for the current annotation.
     *
     * @param judgment The judgment value to be saved.
     * @param comment  The comment to be saved.
     * @throws SystemErrorException If there is an error in the system.
     * @throws UserErrorException   If there is an error with the user input.
     */
    @Override
    @Transactional
    @Modifying
    public void saveJudgment(float judgment, String comment) throws SystemErrorException, UserErrorException {
        User annotator = annotationSessionData.getCurrentAnnotator();
        Lemma lemma = annotationSessionData.getCurrentLemma();
        AnnotationSequence annotationSequence = annotationSessionData.getCurrentAnnotationSequence();
        int firstSentenceID = annotationSessionData.getCurrentFirstSentenceID();
        int secondSentenceID = annotationSessionData.getCurrentSecondSentenceID();
        annotationService.saveSingleAnnotationAndUpdateSequence(lemma, annotator, annotationSequence, judgment, firstSentenceID, secondSentenceID, comment);
    }
}

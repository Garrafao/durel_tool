package durel.services;

import durel.domain.AnnotationFilterCriteria;
import durel.domain.model.*;
import durel.domain.repository.SequenceDAO;
import durel.services.annotation.AnnotationQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SequenceService {

    private final SequenceDAO sequenceDAO;

    private final FilterDataService filterDataService;

    private final AnnotationQueryService annotationQueryService;

    @Autowired
    public SequenceService(SequenceDAO sequenceDAO, FilterDataService filterDataService, AnnotationQueryService annotationQueryService) {
        this.sequenceDAO = sequenceDAO;
        this.filterDataService = filterDataService;
        this.annotationQueryService = annotationQueryService;
    }

    /**
     * Fetches the annotation sequence of a user for a given word.
     */
    @Transactional
    public AnnotationSequence getSeq(User user, Lemma lemma) {
        Optional<AnnotationSequence> seq = sequenceDAO.findByLemmaAndUser(lemma, user);
        return seq.orElse(null);
    }

    @Transactional
    public AnnotationSequence getExistingOrNewSeq(User user, Lemma lemma) {
        // Try to retrieve the sequence of the user.
        AnnotationSequence annotationSequence = getSeq(user, lemma);
        // If seq == null we are starting a new annotation.
        if(annotationSequence == null) {
            annotationSequence = createNewSeq(user, lemma, 0);
        }
        return annotationSequence;
    }

    @Transactional
    public Map<User, Map<Lemma, AnnotationSequence>> getSequencesOfProject(Project project) {
        List<AnnotationSequence> annotationSequenceList = sequenceDAO.findAllByLemma_Project(project);
        return annotationSequenceList.stream().collect(Collectors.groupingBy(AnnotationSequence::getUser, Collectors.toMap(AnnotationSequence::getLemma, Function.identity())));
    }


    /**
     * Creates a new sequence for a new user-word pair.
     */
    @Transactional
    public AnnotationSequence createNewSeq(User user, Lemma lemma, int idx) {
        // Create a new seed for the sequence. This will generate a random annotation sequence for the user.
        long seed = ThreadLocalRandom.current().nextLong(0, 9223372036854775806L);
        // Set up the sequence object and save it to the database.
        AnnotationSequence annotationSequence = new AnnotationSequence(new AnnotationSequenceId(lemma.getId(), user.getUsername()), user, lemma, idx, seed);
        sequenceDAO.save(annotationSequence);
        return annotationSequence;
    }

    @Transactional
    public void updateSequence(AnnotationSequence annotationSequence) {
        updateSequenceIndex(annotationSequence);
        saveSeq(annotationSequence);
    }

    private void updateSequences(Collection<AnnotationSequence> annotationSequences) {
        for (AnnotationSequence annotationSequence : annotationSequences) {
            updateSequenceIndex(annotationSequence);
        }
    }

    /**
     * Updates the sequences of a project and saves the changes.
     *
     * @param sequencesOfProject  A map of users to word sequences.
     */
    public void updateSequencesAndSave(@NotNull Map<User, Map<Lemma, AnnotationSequence>> sequencesOfProject) {
        for (User user : sequencesOfProject.keySet()) {
            updateSequences(sequencesOfProject.get(user).values());
            saveSeqs(sequencesOfProject.get(user).values());
        }
    }

    /**
     * Creates a missing sequence in the given map of sequences.
     * If the user and word do not exist in the map, they will be added with an empty sequence.
     *
     * @param sequences The map of sequences to be updated.
     * @param user The user associated with the missing sequence.
     * @param lemma      The word associated with the missing sequence.
     */
    @Transactional
    public void createMissingSequence(@NotNull Map<User, Map<Lemma, AnnotationSequence>> sequences, @NotNull User user, @NotNull Lemma lemma) {
        sequences.computeIfAbsent(user, k -> new HashMap<>());
        sequences.get(user)
                .computeIfAbsent(lemma, k -> createNewSeq(user, lemma, 0)); // We only put 0 here because we will update later
    }

    @Transactional
    public void saveSeq(AnnotationSequence annotationSequence) {
        sequenceDAO.save(annotationSequence);
    }

    private void saveSeqs(Collection<AnnotationSequence> annotationSequences) {
        sequenceDAO.saveAll(annotationSequences);
    }

    private void updateSequenceIndex(AnnotationSequence annotationSequence) {
        AnnotationFilterCriteria annotationFilterCriteria = filterDataService.createAnnotationFilterData(annotationSequence.getLemma(), null, null, null, annotationSequence.getUser(), null, null, null);
        annotationSequence.setIndex(annotationQueryService.getAnnotationCountWithFilterOptions(annotationFilterCriteria));
    }
}

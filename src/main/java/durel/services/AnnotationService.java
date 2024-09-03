package durel.services;

import durel.domain.AnnotationFilterCriteria;
import durel.domain.model.annotation.UserAnnotation;
import durel.services.dataManagement.uploadData.AnnotationData;
import durel.services.dataManagement.uploadData.PairedUploadData;
import durel.domain.model.*;
import durel.domain.repository.UserAnnotationDAO;
import durel.exceptions.DatabaseAccessException;
import durel.exceptions.LongVarcharException;
import durel.exceptions.SystemErrorException;
import durel.exceptions.UserErrorException;
import durel.services.annotation.AnnotationQueryService;
import durel.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.management.InstanceNotFoundException;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AnnotationService is responsible for saving and updating annotations in the system.
 */
@Service
public class AnnotationService {

    /**
     * AnnotationService logger.
     *
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(AnnotationService.class);

    /**
     * Default judgment value for annotations.
     */
    private static final float FINAL_DEFAULT_JUDGMENT_VALUE = -1;
    /**
     * "Please contact the admins with the following information: "
     */
    private static final String ERROR_MESSAGE = "Please contact the admins with the following information: ";
    /**
     * "Please redo the annotation: "
     */
    private static final String USER_INFO_MESSAGE = "Please redo the annotation: ";
    /**
     * "Please check the annotation: "
     */
    private static final String USER_UPLOAD_MESSAGE = "Please check the annotation: ";

    // DAO
    private final UserAnnotationDAO userAnnotationDAO;

    // Services
    private final UseService useService;

    private final AnnotationQueryService annotationQueryService;

    private final UserService userService;

    private final FilterDataService filterDataService;

    private final SequenceService sequenceService;

    @Autowired
    public AnnotationService(UserAnnotationDAO userAnnotationDAO, UseService useService, FilterDataService filterDataService, SequenceService sequenceService, AnnotationQueryService annotationQueryService, UserService userService) {
        this.userAnnotationDAO = userAnnotationDAO;
        this.useService = useService;
        this.filterDataService = filterDataService;
        this.sequenceService = sequenceService;
        this.annotationQueryService = annotationQueryService;
        this.userService = userService;
    }

    // Access point single annotation upload -----------------------------------------------------------------------

    /**
     * Saves a single annotation and updates the sequence.
     *
     * @param lemma        The Word object associated with the annotation.
     * @param annotator   The Annotator object creating the annotation.
     * @param annotationSequence         The Seq object to be updated.
     * @param judgment    The judgment value for the annotation.
     * @param sentence1_id The ID of the first sentence associated with the annotation.
     * @param sentence2_id The ID of the second sentence associated with the annotation.
     * @param comment     The comment for the annotation.
     * @throws SystemErrorException if an unexpected error occurs while saving the annotation and updating the sequence.
     * @throws UserErrorException   if there is an error with the user input.
     */
    @Transactional
    public void saveSingleAnnotationAndUpdateSequence(@NotNull Lemma lemma, @NotNull User annotator, @NotNull AnnotationSequence annotationSequence, float judgment, int sentence1_id,
                                                      int sentence2_id, String comment)
            throws SystemErrorException, UserErrorException {
        try {
            Use use1 = useService.getSentence(sentence1_id);
            Use use2 = useService.getSentence(sentence2_id);
            useService.checkSentencePairing(lemma, use1, use2);
            UserAnnotation userAnnotation = createOrModifyAnnotation(annotator, judgment, use1, use2, comment, null);
            userAnnotationDAO.save(userAnnotation);
            sequenceService.updateSequence(annotationSequence);
        } catch (UnexpectedRollbackException e) {
            logger.error("An unexpected error occurred while trying to save the annotation and update the sequence.", e);
            throw new SystemErrorException(ERROR_MESSAGE + "UnexpectedRollbackException " + LocalDateTime.now());
        } catch (IllegalArgumentException | InstanceNotFoundException e) {
            logger.error("{}: {}", annotator.getUsername(), e.getMessage());
            throw new SystemErrorException(ERROR_MESSAGE + "Data Integrity Violation " + LocalDateTime.now());
        } catch (LongVarcharException e) {
            throw new UserErrorException(USER_INFO_MESSAGE + e.getMessage());
        }
    }

    // Access point multi annotation upload ------------------------------------------------------------------------------

    /**
     * Saves a list of annotations and updates all sequences.
     *
     * @param pairedDataList   The list of PairedUploadData representing the annotations to be saved.
     * @param idToSentence     The mapping of IDs to Sentence objects.
     * @param project          The Project object associated with the annotations.
     * @throws SystemErrorException  If an unexpected error occurs while saving the annotations and updating the sequences.
     * @throws UserErrorException    If there is an error with the user input.
     */
    @Transactional
    public void saveListOfAnnotationsAndUpdateAllSequences(@NotNull List<PairedUploadData> pairedDataList,
                                                           @NotNull ConcurrentHashMap<String, Use> idToSentence,
                                                           @NotNull Project project) throws SystemErrorException, UserErrorException {
        try {
            Map<User, Map<Lemma, AnnotationSequence>> sequencesOfProject = sequenceService.getSequencesOfProject(project);
            AnnotationFilterCriteria annotationFilterCriteria = filterDataService.createAnnotationFilterData(null, project, null, null, (User) null, null, null, null);
            ConcurrentHashMap<UsePairAndAnnotator, UserAnnotation> existingAnnotations =
                    new ConcurrentHashMap<>(annotationQueryService.getAnnotationsWithFilterOptions(annotationFilterCriteria)
                            .stream().collect(Collectors.toMap(UserAnnotation::getId, Function.identity())));
            Set<UserAnnotation> newUserAnnotations = mapPairedDataToAnnotations(pairedDataList, idToSentence, sequencesOfProject, existingAnnotations);
            userAnnotationDAO.saveAll(newUserAnnotations);
            sequenceService.updateSequencesAndSave(sequencesOfProject);
        }  catch (UnexpectedRollbackException e) {
            logger.error("An unexpected error occurred while trying to save the annotation and update the sequence.", e);
            throw new SystemErrorException(ERROR_MESSAGE + "UnexpectedRollbackException " + LocalDateTime.now());
        }
    }

    /**
     * Maps paired upload data to annotations.
     *
     * @param pairedDataList         The list of paired upload data.
     * @param idToSentence           The mapping of IDs to sentences.
     * @param sequencesOfProject     The mapping of annotators to word sequences.
     * @param existingAnnotations    The existing annotations.
     * @return The set of mapped annotations.
     * @throws UserErrorException    If there is an error with the user input.
     */
    private Set<UserAnnotation> mapPairedDataToAnnotations(List<PairedUploadData> pairedDataList, ConcurrentHashMap<String, Use> idToSentence,
                                                           Map<User, Map<Lemma, AnnotationSequence>> sequencesOfProject,
                                                           ConcurrentHashMap<UsePairAndAnnotator, UserAnnotation> existingAnnotations)
            throws UserErrorException {
        List<UserErrorException> errors = new ArrayList<>();
        Set<UserAnnotation> userAnnotations = pairedDataList.stream()
                .filter(PairedUploadData::isInstanceOfAnnotationData)
                .map(annotationData -> {
                    try {
                        return createAnnotation((AnnotationData) annotationData, idToSentence, sequencesOfProject, existingAnnotations);
                    } catch (IllegalArgumentException | InstanceNotFoundException | LongVarcharException e) {
                        // Erroneous mappings are filtered out, and we add an error to the error list.
                        errors.add(new UserErrorException(USER_UPLOAD_MESSAGE + e.getMessage(), e));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!errors.isEmpty()) {
            String errorMessage = errors.stream()
                    .map(UserErrorException::getMessage)
                    .collect(Collectors.joining("\n"));
            throw new UserErrorException(errorMessage);
        }

        return userAnnotations;
    }

    /**
     * Creates an Annotation object based on the provided AnnotationData, sentence mappings, sequences, and annotations in the database.
     *
     * @param annotationData          The AnnotationData object containing the data for creating the annotation.
     * @param idToSentence            The mapping of IDs to sentences.
     * @param sequences               The mapping of annotators to word sequences.
     * @param annotationsInDatabase   The annotations already present in the database.
     * @return The created Annotation object.
     * @throws IllegalArgumentException     If any of the input parameters are invalid.
     * @throws InstanceNotFoundException     If the sentences cannot be found in the sentence map.
     * @throws LongVarcharException           If the comment exceeds the maximum length of 255 characters.
     */
    private UserAnnotation createAnnotation(@NotNull AnnotationData annotationData, @NotNull ConcurrentHashMap<String, Use> idToSentence,
                                            Map<User, Map<Lemma, AnnotationSequence>> sequences, ConcurrentHashMap<UsePairAndAnnotator, UserAnnotation> annotationsInDatabase)
            throws IllegalArgumentException, InstanceNotFoundException, LongVarcharException {
        Use use1 = idToSentence.get(annotationData.getIdentifierOne());
        Use use2 = idToSentence.get(annotationData.getIdentifierTwo());
        useService.checkSentencePairing(null, use1, use2);
        User annotator = userService.getUserByUsername(annotationData.getAnnotator());
        sequenceService.createMissingSequence(sequences, annotator, use1.getLemma());
        return createOrModifyAnnotation(annotator, annotationData.getJudgment(), use1, use2, annotationData.getComment(), annotationsInDatabase);
    }

    // Create or modify an annotation ---------------------------------------------------------------------------

    /**
     * Creates a new annotation if it does not exist, or modifies an existing annotation if it does, based on the provided parameters.
     *
     * @param annotator            The Annotator object responsible for creating or modifying the annotation.
     * @param judgment             The judgment value for the annotation.
     * @param firstUse        The first Sentence object associated with the annotation.
     * @param secondUse       The second Sentence object associated with the annotation.
     * @param comment              The comment for the annotation.
     * @param annotationsInDatabase The existing annotations in the database.
     * @return The created or modified Annotation object.
     * @throws LongVarcharException If the comment exceeds the maximum length of 255 characters.
     */
    private UserAnnotation createOrModifyAnnotation(User annotator, float judgment, Use firstUse,
                                                    Use secondUse, String comment,
                                                    ConcurrentHashMap<UsePairAndAnnotator, UserAnnotation> annotationsInDatabase)
            throws LongVarcharException{
        UserAnnotation userAnnotation = findExistingAnnotation(annotator, firstUse, secondUse, annotationsInDatabase);
        if (comment.length() > 255) {
            throw new LongVarcharException("Comments must not be longer than 255 characters.");
        }
        if (userAnnotation == null) {
            userAnnotation = createNewAnnotation(annotator, judgment, firstUse, secondUse, comment);
        } else if (judgment != FINAL_DEFAULT_JUDGMENT_VALUE) {
            modifyExistingAnnotation(userAnnotation, judgment, comment);
        }
        return userAnnotation;
    }

    /**
     * Finds an existing annotation in the database based on the provided parameters.
     *
     * @param annotator The annotator for the annotation.
     * @param firstUse The first sentence of the annotation.
     * @param secondUse The second sentence of the annotation.
     * @param annotationsInDatabase The existing annotations in the database.
     * @return The found annotation or null if not found.
     */
    private UserAnnotation findExistingAnnotation (User annotator, Use firstUse, Use secondUse,
                                                   ConcurrentHashMap<UsePairAndAnnotator, UserAnnotation> annotationsInDatabase) {
        try {
            return annotationQueryService.
                    findAnnotationByIDs(annotator, firstUse, secondUse, annotationsInDatabase).orElse(null);
        } catch (DatabaseAccessException e) {
            logger.error("An unexpected error occurred while trying to find existing annotations.", e);
            return null;
        }
    }

    /**
     * Creates a new annotation with the provided parameters.
     *
     * @param annotator        The Annotator object creating the annotation.
     * @param judgment         The judgment value of the annotation.
     * @param firstUse    The first Sentence object associated with the annotation.
     * @param secondUse   The second Sentence object associated with the annotation.
     * @param comment          The comment for the annotation.
     * @return The newly created Annotation object.
     */
    private UserAnnotation createNewAnnotation(User annotator, float judgment, Use firstUse,
                                               Use secondUse, String comment) {
        UserAnnotation newUserAnnotation = new UserAnnotation(new UsePairAndAnnotator(annotator, firstUse,
                secondUse), judgment);
        newUserAnnotation.setComment(comment);
        return newUserAnnotation;
    }

    /**
     * Modifies an existing annotation by updating its judgment, date, and comment.
     *
     * @param userAnnotation The annotation to modify.
     * @param judgment   The new judgment value for the annotation.
     * @param comment    The new comment for the annotation.
     */
    private void modifyExistingAnnotation(UserAnnotation userAnnotation, float judgment, String comment) {
        userAnnotation.setJudgment(judgment);
        userAnnotation.setDt(new Date());
        userAnnotation.setComment(comment);
    }
}

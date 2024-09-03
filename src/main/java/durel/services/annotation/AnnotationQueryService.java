package durel.services.annotation;

import durel.domain.AnnotationFilterCriteria;
import durel.domain.model.*;
import durel.domain.model.annotation.UserAnnotation;
import durel.dto.responses.AnnotationDTO;
import durel.services.dtoServices.AnnotationDTOService;
import durel.session.DataSessionData;
import durel.dto.responses.statistics.JudgmentCount;
import durel.dto.responses.statistics.StringCount;
import durel.domain.repository.UserAnnotationDAO;
import durel.exceptions.DatabaseAccessException;
import durel.services.FilterDataService;
import durel.services.PredicateBuilderService;
import durel.utils.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnnotationQueryService {

    /**
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(AnnotationQueryService.class);


    /* Note: This works because the word of the left sentence and the word of thr right sentence are identical.
        Therefore, we only have to search for the left side. */
    private static final String[] WORD_ID_PATH = {"id", "pair", "use1", "lemma", "id"};
    private static final String[] PROJECT_NAME_PATH = {"id", "pair", "use1", "lemma", "project", "projectName"};
    private static final String[] ANNOTATOR_PATH = {"id", "annotator", "username"};
    private static final String[] JUDGMENT_PATH = {"judgment"};
    private static final String[] LEFT_GROUPINGS_PATH = {"id", "pair", "use1", "groupings"};
    private static final String[] RIGHT_GROUPINGS_PATH = {"id", "pair", "use2", "groupings"};
    private static final String[] LEFT_POS_PATH = {"id", "pair", "use1", "pos"};
    private static final String[] RIGHT_POS_PATH = {"id", "pair", "use2", "pos"};

    public static final List<Float> WHOLE_NUMBER_JUDGMENT_VALUES = List.of(0.0F, 1.0F, 2.0F, 3.0F, 4.0F);

    @PersistenceContext
    private EntityManager entityManager;

    private final UserAnnotationDAO userAnnotationDAO;

    private final AnnotationDTOService annotationDTOService;

    private final FilterDataService filterDataService;

    private final PredicateBuilderService predicateBuilderService;

    private final DataSessionData dataSessionData;

    @Autowired
    public AnnotationQueryService(UserAnnotationDAO userAnnotationDAO, AnnotationDTOService annotationDTOService, FilterDataService filterDataService,
                                  PredicateBuilderService predicateBuilderService, DataSessionData dataSessionData) {
        this.userAnnotationDAO = userAnnotationDAO;
        this.annotationDTOService = annotationDTOService;
        this.filterDataService = filterDataService;
        this.predicateBuilderService = predicateBuilderService;
        this.dataSessionData = dataSessionData;
    }


    /**
     * Finds an annotation based on both possible IDs derived from the username and sentenceIDs.
     * First tries to use the annotationsInDatabase object to avoid calls to the database.
     * Performs a call to the database if unsuccessful.
     *
     * @param username             The username associated with the annotation.
     * @param firstSentenceID      The ID of the first sentence.
     * @param secondSentenceID     The ID of the second sentence.
     * @param annotationsInDatabase A map of annotation IDs to annotations stored in the database. Can be null.
     * @return The annotation found, or null if not found.
     */
    @Transactional
    public Optional<UserAnnotation> findAnnotationByIDs(@NotNull User username, @NotNull Use firstSentenceID, @NotNull Use secondSentenceID,
                                                        ConcurrentHashMap<UsePairAndAnnotator, UserAnnotation> annotationsInDatabase) throws DatabaseAccessException {
        UsePairAndAnnotator usePairAndAnnotator1 = new UsePairAndAnnotator(username, firstSentenceID, secondSentenceID);
        UsePairAndAnnotator usePairAndAnnotator2 = new UsePairAndAnnotator(username, secondSentenceID, firstSentenceID);
        UserAnnotation userAnnotation = null;
        if (annotationsInDatabase != null) {
            // Try to retrieve annotation from map
            userAnnotation = annotationsInDatabase.get(usePairAndAnnotator1);
            if (userAnnotation == null) {
                userAnnotation = annotationsInDatabase.get(usePairAndAnnotator2);
            }
        }
        // Only if not the annotation is not found in the map, retrieve annotation from database.
        // This is done to reduce the number of database accesses.
        if (userAnnotation == null) {
            try {
                Optional<UserAnnotation> optionalAnnotation = userAnnotationDAO.findByIdOrId(usePairAndAnnotator1, usePairAndAnnotator2);
                if (optionalAnnotation.isPresent()) {
                    userAnnotation = optionalAnnotation.get();
                    if (annotationsInDatabase != null) {
                        annotationsInDatabase.putIfAbsent(usePairAndAnnotator1, userAnnotation);
                    }
                }
            } catch (DataAccessException e) {
                logger.error("Unable to retrieve annotation from database.", e);
                throw new DatabaseAccessException("Unable to retrieve annotation from database.", e);
            }
        }
        return Optional.ofNullable(userAnnotation);
    }

    /**
     * For a given annotationFilterData, this counts: a) the total number of annotations and b) the number of
     * annotations with judgment 0,1,2,3,4, respectively.
     *
     * @param annotationFilterCriteria The annotationFilterData for which the counts should be retrieved.
     * @return A list with the retrieved counts [total, 0, 1, 2, 3, 4]
     */
    @Transactional
    public List<Integer> getAnnotationCounts(AnnotationFilterCriteria annotationFilterCriteria) {
        Map<Float, Integer> judgmentCounts = getAnnotationCountByJudgmentWithFilterOptions(annotationFilterCriteria);
        List<Integer> noOfAnnotations = new ArrayList<>();
        int total = 0;

        for (float judgment : WHOLE_NUMBER_JUDGMENT_VALUES) {
            int count;
            if (judgmentCounts.get(judgment) != null) count = judgmentCounts.get(judgment);
            else count = 0;
            total += count;
            noOfAnnotations.add(count);
        }

        noOfAnnotations.add(0, total); // add the total count at the beginning of the list
        return noOfAnnotations;
    }

    /**
     * Returns a set of annotations based on the provided filter options.
     *
     * @param annotationFilterCriteria The filter data used to retrieve the annotations.
     * @return A set of annotations matching the filter options.
     * @throws IllegalStateException If the entity manager is not initialized.
     */
    @Transactional
    public Set<UserAnnotation> getAnnotationsWithFilterOptions(AnnotationFilterCriteria annotationFilterCriteria) throws IllegalStateException {

        List<UserAnnotation> userAnnotations = createQueryWithFilterOptions(UserAnnotation.class, (root, cb, query) -> query.orderBy(cb.asc(root.get("id"))), annotationFilterCriteria);
        return new HashSet<>(userAnnotations);
    }

    @Transactional
    public int getAnnotationCountWithFilterOptions(AnnotationFilterCriteria annotationFilterCriteria) throws IllegalStateException {

        List<Long> counts = createQueryWithFilterOptions(Long.class, (root, cb, query) -> query.select(cb.count(root)), annotationFilterCriteria);
        return counts.isEmpty() ? 0 : Math.toIntExact(counts.get(0));
    }

    private Map<Float, Integer> getAnnotationCountByJudgmentWithFilterOptions(AnnotationFilterCriteria annotationFilterCriteria) throws IllegalStateException {

        List<JudgmentCount> judgmentCounts = createQueryWithFilterOptions(JudgmentCount.class, (root, cb, query) -> query.multiselect(root.get("judgment"), cb.count(root)).groupBy(root.get("judgment"), root.get("annotator").get("username")), annotationFilterCriteria);
        Map<Float, Integer> judgmentMap = new HashMap<>();
        for (JudgmentCount judgmentCount : judgmentCounts) {
            judgmentMap.put(judgmentCount.getJudgment(), Math.toIntExact(judgmentCount.getCount()));
        }
        return judgmentMap;
    }

    @Transactional
    public Map<String, Integer> getAnnotationCountByAnnotatorWithFilterOptions(AnnotationFilterCriteria annotationFilterCriteria) throws IllegalStateException {

        List<StringCount> stringCounts = createQueryWithFilterOptions(StringCount.class, (root, cb, query) -> query.multiselect(root.get("annotator").get("username"), cb.count(root)).groupBy(root.get("annotator").get("username")), annotationFilterCriteria);
        Map<String, Integer> stringMap = new HashMap<>();
        for (StringCount stringCount : stringCounts) {
            stringMap.put(stringCount.getIdentifier(), Math.toIntExact(stringCount.getCount()));
        }
        return stringMap;
    }

    /**
     * Executes a query to the AnnotationDAO with filter options. This can be a count query but also a find query.
     *
     * @param <T>                  The result type.
     * @param resultType           The class of the result type.
     * @param selectFunction       The function that applies additional criteria to the query.
     * @param annotationFilterCriteria The filter data used to retrieve the annotations.
     * @return A list of objects of type T matching the filter options.
     * @throws IllegalStateException If the entity manager is not initialized.
     */
    private <T> List<T> createQueryWithFilterOptions(Class<T> resultType,
                                                     TriFunction<Root<UserAnnotation>, CriteriaBuilder, CriteriaQuery<T>, CriteriaQuery<T>> selectFunction,
                                                     AnnotationFilterCriteria annotationFilterCriteria) {

        if (entityManager == null) {
            throw new IllegalStateException("Entity manager is not initialized");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(resultType);
        Root<UserAnnotation> annotationRoot = query.from(UserAnnotation.class);
        List<Predicate> predicates = buildAnnotationPredicates(annotationRoot, cb, annotationFilterCriteria);

        selectFunction.apply(annotationRoot, cb, query);
        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Builds a list of predicates based on the annotation filter data.
     *
     * @param annotationRoot        The root of the annotation entity.
     * @param cb                    The criteria builder.
     * @param annotationFilterCriteria  The filter data used to build the predicates.
     * @return The list of predicates.
     */
    private List<Predicate> buildAnnotationPredicates(Root<UserAnnotation> annotationRoot, CriteriaBuilder cb,
                                                      AnnotationFilterCriteria annotationFilterCriteria) {
        List<Predicate> predicates = new ArrayList<>();

        predicateBuilderService.checkAndAddInPredicate(predicates, annotationRoot, cb, WORD_ID_PATH, annotationFilterCriteria.getWords());
        predicateBuilderService.checkAndAddInPredicate(predicates, annotationRoot, cb, PROJECT_NAME_PATH, annotationFilterCriteria.getProjects());
        predicateBuilderService.checkAndAddSentenceDatePredicate(predicates, annotationRoot, cb, annotationFilterCriteria.getSentenceDate1(), annotationFilterCriteria.getSentenceDate2());
        predicateBuilderService.checkAndAddInPredicate(predicates, annotationRoot, cb, LEFT_GROUPINGS_PATH, annotationFilterCriteria.getGroupings());
        predicateBuilderService.checkAndAddInPredicate(predicates, annotationRoot, cb, RIGHT_GROUPINGS_PATH, annotationFilterCriteria.getGroupings());
        predicateBuilderService.checkAndAddInPredicate(predicates, annotationRoot, cb, ANNOTATOR_PATH, annotationFilterCriteria.getAnnotators());
        predicateBuilderService.checkAndAddInPredicate(predicates, annotationRoot, cb, LEFT_POS_PATH, annotationFilterCriteria.getPos());
        predicateBuilderService.checkAndAddInPredicate(predicates, annotationRoot, cb, RIGHT_POS_PATH, annotationFilterCriteria.getPos());
        predicateBuilderService.checkAndAddInPredicate(predicates, annotationRoot, cb, JUDGMENT_PATH, annotationFilterCriteria.getJudgments());
        return predicates;
    }

    @Transactional
    public void updateAnnotationViewDataList(Lemma lemma, String username) {
        AnnotationFilterCriteria annotationFilterCriteria = filterDataService.createAnnotationFilterData(lemma,null,null, null,(User)null,null,null,null);
        Set<UserAnnotation> userAnnotations = getAnnotationsWithFilterOptions(annotationFilterCriteria);
        ArrayList<AnnotationDTO> annotationsArrayList = new ArrayList<>();
        for (UserAnnotation userAnnotation : userAnnotations) {
            // Ensure that annotations can only be seen by project owner, or the annotator himself.
            if (userAnnotation.getAnnotator().getUsername().equals(username) ||
                    userAnnotation.getUses().stream().toList().get(0).getLemma().getProject().getCreator().getUsername().equals(username)) {
                annotationsArrayList.add(annotationDTOService.constructAnnotationDTOfromAnnotation(userAnnotation));
            }
        }
        this.dataSessionData.setAnnotationViewData(annotationsArrayList);
    }

    public ArrayList<AnnotationDTO> getCurrentAnnotations() {
        return dataSessionData.getAnnotationViewData();
    }
}

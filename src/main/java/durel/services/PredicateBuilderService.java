package durel.services;

import durel.domain.model.BaseUse;
import durel.domain.model.Use;
import durel.domain.model.annotation.UserAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;

@Service
public class PredicateBuilderService {

    /**
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(PredicateBuilderService.class);

    public Path<Object> findPredicatePath(CriteriaBuilder cb, Root<?> root, String[] pathParameters) {
        Path<Object> path = root.get(pathParameters[0]);
        for (int i = 1; i < pathParameters.length; i++) {
            if (path.getJavaType().equals(BaseUse.class)) {
                path = cb.treat(path, Use.class).get(pathParameters[i]);
            } else {
                path = path.get(pathParameters[i]);
            }
        }
        return path;
    }

    // Annotation Predicates

    public void checkAndAddInPredicate(List<Predicate> predicates, Root<UserAnnotation> annotationRoot, CriteriaBuilder cb, String[] path, Collection<?> collection) {
        if (collection != null && !collection.isEmpty()) {
            predicates.add(createInPredicate(cb, path, collection, annotationRoot));
        }
    }

    public void checkAndAddDatePredicate(List<Predicate> predicates, Root<UserAnnotation> annotationRoot, CriteriaBuilder cb, Date date1, Date date2) {
        if (date1 != null && date2 != null) {
            predicates.add(cb.between(annotationRoot.get("dt"), date1, date2));
        }
    }

    public void checkAndAddSentenceDatePredicate(List<Predicate> predicates, Root<UserAnnotation> annotationRoot, CriteriaBuilder cb, String sentenceDate1, String sentenceDate2) {
        if (sentenceDate1 != null && sentenceDate2 != null) {
            predicates.add(cb.and(
                    cb.between(annotationRoot.get("leftSentenceId").get("sentenceDate"), sentenceDate1, sentenceDate2),
                    cb.between(annotationRoot.get("rightSentenceId").get("sentenceDate"), sentenceDate1, sentenceDate2)));
        }
    }

    private Predicate createInPredicate(CriteriaBuilder cb, String[] path, Collection<?> value, Root<?> root) {
        return findPredicatePath(cb, root, path).in(value);
    }
}

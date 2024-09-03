package durel.services.statistics;

import durel.domain.AnnotationFilterCriteria;
import durel.dto.responses.statistics.AnnotationCounts;
import durel.domain.model.User;
import durel.domain.model.Project;
import durel.domain.model.Lemma;
import durel.services.*;
import durel.services.annotation.AnnotationQueryService;
import durel.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseCountsStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCountsStatisticsService.class);

    private final UserService userService;

    private final ProjectService projectService;

    private final AnnotationQueryService annotationQueryService;

    private final UseService useService;

    private final FilterDataService filterDataService;

    private final PairService pairService;

    @Autowired
    public DatabaseCountsStatisticsService(UserService userService, ProjectService projectService, AnnotationQueryService annotationQueryService, UseService useService, FilterDataService filterDataService, PairService pairService) {
        this.userService = userService;
        this.projectService = projectService;
        this.annotationQueryService = annotationQueryService;
        this.useService = useService;
        this.filterDataService = filterDataService;
        this.pairService = pairService;
    }

    public List<AnnotationCounts> getAllAnnotationCounts (String projectName, String usernames) {
        try {
            Project project = projectService.getProject(projectName);
            List<AnnotationCounts> allAnnotationCounts = new ArrayList<>();
            List<User> annotatorsList = userService.stringOfUsernamesToListOfUsers(usernames);
            allAnnotationCounts.add(getAnnotationCounts(project, null, annotatorsList, false));
            for (Lemma lemma : project.getLemmas()) {
                allAnnotationCounts.add(getAnnotationCounts(project, lemma, annotatorsList, true));
            }
            setAllWordsPairCounts(allAnnotationCounts);
            return allAnnotationCounts;
        } catch (InstanceNotFoundException e) {
            logger.error("Did not find project {}", projectName);
            return new ArrayList<>();
        }
    }

    private AnnotationCounts getAnnotationCounts (Project project, Lemma lemma, List<User> annotatorsList, boolean isWord) {
        AnnotationCounts annotationCounts = initializeCounts(project, lemma, isWord);
        setPairCounts(annotationCounts, project, lemma, isWord);
        calculateAnnotatorCounts(annotationCounts, project, lemma, annotatorsList);
        finishCounts(annotationCounts);
        return annotationCounts;
    }

    private AnnotationCounts initializeCounts(Project project, Lemma lemma, boolean isWord){
        AnnotationCounts annotationCounts = new AnnotationCounts();
        if (isWord) {
            annotationCounts.setWord(lemma.getLemma());
            annotationCounts.setNoOfUses(lemma.getUses().size());
        } else {
            annotationCounts.setWord("All Words");
            annotationCounts.setNoOfUses(useService.getNoOfSentencesOfProject(project));
        }
        return annotationCounts;
    }

    private void setPairCounts(AnnotationCounts annotationCounts, Project project, Lemma lemma, boolean isWord){
        if (isWord) {
            int count = pairService.countByProjectAndLemma(project, lemma.getLemma());
            if (count != 0) {
                annotationCounts.setNoOfPairs(count);
            } else {
                annotationCounts.setNoOfPairs((annotationCounts.getNoOfUses()*(annotationCounts.getNoOfUses()-1))/2);
            }
        }
    }

    private void setAllWordsPairCounts(List<AnnotationCounts> annotationCounts) {
        if (annotationCounts.get(0).getNoOfPairs() == 0) {
            int noOfPairs = 0;
            for (AnnotationCounts annotationCount : annotationCounts) {
                noOfPairs += annotationCount.getNoOfPairs();
            }
            annotationCounts.get(0).setNoOfPairs(noOfPairs);
        }
    }

    private void calculateAnnotatorCounts(AnnotationCounts annotationCounts, Project project, Lemma lemma, List<User> annotatorsList){
        for (User annotator : annotatorsList) {
            AnnotationFilterCriteria annotationFilterCriteria = filterDataService.createAnnotationFilterData(lemma, project, null, null, annotator, null, null, null);
            List<Integer> annotatorCounts = annotationQueryService.getAnnotationCounts(annotationFilterCriteria);
            if (annotatorCounts.get(0) > 0) {
                increaseAnnotationCounts(annotationCounts, annotatorCounts, annotator.getUsername());
            }
        }
    }

    private void finishCounts(AnnotationCounts annotationCounts){
        if (!annotationCounts.getNamesOfAnnotators().isEmpty()) {
            annotationCounts.setNamesOfAnnotators(annotationCounts.getNamesOfAnnotators().substring(2));
        }
        annotationCounts.calculateAverage();
    }

    private void increaseAnnotationCounts(AnnotationCounts annotationCounts, List<Integer> counts, String username) {
        annotationCounts.increaseNoOfAnnotations(counts.get(0));
        annotationCounts.increaseNoOf0(counts.get(1));
        annotationCounts.increaseNoOf1(counts.get(2));
        annotationCounts.increaseNoOf2(counts.get(3));
        annotationCounts.increaseNoOf3(counts.get(4));
        annotationCounts.increaseNoOf4(counts.get(5));
        annotationCounts.increaseNoOfAnnotators();
        annotationCounts.setNamesOfAnnotators(annotationCounts.getNamesOfAnnotators() + ", " + username);
    }
}

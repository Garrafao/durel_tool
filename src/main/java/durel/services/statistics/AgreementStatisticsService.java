package durel.services.statistics;

import durel.domain.AnnotationFilterCriteria;
import durel.dto.responses.statistics.AgreementStatisticsData;
import durel.domain.model.annotation.UserAnnotation;
import durel.domain.model.User;
import durel.domain.model.Project;
import durel.domain.model.Lemma;
import durel.services.FilterDataService;
import durel.services.ProjectService;
import durel.services.annotation.AnnotationQueryService;
import durel.services.user.UserService;
import durel.utils.OrdinalDistanceFunctionWithDoubles;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.CohenKappaAgreement;
import org.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import org.dkpro.statistics.agreement.distance.OrdinalDistanceFunction;
import org.dkpro.statistics.correlation.PearsonCorrelation;
import org.dkpro.statistics.correlation.SpearmansRankCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import java.util.*;

@Service
public class AgreementStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(AgreementStatisticsService.class);

    private final AnnotationQueryService annotationService;

    private final UserService userService;

    private final FilterDataService filterDataService;

    private final ProjectService projectService;

    private final AgreementStatisticsSessionDataService agreementStatisticsSessionDataService;

    @Autowired
    public AgreementStatisticsService(AnnotationQueryService annotationService, UserService userService, FilterDataService filterDataService, ProjectService projectService, AgreementStatisticsSessionDataService agreementStatisticsSessionDataService) {
        this.annotationService = annotationService;
        this.userService = userService;
        this.filterDataService = filterDataService;
        this.projectService = projectService;
        this.agreementStatisticsSessionDataService = agreementStatisticsSessionDataService;
    }

    public double[][] getAllAgreementStatistics(String projectSelect, String wordSelect, String annotatorsList, String metricSelect) {
        try {
            Project project = projectService.getProject(projectSelect);
            List<User> annotators = userService.stringOfUsernamesToListOfUsers(annotatorsList);
            // Todo somehow this check does not work
            if (agreementStatisticsSessionDataService.checkSessionData(annotators, project)) {
                Map<String, AgreementStatisticsData> agreementStatisticsDataMap = calculateAgreementStatistics(annotators, project);
                agreementStatisticsSessionDataService.updateSessionData(annotators, project, agreementStatisticsDataMap);
            }
            return agreementStatisticsSessionDataService.getAgreementStatisticsDataMap(wordSelect, metricSelect);
        } catch (InstanceNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public Map<String, AgreementStatisticsData> calculateAgreementStatistics(List<User> annotators, Project project) {
        // TODO Nan-Value Setting
        String[] metrics = {"KrippendorffAlphaAgreement", "CohenKappaAgreement", "PearsonCorrelation", "SpearmansRankCorrelation", "HammingLoss"};

        Map<String, AgreementStatisticsData> agreementStatisticsDataMap = new HashMap<>();
        List<UserAnnotation> allUserAnnotations = new ArrayList<>();

        for (Lemma lemma :project.getLemmas()) {
            AnnotationFilterCriteria annotationFilterCriteria = filterDataService.createAnnotationFilterDataLists(List.of(new Lemma[]{lemma}), null, null, null, annotators, null, null, null);
            List<UserAnnotation> userAnnotations = new ArrayList<>(annotationService.getAnnotationsWithFilterOptions(annotationFilterCriteria));
            allUserAnnotations.addAll(userAnnotations);
            addAgreementStatisticsData(userAnnotations, agreementStatisticsDataMap, metrics, annotators, lemma.getLemma());
        }
        addAgreementStatisticsData(allUserAnnotations, agreementStatisticsDataMap, metrics, annotators, "wholeProject");

        return agreementStatisticsDataMap;
    }

    private void addAgreementStatisticsData(List<UserAnnotation> userAnnotations, Map<String, AgreementStatisticsData> agreementStatisticsDataMap, String[] metrics, List<User> annotators, String word) {
        if (!userAnnotations.isEmpty()) {
            agreementStatisticsDataMap.put(word, new AgreementStatisticsData(word, metrics, userAnnotations, annotators));
            getAgreements(agreementStatisticsDataMap.get(word));
        }
        else {
            agreementStatisticsDataMap.put(word, new AgreementStatisticsData());
        }
    }

    private void getAgreements(AgreementStatisticsData agreementStatisticsData) {
        for (int i = 0; i< agreementStatisticsData.getAnnotators().size(); i++) {
            try {
                for (int j = i+1; j< agreementStatisticsData.getAnnotators().size(); j++) {
                    calculateAgreementForPairOfAnnotators(i, j, agreementStatisticsData);
                }
                calculateAgreementForPairOfAnnotators(i, agreementStatisticsData.getAnnotators().size(), agreementStatisticsData);
            }
            catch (IndexOutOfBoundsException ignored) {
            }
        }
        KrippendorffAlphaAgreement krippendorffAlphaAgreement = new KrippendorffAlphaAgreement(agreementStatisticsData.getCodingAnnotationStudy(), new OrdinalDistanceFunction());
        agreementStatisticsData.getAgreementData().put("Krippendorff_full", new double[1][1]);
        agreementStatisticsData.getAgreementData().get("Krippendorff_full")[0][0] = krippendorffAlphaAgreement.calculateAgreement();
    }

    private CodingAnnotationStudy createCodingAnnotationStudyFor2(List<Double> list1, List<Double> list2) {
        CodingAnnotationStudy codingAnnotationStudy = new CodingAnnotationStudy(2);
        for (int j=0;j<list1.size();j++) {
            codingAnnotationStudy.addItem(list1.get(j), list2.get(j));
        }
        return codingAnnotationStudy;
    }

    private void calculateAgreementForPairOfAnnotators(int i, int j, AgreementStatisticsData agreementStatisticsData) {
        if (i >= agreementStatisticsData.getAnnotators().size() || j > agreementStatisticsData.getAnnotators().size()) {
            throw new IndexOutOfBoundsException("Trying to calculate agreement for nonexistent annotators.");
        }
        CodingAnnotationStudy codingAnnotationStudy;
        List<Double> list1 = new ArrayList<>(agreementStatisticsData.getAnnotator2Judgment().get(i));
        List<Double> list2;
        if (j < agreementStatisticsData.getAnnotators().size()) {
            list2 = new ArrayList<>(agreementStatisticsData.getAnnotator2Judgment().get(j));
            codingAnnotationStudy = agreementStatisticsData.getCodingAnnotationStudy().extractRaters(i,j);
        }
        else {
            list2 = new ArrayList<>(agreementStatisticsData.getAnnotator2MeanOther().get(i));
            codingAnnotationStudy = createCodingAnnotationStudyFor2(list1, list2);
        }

        KrippendorffAlphaAgreement krippendorffAlphaAgreement = new KrippendorffAlphaAgreement(codingAnnotationStudy, new OrdinalDistanceFunctionWithDoubles());
        agreementStatisticsData.getAgreementData().getOrDefault("KrippendorffAlphaAgreement", new double[i][j])[i][j] = krippendorffAlphaAgreement.calculateAgreement();

        List<List<Double>> removedNaN= removeNaN(list1, list2);
        list1 = removedNaN.get(0);
        list2 = removedNaN.get(1);
        agreementStatisticsData.getOverlap()[i][j] = list1.size();

        CohenKappaAgreement cohenKappaAgreement = new CohenKappaAgreement(codingAnnotationStudy);
        agreementStatisticsData.getAgreementData().getOrDefault("CohenKappaAgreement", new double[i][j])[i][j] = cohenKappaAgreement.calculateAgreement();
        agreementStatisticsData.getAgreementData().getOrDefault("SpearmansRankCorrelation", new double[i][j])[i][j] = SpearmansRankCorrelation.computeCorrelation(list1, list2);
        agreementStatisticsData.getAgreementData().getOrDefault("PearsonCorrelation", new double[i][j])[i][j] = PearsonCorrelation.computeCorrelation(list1, list2);
        agreementStatisticsData.getAgreementData().getOrDefault("HammingLoss", new double[i][j])[i][j] = hammingLoss(list1, list2);
    }

    private List<List<Double>> removeNaN(List<Double> list1, List<Double> list2) {
        assert list1.size() == list2.size();
        for (int j = 0; j < list1.size(); j++) {
            if (list1.get(j).isNaN() || list2.get(j).isNaN()) {
                list1.set(j, Double.NaN);
                list2.set(j, Double.NaN);
            }
        }
        List<List<Double>> result = new ArrayList<>();
        result.add(list1.stream().filter(value -> !value.isNaN()).toList());
        result.add(list2.stream().filter(value -> !value.isNaN()).toList());
        return result;
    }

    private double hammingLoss(List<Double> list1, List<Double> list2) {
        int count=0;
        if(list1.size()==list2.size()) {
            for(int i=0;i<list1.size();i++) {
                if(!Objects.equals(list1.get(i), list2.get(i))) count++;
            }
        }
        return count/(double)list1.size();
    }
}

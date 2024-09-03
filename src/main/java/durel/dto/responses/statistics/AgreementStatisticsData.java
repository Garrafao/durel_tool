package durel.dto.responses.statistics;

import durel.domain.model.annotation.UserAnnotation;
import durel.domain.model.User;
import lombok.Getter;
import lombok.Setter;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class AgreementStatisticsData {

    private String lemma;

    private List<User> annotators;
    private List<UserAnnotation> userAnnotations;

    private Map<Set<Integer>, Map<User,List<UserAnnotation>>> combo2Annotator2Annotation;
    private List<List<Double>> annotator2Judgment;
    private List<List<Double>> annotator2MeanOther;
    private Map<Set<Integer>,List<UserAnnotation>> combo2Annotations;

    private CodingAnnotationStudy codingAnnotationStudy;

    private Double averageAnnotationNumber;

    private Map<String, double[][]> agreementData = new HashMap<>();
    private double[][] overlap;

    public AgreementStatisticsData() {

    }

    public AgreementStatisticsData(String lemma, String[] metrics, List<UserAnnotation> userAnnotations, List<User> annotators) {
        this.lemma = lemma;
        this.annotators = annotators;
        this.userAnnotations = userAnnotations;
        this.overlap = new double[annotators.size()][annotators.size()+1];
        extractCombo2Annotations();
        extractCombo2Annotator2Annotations();
        extractAnnotator2Judgment();
        extractMeanOther();
        extractAverageAnnotationNumber();
        createCodingAnnotationStudy();
        for (String metric : metrics) {
            agreementData.put(metric, new double[annotators.size()][annotators.size()+1]);
        }
    }

    private void extractCombo2Annotations() {
        this.combo2Annotations = this.userAnnotations.stream()
                .collect(Collectors.groupingBy(annotation -> annotation.getId().getPair().getSentenceIDs()));
    }

    private void extractCombo2Annotator2Annotations() {
        this.combo2Annotator2Annotation = new HashMap<>();
        for (Map.Entry<Set<Integer>, List<UserAnnotation>> entries: combo2Annotations.entrySet()) {
            Map<User,List<UserAnnotation>> annotationList = entries.getValue().stream().collect(Collectors.groupingBy(UserAnnotation::getAnnotator));
            combo2Annotator2Annotation.put(entries.getKey(),annotationList);
        }
    }

    private void extractAnnotator2Judgment() {
        this.annotator2Judgment = annotators.stream()
                .map(annotator ->
                        combo2Annotator2Annotation.values().stream()
                                .map(annotatorListMap -> annotatorListMap.getOrDefault(annotator, new ArrayList<>()))
                                .map(this::getJudgment)
                                .toList())
                .toList();
    }

    private void extractMeanOther() {
        if (annotator2Judgment.size() == 1) {
            annotator2MeanOther = null;
            return;
        }
        annotator2MeanOther = new ArrayList<>();
        for (int i=0;i<annotators.size();i++) {
            List<List<Double>> copy = annotator2Judgment.stream().skip(i).toList();
            annotator2MeanOther.add(new ArrayList<>());
            for (int j=0;j<copy.get(0).size();j++) {
                int finalJ = j;
                double average = annotator2Judgment.stream()
                        .map(list -> list.get(finalJ))
                        .filter(value -> !value.isNaN())
                        .collect(Collectors.averagingDouble(Double::doubleValue));
                annotator2MeanOther.get(i).add(average);
            }
        }
    }

    private void extractAverageAnnotationNumber() {
        OptionalDouble averageAnnotationNumber = combo2Annotations.values().stream()
                .map(List::size).mapToDouble(Integer::doubleValue).average();
        this.averageAnnotationNumber = averageAnnotationNumber.orElse(0.0);
    }

    private void createCodingAnnotationStudy() {
        this.codingAnnotationStudy = new CodingAnnotationStudy(annotators.size());
        for (Map<User, List<UserAnnotation>> entries: combo2Annotator2Annotation.values()) {
            Object[] annotationsForAllAnnotators = annotators.stream().
                    map(annotator -> entries.getOrDefault(annotator,new ArrayList<>())).
                    map(this::getJudgment).toArray();
            assert annotationsForAllAnnotators.length == annotators.size();
            codingAnnotationStudy.addItemAsArray(annotationsForAllAnnotators);
        }
    }

    private Double getJudgment(List<UserAnnotation> userAnnotationList) {
        if (userAnnotationList.isEmpty()) {
            return Double.NaN;
        } else if (userAnnotationList.size() == 1) {
            return userAnnotationList.get(0).getJudgment().doubleValue();
        }
        else {
            System.out.println("There were more than one annotation per annotator!");
            return Double.NaN;
        }
    }
}

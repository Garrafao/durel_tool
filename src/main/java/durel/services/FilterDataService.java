package durel.services;

import durel.domain.AnnotationFilterCriteria;
import durel.domain.model.User;
import durel.domain.model.Project;
import durel.domain.model.Lemma;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class FilterDataService {

    public AnnotationFilterCriteria createAnnotationFilterData(Lemma lemma, Project project, String sentenceDate1,
                                                               String sentenceDate2, User annotator,
                                                               Collection<String> groupings, Collection<String> pos,
                                                               Collection<Float> judgments) throws IllegalStateException {
        List<String> annotators = null;
        if (annotator != null) {
            annotators = new ArrayList<>();
            annotators.add(annotator.getUsername());
        }
        return createAnnotationFilterData(lemma, project, sentenceDate1, sentenceDate2, annotators, groupings, pos, judgments);
    }


    public AnnotationFilterCriteria createAnnotationFilterData(Lemma lemma, Project project, String sentenceDate1,
                                                               String sentenceDate2, List<String> annotators,
                                                               Collection<String> groupings, Collection<String> pos,
                                                               Collection<Float> judgments) throws IllegalStateException {
        List<Integer> words = null;
        if (lemma != null) {
            words = new ArrayList<>();
            words.add(lemma.getId());
        }
        List<String> projects = null;
        if (project != null) {
            projects = new ArrayList<>();
            projects.add(project.getProjectName());
        }
        return new AnnotationFilterCriteria(words, projects, sentenceDate1, sentenceDate2, annotators, groupings, pos, judgments);
    }


    public AnnotationFilterCriteria createAnnotationFilterDataLists(List<Lemma> lemmata, List<Project> projects, String sentenceDate1,
                                                                    String sentenceDate2, List<User> annotators,
                                                                    Collection<String> groupings, Collection<String> pos,
                                                                    Collection<Float> judgments) throws IllegalStateException {
        List<Integer> words_ids = null;
        if (lemmata != null) {
            words_ids = new ArrayList<>();
            for (Lemma lemma : lemmata) {
                words_ids.add(lemma.getId());
            }
        }
        List<String> project_ids = null;
        if (projects != null) {
            project_ids = new ArrayList<>();
            for (Project project:projects) {
                project_ids.add(project.getProjectName());
            }
        }
        List<String> annotator_names = null;
        if (annotators != null) {
            annotator_names = new ArrayList<>();
            for (User annotator:annotators) {
                annotator_names.add(annotator.getUsername());
            }
        }
        return new AnnotationFilterCriteria(words_ids, project_ids, sentenceDate1, sentenceDate2, annotator_names, groupings, pos, judgments);
    }
}

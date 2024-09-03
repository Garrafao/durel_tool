package durel.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationFilterCriteria {

    private List<Integer> words;
    private List<String> projects;
    private String sentenceDate1;
    private String sentenceDate2;
    private List<String> annotators;
    private Collection<String> groupings;
    private Collection<String> pos;
    private Collection<Float> judgments;
}

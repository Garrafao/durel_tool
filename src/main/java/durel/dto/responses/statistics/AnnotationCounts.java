package durel.dto.responses.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AnnotationCounts {

    private String word;

    private int noOfUses;

    private int noOfPairs;

    private int noOfAnnotations;

    private int noOfAnnotators;

    private String namesOfAnnotators;

    private int noOf4;

    private int noOf3;

    private int noOf2;

    private int noOf1;

    private int noOf0;

    private float average;

    public AnnotationCounts() {
        this.namesOfAnnotators = "";
        this.noOfAnnotations = 0;
        this.noOfAnnotators = 0;
        this.noOf4 = 0;
        this.noOf3 = 0;
        this.noOf2 = 0;
        this.noOf1 = 0;
        this.noOf0 = 0;
        this.average = 0;
    }

    public void increaseNoOfAnnotators() {
        this.noOfAnnotators += 1;
    }

    public void increaseNoOfAnnotations(int addition) {
        this.noOfAnnotations += addition;
    }

    public void increaseNoOf4(int addition) {
        this.noOf4 += addition;
    }

    public void increaseNoOf3(int addition) {
        this.noOf3 += addition;
    }

    public void increaseNoOf2(int addition) {
        this.noOf2 += addition;
    }

    public void increaseNoOf1(int addition) {
        this.noOf1 += addition;
    }

    public void increaseNoOf0(int addition) {
        this.noOf0 += addition;
    }

    public void calculateAverage() {
        this.average = (float) (noOf1 + noOf2 * 2 + noOf3 * 3 + noOf4 * 4) / (noOfAnnotations - noOf0);
    }
}

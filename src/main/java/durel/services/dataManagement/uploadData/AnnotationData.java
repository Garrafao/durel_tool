package durel.services.dataManagement.uploadData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class AnnotationData extends PairedUploadData {

    @NotNull
    private String comment;

    @NotNull
    @Min(0)
    @Max(4)
    private float judgment;

    @NotNull
    private String annotator;

    public AnnotationData(String lemma, String identifierOne, String identifierTwo, String annotator, float judgment, String comment) {
        super(lemma, identifierOne, identifierTwo);
        this.comment = comment;
        this.judgment = judgment;
        this.annotator = annotator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        AnnotationData that = (AnnotationData) o;
        return this.identifierOne.equals(that.getIdentifierOne()) &&
                this.identifierTwo.equals(that.getIdentifierTwo()) &&
                this.annotator.equals(that.getAnnotator());
    }
}

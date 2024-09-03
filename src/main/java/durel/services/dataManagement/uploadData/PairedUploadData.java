package durel.services.dataManagement.uploadData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class PairedUploadData extends UploadData {

    @NotNull
    protected String identifierOne;

    @NotNull
    protected String identifierTwo;

    public PairedUploadData(String lemma, String identifierOne, String identifierTwo) {
        super(lemma);
        this.identifierOne = identifierOne;
        this.identifierTwo = identifierTwo;
    }

    public boolean isInstanceOfAnnotationData() {
        return this instanceof AnnotationData;
    }

    public boolean isInstanceOfInstanceData() {
        return this instanceof InstanceData;
    }

    public boolean equals(PairedUploadData pairedUploadData) {
        return this.identifierOne.equals(pairedUploadData.identifierOne) && this.identifierTwo.equals(pairedUploadData.identifierTwo)
                || this.identifierOne.equals(pairedUploadData.identifierTwo) && this.identifierTwo.equals(pairedUploadData.identifierOne);
    }
}
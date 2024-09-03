package durel.services.dataManagement.uploadData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InstanceData extends PairedUploadData {

    public InstanceData(String lemma, String identifierOne, String identifierTwo) {
        super(lemma, identifierOne, identifierTwo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        InstanceData that = (InstanceData) o;
        return this.identifierOne.equals(that.getIdentifierOne()) &&
                this.identifierTwo.equals(that.getIdentifierTwo());
    }
}

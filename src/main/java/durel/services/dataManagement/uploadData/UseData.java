package durel.services.dataManagement.uploadData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class UseData extends UploadData {

    String pos;

    @NotNull
    String date;

    @NotNull
    String grouping;

    @NotNull
    String identifier;

    String description;

    @NotNull
    String context;

    @NotNull
    String tokenIndex;

    @NotNull
    String sentenceIndex;

    public UseData(String lemma, String pos, String date, String grouping, String identifier, String description,
                   String context, String tokenIndex, String sentenceIndex) {
        super(lemma);
        this.pos = pos;
        this.date = date;
        this.grouping = grouping;
        this.identifier = identifier;
        this.description = description;
        this.context = context;
        this.tokenIndex = tokenIndex;
        this.sentenceIndex = sentenceIndex;
    }
}

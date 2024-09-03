package durel.dto.requests.statistics;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralParameterData {

    @NotEmpty
    private String languageID;

    @NotNull
    private String projectName;

    private String[] annotators;

}

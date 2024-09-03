package durel.dto.requests.statistics;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WUGsPipelineRequest {

    @NotEmpty
    private String languageID;

    @NotNull
    private String projectName;

    @NotNull
    private String[] lemmas;

    private String[] annotators;

    // AGGREGATION PARAMETERS
    private String summaryStatistic;

    private String edgeFilter;

    private boolean nanNodes;

    private boolean nanEdges;

    // CLUSTERING PARAMETERS
    @NotNull
    private String algorithm;

    private String ambiguity;

    private Integer min;

    private Integer max;

    private Integer collapse;

    @Min(-10)
    @Max(10)
    @NotNull
    private Float threshold;

    // Chinese Whispers parameters
    private String degree;

    // WSBM parameters
    private String distribution;

    private boolean isMultiple;

    private boolean degreeCorrection;

    private boolean adjacency;

    private boolean degreedl;

    // Correlation Clustering parameters
    @Min(1)
    private Integer noIterations;
}

package durel.dto.responses;


import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;

/**
 * Object that holds a sentence from the database with all given features.
 */
@Builder
@Getter
@Slf4j
public class UseDTO {

    @NotEmpty
    private int id;
    @NotEmpty
    private String word;

    // The example consists of an optional previousSentence, the sentence with leftContext, the target, rightContext, and of the nextSentence
    private String previousSentence;
    @NotEmpty
    private String leftSentenceContext;
    @NotEmpty
    private String target;
    @NotEmpty
    private String rightSentenceContext;
    private String nextSentence;

    private String pos;
    private String sentenceDate;
}

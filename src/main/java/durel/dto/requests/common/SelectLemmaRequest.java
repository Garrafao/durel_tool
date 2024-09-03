package durel.dto.requests.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

/**
 * Represents a Data Transfer Object (DTO) for a request that involves words.
 * It contains the necessary information to identify one or multiple words of a project.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
public class SelectLemmaRequest {
    /*
     * The lemmas variable stores an array of strings representing the lemma forms of words.
     */
    @NotNull
    private String[] lemmas;
    @NotNull
    private String projectName;
    /*
     * The languageID variable represents the identifier of a language. This variable is not strictly necessary
     * for the identification of words, but it is used due to the format of the front-end.
     */
    @NotNull
    private String languageID;
}

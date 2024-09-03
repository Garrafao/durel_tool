package durel.services.dtoServices;

import durel.domain.model.Use;
import durel.domain.model.TutorialUse;
import durel.dto.responses.UseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class UseDTOService {

    /**
     * This constant variable is a mapping of space patterns that need to be removed from a given string.
     * The keys represent the space patterns to be replaced, and the values represent the replacement for those patterns.
     */
    private static final Map<String, String> REMOVE_SPACES_MAPPING = Map.ofEntries(
            Map.entry(" ,", ","),
            Map.entry(" .", "."),
            Map.entry(" ;", ";"),
            Map.entry(" ?", "?"),
            Map.entry(" !", "!"),
            Map.entry("„ ", "„"),
            Map.entry(" “", "“"),
            Map.entry(" \"", "\""),
            Map.entry(" :", ":"),
            Map.entry(" )", ")"),
            Map.entry("( ", "("),
            Map.entry(" 's", "'s"),
            Map.entry("- ", "-"),
            Map.entry("  ", " ")
    );


    public UseDTO constructSentenceDTOfromAnnotationSentence(Use use) {
        String[] sentenceParts = splitContextIntoParts(use.getContext(), use.getIndexesTargetSentence(), use.getIndexesTargetToken());
        return UseDTO.builder()
                .id(use.getId())
                .pos(use.getPos())
                .sentenceDate(use.getUseDate())
                .word(use.getLemma().getLemma())
                .previousSentence(sentenceParts[0])
                .leftSentenceContext(sentenceParts[1])
                .target(sentenceParts[2])
                .rightSentenceContext(sentenceParts[3])
                .nextSentence(sentenceParts[4])
                .build();
    }

    public UseDTO constructSentenceDTOfromTutorialSentence(TutorialUse sentence) {
        String[] sentenceParts = splitContextIntoParts(sentence.getContext(), sentence.getIndexesTargetSentence(), sentence.getIndexesTargetToken());
        return UseDTO.builder()
                .id(sentence.getId())
                .word(sentence.getWord())
                .previousSentence(sentenceParts[0])
                .leftSentenceContext(sentenceParts[1])
                .target(sentenceParts[2])
                .rightSentenceContext(sentenceParts[3])
                .nextSentence(sentenceParts[4])
                .build();
    }

    public UseDTO constructEmptyUseDTO() {
        return UseDTO.builder()
                .target("")
                .rightSentenceContext("")
                .leftSentenceContext("")
                .nextSentence("")
                .previousSentence("")
                .build();
    }

    /**
     * Ensures that the target indexes provided are valid within the given context length.
     *
     * @param targetIndices   the indexes of the target within the context, specified as "start:end"
     * @param contextLength   the length of the context
     * @param indexCorrection the correction value to adjust the indexes if context is already a substring
     * @return an array of two integers representing the validated start and end indexes
     */
    private int[] ensureValidIndices(String targetIndices, int contextLength, int indexCorrection){
        String[] indexes = targetIndices.split(":");
        try {
            int startIndex = Integer.parseInt(indexes[0]) - indexCorrection;
            int endIndex = Integer.parseInt(indexes[1]) - indexCorrection;
            startIndex = Math.min(Math.max(0, startIndex), contextLength);
            endIndex = Math.min(Math.max(0, endIndex), contextLength);
            if (startIndex > endIndex) throw new IndexOutOfBoundsException("Index out of bounds");
            return new int[]{startIndex, endIndex};
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return new int[]{contextLength, contextLength};
        }
    }

    /**
     * Splits a given string into three parts based on the target indexes.
     *
     * @param context        the input string to be split
     * @param targetIndices  an array of two integers representing the start and end indexes of the target within the context
     * @return an array of three strings representing the context split at the given indexes
     */
    private String[] splitString(String context, int[] targetIndices) {
        String beforeTarget = context.substring(0, targetIndices[0]);
        String target = context.substring(targetIndices[0], targetIndices[1]);
        String afterTarget = context.substring(targetIndices[1]);
        return new String[]{beforeTarget, target, afterTarget};
    }

    /**
     * Splits the given context into parts based on the indexes of the target sentence and target token.
     * The context is split into five parts:
     *   1. Sentence before the target sentence,
     *   2. Target sentence up to the target token,
     *   3. Target token,
     *   4. Target sentence after the target token,
     *   5. Sentence after the target sentence.
     *
     * @param context The original context to split.
     * @param indexesTargetSentence The indexes of the target sentence in the context.
     *                              16:27 means, first char is at position 16 and last char is at position 26.
     * @param indexesTargetToken The indexes of the target token in the context.
     *                           16:27 means, first char is at position 16 and last char is at position 26.
     * @return An array of five strings representing the split context parts,
     *         with leading and trailing spaces removed.
     */
    private String[] splitContextIntoParts(String context, String indexesTargetSentence, String indexesTargetToken){
        int contextLength = context.length();
        int[] validatedSentenceIndices = ensureValidIndices(indexesTargetSentence, contextLength, 0);
        String[] splitContext = splitString(context, validatedSentenceIndices);
        int[] validatedTokenIndices = ensureValidIndices(indexesTargetToken, splitContext[1].length(), validatedSentenceIndices[0]);
        String[] splitTargetSentence = splitString(splitContext[1], validatedTokenIndices);

        String[] sentenceParts = {"", "", "", "", ""};
        sentenceParts[0] = splitContext[0];
        sentenceParts[1] = splitTargetSentence[0];
        sentenceParts[2] = splitTargetSentence[1];
        sentenceParts[3] = splitTargetSentence[2];
        sentenceParts[4] = splitContext[2];
        for (int i = 0; i < sentenceParts.length; i++) {
            sentenceParts[i] = trimSpaces(sentenceParts[i]);
        }
        return sentenceParts;
    }

    /**
     * Trims spaces from a given string based on a predefined mapping of space patterns.
     *
     * @param str the string to trim spaces from
     * @return the string with spaces trimmed
     */
    private String trimSpaces(String str){
        for (Map.Entry<String, String> entry : REMOVE_SPACES_MAPPING.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }
        return str;
    }
}

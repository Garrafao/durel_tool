package durel.services.dataManagement.fileTypeSpecifications;

import durel.services.dataManagement.uploadData.UseData;
import durel.domain.model.Use;
import durel.domain.model.Lemma;
import durel.services.dataManagement.upload.DefaultFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class UseFileType extends DefaultFileType<Use, UseData> {

    private static final Logger useLogger = LoggerFactory.getLogger(UseFileType.class);
    private static final String useFileName = "uses.csv";
    private static final int useColumnNumber = 9;
    private static final String[] useContentColumns = {"lemma", "pos", "date", "grouping", "identifier", "description", "context", "indexes_target_token", "indexes_target_sentence"};
    private static final String[] useSystemColumns = {"identifier_system", "project", "lang", "user"};

    protected UseFileType() {
        super(useLogger, useFileName, useColumnNumber, useContentColumns, useSystemColumns);
    }

    // METHODS --------------------------------------------------------------------------------------

    /**
     * Retrieves the content data from a Sentence and Word object.
     *
     * @param use the Sentence object
     * @param lemma the Word object
     * @return an array of strings containing the content data
     * @throws NullPointerException if the annotation or word is null, or if any of the required fields in the annotation are null
     */
    @Override
    protected String[] getDataContent(Use use, Lemma lemma) throws NullPointerException {
        super.getDataContent(use, lemma);
        return new String[]{lemma.getLemma(), use.getPos(), use.getUseDate(), use.getGrouping(),
                use.getCsvId(), use.getDescription(), use.getContext(),
                use.getIndexesTargetToken(), use.getIndexesTargetSentence(), String.valueOf(use.getId()),
                lemma.getProject().getProjectName(), lemma.getProject().getLanguage().getCode(),
                lemma.getProject().getCreator().getUsername()};
    }

    /**
     * Converts a list of string data into a UseData object.
     *
     * @param dataLine the list of strings containing the data
     * @return a UseData object
     */
    @Override
    protected UseData stringToData(List<String> dataLine) throws IllegalArgumentException {
        String lemma = DefaultFileUpload.extractData(dataLine, 0);
        String pos = DefaultFileUpload.extractData(dataLine, 1);
        String date = DefaultFileUpload.extractData(dataLine, 2);
        String grouping = DefaultFileUpload.extractData(dataLine, 3);
        String identifier = DefaultFileUpload.extractData(dataLine, 4);
        String description = DefaultFileUpload.extractData(dataLine, 5);
        String context = DefaultFileUpload.normalize(DefaultFileUpload.extractData(dataLine, 6));
        String tokenIndex = DefaultFileUpload.extractData(dataLine, 7);
        String sentenceIndex = DefaultFileUpload.extractData(dataLine, 8);
        return new UseData(lemma, pos, date, grouping, identifier,
                description, context,tokenIndex, sentenceIndex);
    }

    /**
     * Validates a sentence by checking if it is complete.
     *
     * @param use the Sentence object to validate
     * @param lemma the Word object associated with the annotation
     * @throws NullPointerException if the annotation or word is null, or if any of the required fields in the annotation are null
     */
    @Override
    protected void validateData(Use use, Lemma lemma) throws NullPointerException {
        if (lemma == null || use == null ||
                lemma.getProject() == null ||
                lemma.getProject().getLanguage() == null || lemma.getProject().getCreator() == null) {
            throw new NullPointerException("Encountered an incomplete word or sentence!");
        }
    }

    // This method only appears in this class, not in the other file type specifications
    /**
     * Retrieves the header for computational annotators.
     *
     * @return an array of strings containing the header for computational annotators
     */
    protected String getHeaderForComputationalAnnotators() {
        return String.join(DELIMITER, useContentColumns[0],
                useContentColumns[4]+"1", useContentColumns[6]+"1", useContentColumns[7]+"1",
                useContentColumns[4]+"2", useContentColumns[6]+"2", useContentColumns[7]+"2") + NEW_LINE;
    }
}

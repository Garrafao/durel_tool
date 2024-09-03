package durel.services.dataManagement.fileTypeSpecifications;

import durel.domain.model.Use;
import durel.services.dataManagement.uploadData.AnnotationData;
import durel.domain.model.annotation.UserAnnotation;
import durel.domain.model.Lemma;
import durel.services.dataManagement.upload.DefaultFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AnnotationFileType extends DefaultFileType<UserAnnotation, AnnotationData> {

    private static final Logger annotationLogger = LoggerFactory.getLogger(AnnotationFileType.class);
    private static final String annotationFileName = "annotations.csv";
    private static final int annotationColumnNumber = 6;
    private static final String[] annotationContentColumns = {"identifier1", "identifier2", "annotator", "judgment", "comment", "lemma"};
    private static final String[] annotationSystemColumns = {"timestamp", "identifier1_system", "identifier2_system"};

    protected AnnotationFileType() {
        super(annotationLogger, annotationFileName, annotationColumnNumber, annotationContentColumns, annotationSystemColumns);
    }

    // METHODS --------------------------------------------------------------------------------------

    /**
     * Retrieves the content data from an Annotation and Word object.
     *
     * @param userAnnotation the Annotation object
     * @param lemma the Word object
     * @return an array of strings containing the content data
     * @throws NullPointerException if the annotation or word is null, or if any of the required fields in the annotation are null
     */
    @Override
    protected String[] getDataContent(UserAnnotation userAnnotation, Lemma lemma) throws NullPointerException {
        super.getDataContent(userAnnotation, lemma);
        Use use1 = userAnnotation.getUses().stream().toList().get(0);
        Use use2 = userAnnotation.getUses().stream().toList().get(1);
        return new String[]{use1.getCsvId(),
                use2.getCsvId(),
                userAnnotation.getAnnotator().getUsername(),
                String.valueOf(userAnnotation.getJudgment()),
                userAnnotation.getComment(),
                lemma.getLemma(),
                String.valueOf(userAnnotation.getDt()),
                String.valueOf(use1.getId()),
                String.valueOf(use2.getId())};
    }

    /**
     * Converts a list of string data into an AnnotationData object.
     *
     * @param dataLine the list of strings containing the data
     * @return an AnnotationData object
     */
    @Override
    protected AnnotationData stringToData(List<String> dataLine) throws IllegalArgumentException{
        String identifier1 = DefaultFileUpload.extractData(dataLine, 0);
        String identifier2 = DefaultFileUpload.extractData(dataLine, 1);
        String annotator = DefaultFileUpload.extractData(dataLine, 2);
        float vote = Float.parseFloat(DefaultFileUpload.extractData(dataLine, 3));
        String comment = DefaultFileUpload.extractData(dataLine, 4);
        String lemma = DefaultFileUpload.extractData(dataLine, 5);
        return new AnnotationData(lemma, identifier1, identifier2, annotator, vote, comment);
    }

    /**
     * Validates an annotation by checking if it is complete.
     *
     * @param userAnnotation the Annotation object to validate
     * @param lemma the Word object associated with the annotation
     * @throws NullPointerException if the annotation or word is null, or if any of the required fields in the annotation are null
     */
    @Override
    protected void validateData(UserAnnotation userAnnotation, Lemma lemma) throws NullPointerException {
        if (lemma == null || userAnnotation == null ||
                userAnnotation.getAnnotator() == null ||
                userAnnotation.getUses().stream().toList().get(0) == null || userAnnotation.getUses().stream().toList().get(1) == null) {
            throw new NullPointerException("Encountered an incomplete annotation!");
        }
    }
}

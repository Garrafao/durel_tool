package durel.services.dataManagement.upload;

import durel.services.dataManagement.uploadData.AnnotationData;
import durel.services.dataManagement.fileTypeSpecifications.AnnotationFileType;
import durel.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for loading annotation data from a file and converting it into AnnotationData objects.
 */
@Service
public class AnnotationFileUploadService extends AnnotationFileType implements DefaultFileUpload<AnnotationData> {
    private final UserService userService;

    @Qualifier("lightTaskExecutor")
    ThreadPoolTaskExecutor taskExecutor;


    @Autowired
    public AnnotationFileUploadService(UserService userService, ThreadPoolTaskExecutor taskExecutor) {
        this.userService = userService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public int getColumnNumber() {
        return super.getColumnNumber();
    }

    @Override
    public String[] getColumns() {
        return super.getColumns();
    }

    @Override
    public AnnotationData getData(List<String> dataLine) {
        return super.stringToData(dataLine);
    }

    @Override
    public ThreadPoolTaskExecutor getExecutor() {
        return taskExecutor;
    }

    /**
     * Performs data type specific checks. In the case of annotation data, it is checked whether the annotator exists,
     * and whether the annotation is a duplicate.
     *
     * @param lineNumber the line number of the data
     * @param data the annotation data
     * @param dataSet the set of annotation data
     * @throws IOException if an error occurs while performing the checks
     */
    @Override
    public void doDataTypeSpecificChecks(int lineNumber, AnnotationData data, Set<AnnotationData> dataSet) throws IOException {
        userService.getUserByUsername(data.getAnnotator());
        checkDuplicateAnnotations(lineNumber, data, dataSet);
    }

    private void checkDuplicateAnnotations(int lineNumber, AnnotationData annotation, Set<AnnotationData> annotations) throws IOException {
        if (annotations.contains(annotation)) {
            throw new IOException("Line " + lineNumber + " contains a duplicate vote (same annotator and identifiers).");
        }
    }

    @Override
    public void validateIdentifiers(int lineNumber, AnnotationData judgment, Set<String> usesIdentifiers) throws IOException {
        if (!usesIdentifiers.contains(judgment.getIdentifierOne()) || !usesIdentifiers.contains(judgment.getIdentifierTwo())) {
            if (!usesIdentifiers.isEmpty()) {
                throw new IOException("An identifier in line " + lineNumber + " could not be linked to any identifier in the uses files.");
            }
        }
    }
}

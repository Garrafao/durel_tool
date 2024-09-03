package durel.services.dataManagement.download;

import durel.domain.AnnotationFilterCriteria;
import durel.domain.model.annotation.UserAnnotation;
import durel.domain.model.User;
import durel.domain.model.Lemma;
import durel.services.FilterDataService;
import durel.services.annotation.AnnotationQueryService;
import durel.services.dataManagement.fileTypeSpecifications.AnnotationFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnnotationFileDownloadService extends AnnotationFileType implements DefaultFileDownload<UserAnnotation> {

    /**
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(AnnotationFileDownloadService.class);

    AnnotationQueryService annotationService;

    FilterDataService filterDataService;

    @Autowired
    public AnnotationFileDownloadService(AnnotationQueryService annotationService, FilterDataService filterDataService) {
        super();
        this.annotationService = annotationService;
        this.filterDataService = filterDataService;
    }

    @Override
    public String getFileName() {
        return super.getFileName();
    }

    @Override
    public String getHeader() {
        return getFullHeader();
    }

    /**
     * Retrieves a set of annotations for download based on the given word.
     *
     * @param lemma The Word object.
     * @return A set of Annotation objects that match the given word. The set is filtered to exclude annotations with a judgment value of -1.
     */
    @Override
    public Set<UserAnnotation> getDataForDownload(@NotNull Lemma lemma) {
        AnnotationFilterCriteria annotationFilterCriteria = filterDataService.createAnnotationFilterData(lemma, null, null, null, (User) null, null, null, null);
        return annotationService.getAnnotationsWithFilterOptions(annotationFilterCriteria)
                .stream().filter(annotation -> annotation.getJudgment() != -1).collect(Collectors.toSet());
        // We filter -1 Judgments because they only exist for system purposes. TODO make -1 judgments superfluous
    }

    /**
     * Converts data to a list of strings.
     *
     * @param lemma        The Word object.
     * @return A list of strings representing the data in a file format.
     */
    @Override
    public List<String> dataToListOfStrings(@NotNull Lemma lemma) {
        return dataSetToStringList(getDataForDownload(lemma), lemma);
    }
}

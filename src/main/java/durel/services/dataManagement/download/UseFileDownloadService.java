package durel.services.dataManagement.download;

import durel.domain.model.Use;
import durel.domain.model.Lemma;
import durel.services.dataManagement.fileTypeSpecifications.UseFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Service
public class UseFileDownloadService extends UseFileType implements DefaultFileDownload<Use> {

    /**
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(UseFileDownloadService.class);

    public UseFileDownloadService() {
        super();
    }

    @Override
    public String getFileName() {
        return super.getFileName();
    }

    @Override
    public String getHeader() {
        return super.getFullHeader();
    }

    @Override
    public Set<Use> getDataForDownload(@NotNull Lemma lemma) {
        return lemma.getUses();
    }

    @Override
    public List<String> dataToListOfStrings(@NotNull Lemma lemma) {
        return dataSetToStringList(getDataForDownload(lemma), lemma);
    }
}

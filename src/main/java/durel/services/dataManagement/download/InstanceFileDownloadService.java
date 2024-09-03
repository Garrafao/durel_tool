package durel.services.dataManagement.download;

import durel.domain.model.Instance;
import durel.domain.model.Lemma;
import durel.services.PairService;
import durel.services.dataManagement.fileTypeSpecifications.InstanceFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class InstanceFileDownloadService extends InstanceFileType implements DefaultFileDownload<Instance> {

    /**
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(InstanceFileDownloadService.class);

    private final PairService pairService;

    @Autowired
    public InstanceFileDownloadService(PairService pairService) {
        super();
        this.pairService = pairService;
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
    public Set<Instance> getDataForDownload(@NotNull Lemma lemma) {
        return new HashSet<>(pairService.getPairsByProjectAndLemma(lemma.getProject(), lemma.getLemma()));
    }

    @Override
    public List<String> dataToListOfStrings(@NotNull Lemma lemma) {
        return dataSetToStringList(getDataForDownload(lemma), lemma);
    }
}

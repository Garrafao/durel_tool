package durel.services.dataManagement.annotatorData;

import durel.domain.model.Instance;
import durel.domain.model.Use;
import durel.domain.model.Lemma;
import durel.services.PairService;
import durel.services.dataManagement.fileTypeSpecifications.UseFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class ExtendedInstancesExportService extends UseFileType {

    /**
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(ExtendedInstancesExportService.class);

    private final PairService pairService;

    @Autowired
    public ExtendedInstancesExportService(PairService pairService) {
        super();
        this.pairService = pairService;
    }

    public String getHeader() {
        return super.getHeaderForComputationalAnnotators();
    }

    /**
     * Exports extended instances for a given Word object.
     *
     * @param lemma the Word object for which extended instances need to be exported
     * @return a list of extended instances as strings
     */
    public List<String> exportExtendedInstances(Lemma lemma) {
        List<String> extendedInstances = new ArrayList<>();
        if (!lemma.getProject().isAllPossiblePairs()) {
            Set<Instance> instances = new HashSet<>(pairService.getPairsByProjectAndLemma(lemma.getProject(), lemma.getLemma()));
            for (Instance instance : instances) {
                extendedInstances.add(processPair(instance.getUsePair().getUses(), lemma));
            }
        } else {
            List<Use> uses = new ArrayList<>(lemma.getUses());
            for (int i = 0; i< uses.size(); i++) {
                for (int j = i+1; j< uses.size(); j++) {
                    Set<Use> usePair = new HashSet<>();
                    usePair.add(uses.get(i));
                    usePair.add(uses.get(j));
                    extendedInstances.add(processPair(usePair, lemma));
                }
            }
        }
        return extendedInstances;
    }

    /**
     * Processes a pair of uses and a lemma to create an extended instance.
     *
     * @param usePair the pair of uses
     * @param lemma the lemma to be processed
     * @return the created extended instance
     */
    private String processPair(Set<Use> usePair, Lemma lemma) {
        try {
            String[] sentences = usePair.stream().map(u -> getDataContent(u, lemma)).flatMap(Arrays::stream).toArray(String[]::new);
            String[] extendedInstance = Stream.concat(Arrays.stream(new String[]{lemma.getLemma()}), Arrays.stream(sentences)).toArray(String[]::new);
            return createLine(extendedInstance);
        } catch (NullPointerException e) {
            logger.error("Unexpected error while creating file string from data. Incomplete data or null word.");
            return "";
        }
    }

    @Override
    protected String[] getDataContent(Use use, Lemma lemma) throws NullPointerException {
        super.getDataContent(use, lemma);
        return new String[]{use.getCsvId(), use.getContext(), use.getIndexesTargetToken()};
    }
}

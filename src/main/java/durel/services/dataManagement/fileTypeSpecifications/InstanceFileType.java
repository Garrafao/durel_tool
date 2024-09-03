package durel.services.dataManagement.fileTypeSpecifications;

import durel.services.dataManagement.uploadData.InstanceData;
import durel.domain.model.Instance;
import durel.domain.model.Use;
import durel.domain.model.Lemma;
import durel.services.dataManagement.upload.DefaultFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class InstanceFileType extends DefaultFileType<Instance, InstanceData> {

    private static final Logger instanceLogger = LoggerFactory.getLogger(InstanceFileType.class);
    private static final String instanceFileName = "instances.csv";
    private static final int instanceColumnNumber = 3;
    private static final String[] instanceContentColumns = {"lemma", "identifier1", "identifier2"};
    private static final String[] instanceSystemColumns = {"id", "identifier1_system", "identifier2_system"};

    public InstanceFileType() {
        super(instanceLogger, instanceFileName, instanceColumnNumber, instanceContentColumns, instanceSystemColumns);
    }

    // METHODS --------------------------------------------------------------------------------------

    /**
     * Retrieves the content data from a Pair and Word object.
     *
     * @param instance the Pair object
     * @param lemma the Word object associated with the annotation
     * @return an array of Strings representing the content data
     * @throws NullPointerException if the pair or word is null, or if any of the required fields in the pair are null
     */
    @Override
    protected String[] getDataContent(Instance instance, Lemma lemma) throws NullPointerException {
        super.getDataContent(instance, lemma);
        List<Use> uses = new ArrayList<>(instance.getUsePair().getUses());
        return getContentData(uses.get(0), uses.get(1), instance.getLemma());
    }

    /**
     * Retrieves the content data from two Sentence objects and a lemma.
     *
     * @param use1 the first Sentence object
     * @param use2 the second Sentence object
     * @param lemma     the lemma associated with the content data
     * @return an array of Strings representing the content data
     */
    private String[] getContentData(Use use1, Use use2, String lemma) {
        return new String[]{use1.getCsvId(),
                use2.getCsvId(),
                lemma,
                String.valueOf(use1.getId()) + use2.getId(),  // Ids are combined to create a unique instance id, see instanceSystemColumns above
                String.valueOf(use1.getId()),
                String.valueOf(use2.getId())};
    }

    @Override
    protected InstanceData stringToData(List<String> dataLine) throws IllegalArgumentException {
        String lemma = DefaultFileUpload.extractData(dataLine, 0);
        String identifier1 = DefaultFileUpload.extractData(dataLine, 1);
        String identifier2 = DefaultFileUpload.extractData(dataLine, 2);
        return new InstanceData(lemma, identifier1, identifier2);
    }

    @Override
    protected List<String> dataSetToStringList(Set<Instance> instanceSet, @NotNull Lemma lemma) {
        if (instanceSet != null && !instanceSet.isEmpty() && !lemma.getProject().isAllPossiblePairs()) {
            return super.dataSetToStringList(instanceSet, lemma);
        } else {
            List<Use> uses = new ArrayList<>(lemma.getUses());
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < uses.size() - 1 ; i++ ) {
                for (int j = i + 1; j < uses.size(); j++ ) {
                    // we use the counter to create a unique id, this unique id does not mean anything, it needs to be provided because our automatic annotation pipeline needs an identifier
                    String line = createLine(getContentData(uses.get(i), uses.get(j), lemma.getLemma()));
                    lines.add(line);
                }
            }
            return lines;
        }
    }

    /**
     * Validates a pair by checking if it is complete.
     *
     * @param instance the Pair object to validate
     * @param lemma the Word object associated with the annotation
     * @throws NullPointerException if the pair or word is null, or if any of the required fields in the pair are null
     */
    @Override
    protected void validateData(Instance instance, Lemma lemma) throws NullPointerException {
        if (lemma == null || instance == null ||
                instance.getUsePair().getUses().contains(null)) {
            throw new NullPointerException("Encountered an incomplete pair!");
        }
    }
}

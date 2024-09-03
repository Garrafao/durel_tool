package durel.services;

import durel.services.dataManagement.uploadData.PairedUploadData;
import durel.domain.model.Instance;
import durel.domain.model.Project;
import durel.domain.model.Use;
import durel.domain.repository.InstanceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PairService {

    private final InstanceDAO instanceDAO;

    @Autowired
    public PairService(InstanceDAO instanceDAO) {
        this.instanceDAO = instanceDAO;
    }

    public List<Instance> getPairsByProjectAndLemma(Project project, String word) {
        return instanceDAO.findByProjectAndLemma(project, word);
    }

    @Transactional
    public void deleteByProjectName(Project project) {
        instanceDAO.deleteByProject(project);
    }

    @Transactional
    public void deleteByProjectNameAndLemma(Project project, String lemma) {
        instanceDAO.deleteByProjectAndLemma(project, lemma);
    }

    public int countByProject(Project project) {
        return instanceDAO.countByProject(project);
    }

    public int countByProjectAndLemma(Project project, String lemma) {
        return instanceDAO.countByProjectAndLemma(project, lemma);
    }

    @Transactional
    public void createAndSavePairsFromPairedData(@NotNull List<PairedUploadData> pairDataList,
                                                 @NotNull Map<String, Use> idToSentence, @NotNull Project project) {
        Set<Instance> instances = new HashSet<>(pairDataList).stream()
                .map(pairData -> createPair(pairData, idToSentence, project))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        instanceDAO.saveAll(instances);
    }

    private Instance createPair(PairedUploadData pairData, Map<String, Use> idToSentence, Project project) {
        String lemma = pairData.getLemma();
        Use useOne = idToSentence.get(pairData.getIdentifierOne());
        Use useTwo = idToSentence.get(pairData.getIdentifierTwo());

        Instance instance = new Instance(useOne, useTwo);
        instance.setProject(project);
        instance.setLemma(lemma);
        if (project.getInstances().contains(instance)) {
            return null;
        }
        return instance;
    }
}

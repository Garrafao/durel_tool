package durel.services;

import durel.domain.model.UsePair;
import durel.services.dataManagement.uploadData.AnnotationData;
import durel.domain.model.annotation.GoldAnnotation;
import durel.domain.model.TutorialUse;
import durel.domain.repository.GoldAnnotationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class TutorialAnnotationService {

    private final GoldAnnotationDAO goldAnnotationDAO;

    @Autowired
    public TutorialAnnotationService(GoldAnnotationDAO goldAnnotationDAO) {
        this.goldAnnotationDAO = goldAnnotationDAO;
    }

    public GoldAnnotation getGoldAnnotationByIDs (int leftID, int rightID) {
        return goldAnnotationDAO.findById_Use1_IdAndId_Use2_Id(leftID, rightID);
    }

    @Transactional
    public void insertGoldAnnotation(AnnotationData goldAnnotation, TutorialUse leftSentence, TutorialUse rightSentence) {
        UsePair<TutorialUse> tutorialAnnotationId = new UsePair<>(leftSentence, rightSentence);
        GoldAnnotation tutorialAnnotation = new GoldAnnotation(tutorialAnnotationId, goldAnnotation.getJudgment());
        tutorialAnnotation.setComment(goldAnnotation.getComment());

        goldAnnotationDAO.save(tutorialAnnotation);
    }
}

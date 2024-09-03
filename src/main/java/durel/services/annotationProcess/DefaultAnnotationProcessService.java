package durel.services.annotationProcess;

import durel.dto.responses.UseDTO;
import durel.exceptions.SystemErrorException;
import durel.exceptions.UserErrorException;

import java.util.List;

public interface DefaultAnnotationProcessService {
    List<UseDTO> getNextUsePair();

    int getCurrentSequenceIndex();

    int noOfSentencePairs();

    void saveJudgment(float i, String comment) throws SystemErrorException, UserErrorException;
}

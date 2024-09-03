package durel.services.statistics;

import durel.session.AgreementStatisticsSessionData;
import durel.dto.responses.statistics.AgreementStatisticsData;
import durel.domain.model.User;
import durel.domain.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AgreementStatisticsSessionDataService {

    private final AgreementStatisticsSessionData agreementStatisticsSessionData;

    @Autowired
    public AgreementStatisticsSessionDataService(AgreementStatisticsSessionData agreementStatisticsSessionData) {
        this.agreementStatisticsSessionData = agreementStatisticsSessionData;
    }

    public boolean checkSessionData(List<User> annotators, Project project) {
        return agreementStatisticsSessionData.getAnnotators() != annotators
                || agreementStatisticsSessionData.getProject() != project
                || agreementStatisticsSessionData.getAgreementStatisticsDataMap() == null;
    }

    public void updateSessionData(List<User> annotators, Project project, Map<String, AgreementStatisticsData> agreementStatisticsDataMap) {
        this.agreementStatisticsSessionData.setAgreementStatisticsDataMap(agreementStatisticsDataMap);
        this.agreementStatisticsSessionData.setProject(project);
        this.agreementStatisticsSessionData.setAnnotators(annotators);
    }

    public double[][] getAgreementStatisticsDataMap(String word, String metric) {
        return agreementStatisticsSessionData.getAgreementStatisticsDataMap().get(word).getAgreementData().get(metric);
    }
}

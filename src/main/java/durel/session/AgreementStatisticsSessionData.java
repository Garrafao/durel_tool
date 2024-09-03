package durel.session;

import durel.dto.responses.statistics.AgreementStatisticsData;
import durel.domain.model.User;
import durel.domain.model.Project;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AgreementStatisticsSessionData {
    private List<User> annotators;
    private Project project;
    private Map<String, AgreementStatisticsData> agreementStatisticsDataMap = null;
}

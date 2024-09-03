package durel.session;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import durel.domain.model.Tutorial;

@NoArgsConstructor
@Getter
@Setter
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TutorialSessionData {

    private Tutorial currentTutorial;

    private int currentSentencePair;

    private int[] judgments;

    public void saveJudgment(int judgment) {
        if (currentSentencePair < judgments.length) {
            this.judgments[currentSentencePair] = judgment;
            currentSentencePair++;
        }
    }
}

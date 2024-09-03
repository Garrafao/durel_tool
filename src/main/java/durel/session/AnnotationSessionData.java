package durel.session;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import durel.domain.model.User;
import durel.domain.model.Project;
import durel.domain.model.AnnotationSequence;
import durel.domain.model.Lemma;

@NoArgsConstructor
@Getter
@Setter
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AnnotationSessionData {

    private AnnotationSequence currentAnnotationSequence;

    private Project currentProject;

    private Lemma currentLemma;

    private User currentAnnotator;

    private int currentFirstSentenceID;

    private int currentSecondSentenceID;

    private RandomAnnotationSequence randomAnnotationSequence;
}

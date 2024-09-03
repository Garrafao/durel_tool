package durel.domain.model.annotation;

import durel.domain.model.BaseUse;

import java.util.Set;

public interface DefaultAnnotation<U1 extends BaseUse, ID> {

    Float getJudgment();
    void setJudgment(Float judgment);

    String getComment();
    void setComment(String comment);

    Set<U1> getUses();

    ID getId();
}
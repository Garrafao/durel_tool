package durel.domain.model;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Slf4j
@Embeddable
public class UsePair<U1 extends BaseUse> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /***
     * The one {@link BaseUse} the use-pair.
     * <p>
     * This field maps to the {@code id} column in the {@code sentence} table.
     * It cannot be null. It is not directly insertable or updatable as it relies on existing sentence data.
     * Fetch type is set to LAZY to load the sentence entity only when it is accessed.
     * The field should only be accessed through {@link UsePair#getUses()}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "left_sentence_id", nullable = false, insertable = false, updatable = false)
    private U1 use1;

    /**
     * The other {@link BaseUse} of the use-pair.
     * <p>
     * This field maps to the {@code id} column in the {@code sentence} table.
     * It cannot be null. It is not directly insertable or updatable as it relies on existing sentence data.
     * Fetch type is set to LAZY to load the sentence entity immediately.
     * The field should only be accessed through {@link UsePair#getUses()}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "right_sentence_id", nullable = false, insertable = false, updatable = false)
    private U1 use2;

    protected UsePair() { }

    public UsePair(@NonNull U1 use1, @NonNull U1 use2) {
        if (use1.getId() == use2.getId()) {
            throw new IllegalArgumentException("Sentence IDs must not be the same.");
        }
        this.use1 = use1;
        this.use2 = use2;
    }

    public Set<U1> getUses() {
        return Set.of(use1, use2);
    }

    /**
     * Retrieves a set of sentence IDs involved in the use pair. As sentence IDs are mutable, only this method should
     * be used to access them from outside this class to ensure proper representation.
     *
     * @return a {@code Set} containing {@code sentence1ID} and {@code sentence2ID}.
     */
    public Set<Integer> getSentenceIDs() {
        if (use1 == null || use2 == null) {
            return Set.of();
        }
        return Set.of(use1.getId(), use2.getId());
    }

    @Override
    public String toString() {
        String use1Id = (use1 != null) ? String.valueOf(use1.getId()) : "null";
        String use2Id = (use2 != null) ? String.valueOf(use2.getId()) : "null";

        return String.format("%s(sentence1ID = %s, sentence2ID = %s)",
                getClass().getSimpleName(), use1Id, use2Id);
    }

    /**
     * Checks if this annotation ID is equal to another object.
     * <p>
     * Two UsePair objects are considered equal if
     * their sentence ID sets are equal.
     * </p>
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is equal to the reference object, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsePair<?> that = (UsePair<?>) o;
        return getUses() != null && getUses().equals(that.getUses());
    }

    @Override
    public int hashCode() {
        return getUses() != null ? getUses().hashCode() : 0;
    }
}

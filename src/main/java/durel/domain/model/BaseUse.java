package durel.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Slf4j
@Getter
@Setter
@MappedSuperclass
public abstract class BaseUse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the use.
     * <p>
     * This value is generated automatically and uniquely identifies the use entity.
     */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    /**
     * The example use of the target {@link Lemma}.
     * <p>
     * A short text fragment or sentence that contains the target lemma and that is used in the annotation process.
     * As the core element of the use, this field is non-nullable. Contexts can be up to 5000 characters long.
     */
    @Column(name = "context", nullable = false, length = 5000)
    private String context;

    /**
     * The indexes of the target token in the context.
     * <p>
     * This value represents the indexes of the target token in the context and must be non-null.
     * <p>
     *     Indexes must be strings of form {@code startIndex:endIndex}.
     */
    @Column(name = "indexes_target_token", nullable = false, length = 1000)
    private String indexesTargetToken;

    /**
     * The indexes of the target sentence in the context.
     * <p>
     * This value represents the indexes of the target sentence in the context and must be non-null.
     * <p>
     * Indexes must be strings of form {@code startIndex:endIndex}.
     */
    @Column(name = "indexes_target_sentence", nullable = false, length = 1000)
    private String indexesTargetSentence;
}

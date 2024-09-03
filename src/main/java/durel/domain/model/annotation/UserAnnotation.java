package durel.domain.model.annotation;

import durel.domain.model.Lemma;
import durel.domain.model.Use;
import durel.domain.model.UsePairAndAnnotator;
import durel.domain.model.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an annotation within the system, capturing the relationship between an annotator (of type {@link User}),
 * a pair of sentences (see {@link Use}) and a {@code judgment}.
 * <p>
 * This class is marked as an entity for persistence and contains various mappings to represent
 * its relational data, including many-to-one relationships to the annotator and sentences entities -
 * there can be multiple annotations per sentence and annotator.
 * It also contains an embedded ID (see {@link UsePairAndAnnotator}) which forms a composite key comprising the annotator's ID
 * and the two sentence IDs.
 * <p>
 * Lombok Annotations:
 * <ul>
 *   <li>{@code @NoArgsConstructor} - generates a no-argument constructor.</li>
 *   <li>{@code @Getter} - generates getters for fields in the class.</li>
 *   <li>{@code @Setter} - generates setters for fields in the class.</li>
 *   <li>{@code @Slf4j} - provides a logger field named 'log' in the class.</li>
 * </ul>
 */
@NoArgsConstructor
@Getter
@Setter
@Slf4j
@Entity
@Table(name = "annotation", schema = "public")
public class UserAnnotation implements Serializable, DefaultAnnotation<Use, UsePairAndAnnotator> {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Represents an embedded identifier for an annotation (see {@link UsePairAndAnnotator}).
	 * <p>
	 * The {@code id} variable is used as an embedded identifier for an annotation. It is annotated with {@code @EmbeddedId}
	 * and {@code @AttributeOverrides} to specify the mapping for each attribute of the identifier.
	 */
	@Setter(AccessLevel.NONE)
	@EmbeddedId
	private UsePairAndAnnotator id;

	/**
	 * The timestamp when the annotation was created.
	 * <p>
	 * This value is automatically set to the current date and time when the annotation is persisted.
	 * It cannot be null.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt", length = 35, nullable = false)
	@CreationTimestamp
	private Date dt;

	/**
	 * The judgment score of the annotation.
	 * <p>
	 * This value represents the judgment score given by the annotator.
	 * It cannot be null.
	 */
	@Column(name = "judgment", nullable = false)
	private Float judgment;

	private Lemma lemma;

	/**
	 * Additional comments provided by the annotator.
	 * <p>
	 * This value is optional and can contain any additional information or notes added by the annotator.
	 */
	@Column(name = "comment")
	private String comment;

	public UserAnnotation(UsePairAndAnnotator id, float judgment) {
		this.id = id;
		this.judgment = judgment;
	}

	public Set<Use> getUses() {
		return this.id.getPair().getUses();
	}

	public User getAnnotator() {
		return this.id.getAnnotator();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +
				"EmbeddedId = " + id + ", " +
				"judgment = " + judgment + ")";
	}

	/**
	 * Two Annotation objects are considered equal if they have the same id. This means that each use-pair can only
	 * be annotated once by each annotator. Subsequent annotations will overwrite the original annotation.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		UserAnnotation that = (UserAnnotation) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}

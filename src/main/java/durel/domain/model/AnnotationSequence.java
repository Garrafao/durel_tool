package durel.domain.model;

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

/**
 * Represents an annotation sequence entity related to a word ({@link Lemma}) and a {@link User}.
 * <p>
 * Holds information about the progress of the annotation for this annotator and lemma. Also contains the
 * necessary information to reproduce the annotation sequence.
 * <p>
 * Lombok Annotations:
 * <ul>
 *   <li>{@code @NoArgsConstructor} - Generates a no-argument constructor.</li>
 *   <li>{@code @Getter} - Generates getters for all fields.</li>
 *   <li>{@code @Setter} - Generates setters for all fields.</li>
 *   <li>{@code @Slf4j} - Provides a logger field named 'log' in the class.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@Slf4j
@Entity
@Table(name = "seq", schema = "public")
public class AnnotationSequence implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The composite primary key for the sequence entity.
	 * <p>
	 * This field is an embedded ID {@link AnnotationSequenceId} that consists of multiple attributes.
	 */
	@Setter(AccessLevel.NONE)
	@EmbeddedId
	private AnnotationSequenceId id;

	/**
	 * The {@link User} (annotator) associated with this sequence.
	 * <p>
	 * This field references the user who created the sequence and whose annotation progress is stored in this sequence.
	 * It cannot be null.
	 * Fetch type is set to LAZY to load the user entity only when it is accessed.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("username")
	@JoinColumn(name = "annotator_id", nullable = false, insertable = false, updatable = false)
	private User user;

	/**
	 * The {@link Lemma} (word) associated with this sequence.
	 * <p>
	 * This field references the lemma to which the sequence belongs. It cannot be null.
	 * Fetch type is set to LAZY to load the lemma entity only when it is accessed.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("lemmaId")
	@JoinColumn(name = "word_id", nullable = false, insertable = false, updatable = false)
	private Lemma lemma;

	/**
	 * The creation timestamp of the sequence.
	 * <p>
	 * This value represents the date and time when the sequence was created.
	 * The creation timestamp is automatically set.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt", length = 35)
	@CreationTimestamp
	private Date dt;

	/**
	 * The seed value associated with the sequence.
	 * <p>
	 * This value represents the random seed used for the annotation sequence generation and must be non-null.
	 */
	@Column(name = "seed", nullable = false)
	private long seed;

	/**
	 * The index value for the sequence.
	 * <p>
	 * This value represents the index in the sequence, i.e., the current annotation progress and must be non-null.
	 */
	@Column(name = "idx", nullable = false)
	private int index;

	public AnnotationSequence(AnnotationSequenceId id, User user, Lemma lemma, int index, long seed) {
		this.id = id;
		this.user = user;
		this.lemma = lemma;
		this.index = index;
		this.seed = seed ;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		AnnotationSequence annotationSequence = (AnnotationSequence) o;
		return getId() != null && Objects.equals(getId(), annotationSequence.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +
				"EmbeddedId = " + id + ", " +
				"idx = " + index + ")";
	}
}

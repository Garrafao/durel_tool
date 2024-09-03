package durel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the composite key for an annotation entity.
 * <p>
 * This class is used as an embedded ID for annotations that couples
 * information about the annotator and the sentence pair being annotated.

 * <p>
 * Lombok Annotations:
 * <ul>
 *   <li>{@code @NoArgsConstructor} - generates a no-argument constructor.</li>
 *   <li>{@code @Slf4j} - provides a logger field named 'log' in the class.</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
@Slf4j
@Embeddable
public class UsePairAndAnnotator implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The {@link User} who made the annotation.
	 * <p>
	 * This field maps to the {@code username} ({@link User#getUsername()}) column in the {@code annotator} table.
	 * It cannot be null. It is not directly insertable or updatable as it relies on existing user data.
	 * Fetch type is set to EAGER to load the annotator entity immediately.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "annotator_id", nullable = false, insertable = false, updatable = false)
	private User annotator;

	@Embedded
	private UsePair<Use> pair;

	/**
	 * Constructs a new {@code AnnotationID} with the specified annotator ID, sentence 1 ID, and sentence 2 ID.
	 *
	 * @param annotatorId the ID of the annotator.
	 * @throws IllegalArgumentException if the left sentence ID is equal to the right sentence ID.
	 */
	public UsePairAndAnnotator(User annotatorId, @NonNull Use use1, @NonNull Use use2) {
		this.pair = new UsePair<>(use1, use2);
		this.annotator = annotatorId;
	}

	@Override
	public String toString() {
		return String.format("%s(annotatorID = %s, %s)",
				getClass().getSimpleName(), annotator.getUsername(), super.toString());
	}

	/**
	 * Checks if this annotation ID is equal to another object.
	 * <p>
	 * Two AnnotationID objects are considered equal if they have the same
	 * {@code annotatorID} and their sentence ID sets are equal.
	 * </p>
	 *
	 * @param o the reference object with which to compare.
	 * @return {@code true} if this object is equal to the reference object, {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		UsePairAndAnnotator that = (UsePairAndAnnotator) o;
		return (annotator.getUsername() != null && annotator.getUsername().equals(that.annotator.getUsername())
				&& this.getPair().equals(that.getPair()));
	}

	@Override
	public int hashCode() {
		return Objects.hash(annotator.getUsername(), pair.getSentenceIDs());
	}
}

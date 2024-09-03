package durel.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the composite primary key for the {@link AnnotationSequence} entity.
 * <p>
 * This class is used to create a composite key consisting of {@link AnnotationSequenceId#lemmaId} and {@link AnnotationSequenceId#username}.
 * <p>
 * Lombok Annotations:
 * <ul>
 *   <li>{@code @NoArgsConstructor} - Generates a no-argument constructor.</li>
 *   <li>{@code @Getter} - Generates getters for all fields.</li>
 *   <li>{@code @Setter} - Generates setters for all fields.</li>
 *   <li>{@code @Slf4j} - Provides a logger field named 'log' in the class.</li>
 * </ul>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
@Embeddable
public class AnnotationSequenceId implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The unique {@link Lemma#getId()} of the word (lemma).
	 * <p>
	 * This field is part of the composite key and represents the ID of the lemma that is annotated in the sequence.
	 */
	private int lemmaId;

	/**
	 * The unique {@link User#getUsername()} of the annotator.
	 * <p>
	 * This field is part of the composite key and represents the username of the annotator.
	 * The length of this field is limited to 15 characters.
	 */
	private String username;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		AnnotationSequenceId annotationSequenceId = (AnnotationSequenceId) o;
		return getLemmaId() == annotationSequenceId.getLemmaId()
				&& getUsername() != null && getUsername().equals(annotationSequenceId.getUsername());
	}

	@Override
	public int hashCode () {
		return Objects.hash(lemmaId, username);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +
				"wordId = " + lemmaId + ", " +
				"annotatorId = " + username + ")";
	}
}

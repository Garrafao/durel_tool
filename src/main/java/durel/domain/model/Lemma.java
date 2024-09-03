package durel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Lemma (word) entity.
 * <p>
 * Holds information about a lemma, its associated {@link Project}, and its {@link Use}s and {@link AnnotationSequence}.
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
@Getter
@Setter
@Slf4j
@Entity
@Table(name = "word", schema = "public")
public class Lemma implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The unique identifier of the lemma.
	 * <p>
	 * This value is generated automatically and uniquely identifies the lemma entity.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int id;

	/**
	 * The lemma itself.
	 * <p>
	 * This value represents the lemma and must be non-null. Lemmas must not be longer than 40 characters.
	 * <p>
	 * Lemma and project can be used to uniquely identify a Lemma
	 * (see {@link durel.services.WordService#getLemmaObjectByProjectNameAndLemma(String, String)}).
	 */
	@Column(name = "word", nullable = false, length = 40)
	private String lemma;

	/**
	 * The {@link Project} that the lemma belongs to.
	 * <p>
	 * This field references the project to which the lemma belongs. It cannot be null.
	 * Fetch type is set to LAZY to load the project entity only when it is accessed.
	 * <p>
	 * {@link Lemma#lemma} and project can be used to uniquely identify a Lemma
	 * (see {@link durel.services.WordService#getLemmaObjectByProjectNameAndLemma(String, String)}).
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	/**
	 * The {@link Use} instances of the lemma.
	 * <p>
	 * This field references a set of uses where the lemma appears.
	 * Fetch type is set to LAZY to load the use entities only when they are accessed.
	 * The relationship is cascaded for all operations such that all uses that belong to the lemma will be changed if
	 * the lemma is.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "lemma")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<Use> uses = new HashSet<>();

	/**
	 * The annotation {@link AnnotationSequence} of the lemma.
	 * <p>
	 * There is an annotations sequence for each {@link User} that has started annotating a lemma.
	 * This field references the set of sequences that have been started for this lemma.
	 * Fetch type is set to LAZY to load the seq entities only when they are accessed.
	 * The relationship is cascaded for all operations such that annotation sequences cannot be continued after the
	 * lemma has been deleted.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "lemma")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<AnnotationSequence> annotationSequences = new HashSet<>();

	/**
	 * Adds a {@link Use} instance to the lemma's set of uses.
	 *
	 * @param use A {@code Use} instance to be added to the uses set.
	 */
	public void addSentence(Use use) {
		this.uses.add(use);
	}

	/**
	 * Removes a {@link Use} instance from the lemma's set of uses.
	 *
	 * @param use A {@code Use} instance to be removed from the uses set.
	 */
	public void removeUse(Use use) {
		this.uses.remove(use);
	}

	/**
	 * Adds a {@link AnnotationSequence} instance to the lemma's set of sequences.
	 *
	 * @param annotationSequence A {@code Seq} instance to be added to the sequences set.
	 */
	public void addSequence(AnnotationSequence annotationSequence) {
		this.annotationSequences.add(annotationSequence);
	}

	/**
	 * Removes a {@link AnnotationSequence} instance from the lemma's set of sequences.
	 *
	 * @param annotationSequence A {@code Seq} instance to be removed from the sequences set.
	 */
	public void removeSequence(AnnotationSequence annotationSequence) {
		this.annotationSequences.remove(annotationSequence);
	}

	/**
	 * Constructs a new {@code Lemma} instance with the specified project and word.
	 *
	 * @param project The {@link Project} associated with the lemma.
	 * @param lemma The word represented by this {@code Lemma} instance.
	 */
	public Lemma(Project project, String lemma) {
		this.project = project;
		this.lemma = lemma;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Lemma lemma = (Lemma) o;
		return getId() == lemma.getId();
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +
				"id = " + id + ", " +
				"word = " + lemma + ")";
	}
}

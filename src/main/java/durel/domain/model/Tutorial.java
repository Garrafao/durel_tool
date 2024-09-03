package durel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a tutorial entity.
 * <p>
 * Holds information about a tutorial, including its creation timestamp, associated sentences, annotators, and language.
 * @implNote Tutorials have a similar purpose as {@link Project} entities. However, the fields of both classes are
 * quite different because tutorials don't distinguish by {@link Lemma}, and because no annotations are stored.
 * @implNote There can only be one tutorial per {@link Language}. This could be changed but would imply changes to
 * multiple services, as well as to the frontend.
 * </p>
 *
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
@Table(name = "tutorial", schema = "public")
public class Tutorial implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The unique identifier of the tutorial.
	 * <p>
	 * This value is generated automatically and uniquely identifies the tutorial entity.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int id;

	/**
	 * The creation timestamp of the tutorial.
	 * <p>
	 * This value represents the date and time when the tutorial was created.
	 * The creation timestamp is automatically set.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt", length = 35)
	@CreationTimestamp
	private Date dt;

	/**
	 * The list of sentences associated with the tutorial.
	 * <p>
	 * This field references the {@link TutorialUse} entities that are part of the tutorial.
	 * Fetch type is set to LAZY to load the sentences only when needed.
	 * Cascade operations are applied to all cascade types to update the sentences, whenever the tutorial is updated.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tutorial")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<TutorialUse> tutorialUses = new ArrayList<>();

	/**
	 * The set of annotators that have completed the tutorial.
	 * <p>
	 * This field references the {@link User} entities that are annotators of the tutorial.
	 * Fetch type is set to LAZY to load the annotators only when needed.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tutorial")
	private Set<User> annotators = new HashSet<>(0);

	/**
	 * The language associated with the tutorial.
	 * <p>
	 * This field references the {@link Language} entity that represents the language of the tutorial.
	 */
	@OneToOne
	@JoinColumn(name = "lang")
	private Language lang;

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Tutorial tutorial = (Tutorial) o;
		return id == tutorial.id || (lang != null && lang.equals(tutorial.lang));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +
				"lang = " + lang + ", " +
				"dt = " + dt + ")";
	}
}

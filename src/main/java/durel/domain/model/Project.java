package durel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import javax.persistence.*;

/**
 * Represents a project entity.
 * <p>
 * This entity holds information about different projects, including their name, creator, creation timestamp,
 * visibility, and related words, annotators, and language.
 * Each project can also be associated with multiple word pairs.
 * </p>
 *
 * <p>
 * Lombok Annotations:
 * <ul>
 *   <li>{@code @NoArgsConstructor} - generates a no-argument constructor.</li>
 *   <li>{@code @AllArgsConstructor} - generates an all-arguments constructor.</li>
 *   <li>{@code @Getter} - generates getters for all fields.</li>
 *   <li>{@code @Setter} - generates setters for all fields.</li>
 *   <li>{@code @Slf4j} - provides a logger field named 'log' in the class.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@Slf4j
@Entity
@Table(name = "project", schema = "public")
public class Project implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The unique name of the project.
	 * <p>
	 * This value is a unique identifier for the project and must be non-null. Project names must not be longer
	 * than 40 characters.
	 */
	@Id
	@Column(name = "projectname", unique = true, nullable = false, length = 40)
	private String projectName;

	/**
	 * The {@link User} who created the project.
	 * <p>
	 * This field references the {@code annotator_id} column of the {@code annotator} table and cannot be null.
	 * Fetch type is set to LAZY to load the user entity only when it is accessed.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "annotator_id", nullable = false)
	private User creator;

	/**
	 * The timestamp when the project was created.
	 * <p>
	 * This value is automatically set to the current date and time when the project is persisted.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt", length = 35)
	@CreationTimestamp
	private Date dt;

	// TODO add field last updated

	/**
	 * Indicates whether the project's {@link Project#instances} have been generated without restrictions.
	 * <p>
	 * This boolean value specifies how the project's {@link Project#instances} were created.
	 * <ul>
	 *     <li>if true, all possible pairs were generated from the uses upon project creation</li>
	 *     <li>if false, some kind of specification was given when the pairs were created
	 *     (either as in an {@code instances.csv} file or through sampling options)</li>
	 * </ul>
	 */
	@Column(name = "random")
	private boolean isAllPossiblePairs;

	/**
	 * Indicates whether the project is public.
	 * <p>
	 * This boolean value specifies whether the project is public (visible to all users) and must be non-null.
	 */
	@Column(name = "visible", nullable = false)
	private boolean isPublic;

	/**
	 * The {@link Lemma}s associated with the project.
	 * <p>
	 * This field references the set of lemmas that belong to the project.
	 * Fetch type is set to LAZY to load the word entities only when they are accessed.
	 * The relationship is cascaded for all operations because lemmas depend on projects.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	@Cascade(CascadeType.ALL)
	private Set<Lemma> lemmas = new HashSet<>();

	/**
	 * The {@link User}s who can view the project.
	 * <p>
	 * This field references a set of users who have been given access to the project to annotate it.
	 * Fetch type is set to EAGER to load the user entities immediately.
	 * The relationship is managed through the 'project_visibility' join table.
	 * @implNote Currently, DURel does not distinguish between project annotators and project collaborators.
	 * Theoretically, collaborators should have more rights, for example to see the statistics. This is not the
	 * case with the current implementation.
	 */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "project_visibility", schema = "public", joinColumns = {
			@JoinColumn(name = "project_id", nullable = false, updatable = false) }, inverseJoinColumns = {
			@JoinColumn(name = "annotator_id", nullable = false, updatable = false) })
	private Set<User> annotators = new HashSet<>();

	/**
	 * The {@link Language} associated with the project.
	 * <p>
	 * This field references the language used in the project.
	 * It must be unique.
	 */
	@ManyToOne
	@JoinColumn(name = "lang", unique = true)
	private Language language;

	/**
	 * The use-pairs associated with the project (see {@link Instance}).
	 * <p>
	 * This field will be updated when a word is added to or deleted from the project.
	 * This field exists to ensure better accessibility of <b>all</b> use-pairs belonging to
	 * a project (they can also be accessed through {@link Project#lemmas}).
	 * The relationship is cascaded for all operations.
	 */
    @OneToMany(mappedBy = "project")
	@Cascade(CascadeType.ALL)
    private Set<Instance> instances = new LinkedHashSet<>();

	/**
	 * Constructs a new Project with the specified parameters.
	 *
	 * @param projectName the unique name of the project.
	 * @param creator the user who created the project.
	 * @param language the language used in the project.
	 * @param isPublic whether the project is publicly visible.
	 * @param isAllPossiblePairs whether the project includes all possible use-pairs.
	 */
    public Project(String projectName, User creator, Language language, boolean isPublic, boolean isAllPossiblePairs) {
		this.projectName = projectName;
		this.creator = creator;
		this.language = language;
		this.isPublic = isPublic;
		this.isAllPossiblePairs = isAllPossiblePairs;
	}

	/**
	 * Adds a {@link Lemma} to the project's lemma set.
	 *
	 * @param lemma the lemma to be added to the project's word set.
	 */
	public void addLemma(Lemma lemma) {
		this.lemmas.add(lemma) ;
	}

	/**
 	* Adds a {@link User} from the set of annotators who can view the project.
 	*/
	public void addAnnotator(User annotator) {
		this.annotators.add(annotator);
		annotator.getVisibleProjects().add(this);
	}

	/**
	 * Removes a {@link User} from the set of annotators who can view the project.
	 */
	public void removeAnnotator(User annotator) {
		this.annotators.remove(annotator);
		annotator.getVisibleProjects().remove(this);
	}

	/**
	 * Two Project objects are considered equal if they have the same {@link Project#projectName}.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Project project = (Project) o;
		return getProjectName() != null && Objects.equals(getProjectName(), project.getProjectName());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +
				"projectName = " + projectName + ", " +
				"visible = " + isPublic + ", " +
				"lang = " + language + ")";
	}
}

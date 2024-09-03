package durel.domain.model;

import durel.domain.model.annotation.UserAnnotation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Use entity for a specific {@link Lemma}.
 * <p>
 * Each use consists of a {@link BaseUse#getContext()} and indexes that identify the target sentence and target token within said use.
 * Additional information, such as the part of speech of the target token, the original use date, a description and
 * an external csvID can also be given. Each use can be assigned to a grouping for sampling or processing purposes.
 * <p>
 * Lombok Annotations:
 * <ul>
 *   <li>{@code @NoArgsConstructor} - generates a no-argument constructor.</li>
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
@Table(name = "sentence", schema = "public")
public class Use extends BaseUse implements Serializable {

	/**
	 * The {@link Lemma} that the use belongs to.
	 * <p>
	 * Each use belongs to a lemma - namely the lemma of the target token.
	 * It cannot be null. If a lemma is deleted all its uses are deleted, as well.
	 * Fetch type is set to EAGER to load the lemma entity immediately.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "word_id", nullable = false)
	private Lemma lemma;

	/**
	 * The part of speech of the target token in the use {@link BaseUse#getContext()}.
	 * This value must be non-null. There are no restrictions with respect to the POS-labels that should be used
	 * except that no POS must be longer than 20 characters.
	 */
	@Column(name = "pos", nullable = false, length = 20)
	private String pos;

	/**
	 * The date of publication of the use {@link BaseUse#getContext()} .
	 * <p>
	 * This value represents the date when the context was published/posted and is optional.
	 */
	@Column(name = "sentence_date", length = 20)
	private String useDate;

	/**
	 * A string to group uses of a lemma.
	 * <p>
	 * Sub-groupings of uses in the dataset can be created using the grouping variable. This field can mainly be
	 * used for sampling and to selectively query the database. Groupings are optional and there are no restrictions
	 * with respect to what is encoded in this variable.
	 */
	@Column(name = "grouping", length = 1000)
	private String grouping;

	/**
	 * The ID in the upload files.
	 * <p>
	 * The csv ID of the use, i.e., the ID that the use was given in the upload files. The csv ID is expected to be
	 * present on upload and is non-nullable. In front-end applications the csv ID is shown preferentially to allow
	 * the users to link the uses to uses in their upload files.
	 */
	@Column(name = "csv_id", length = 1000, nullable = false)
	private String csvId;

	/**
	 * An optional short description that contains more information on the use.
	 * <p>
	 * Descriptions can be up to 200 characters long.
	 */
	@Column(name = "description", length = 200)
	private String description;

	/**
	 * The set of {@link UserAnnotation}s where this use is use1.
	 * <p>
	 * This field references a set of annotations where this use one of the annotated uses.
	 * Fetch type is set to LAZY to load the annotation entities only when they are accessed.
	 * The relationship is cascaded for all operations.
	 */
	@Getter(AccessLevel.NONE)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "id.pair.use1")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<UserAnnotation> annotationsAsUse1 = new HashSet<>();

	/**
	 * The set of {@link UserAnnotation}s where this use is use2.
	 * <p>
	 * This field references a set of annotations where this use one of the annotated uses.
	 * Fetch type is set to LAZY to load the annotation entities only when they are accessed.
	 * The relationship is cascaded for all operations.
	 */
	@Getter(AccessLevel.NONE)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "id.pair.use2")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<UserAnnotation> annotationsAsUse2 = new HashSet<>();

	/**
	 * Constructor used for the creation of new projects.
	 * Creates a new instance of the Use class with the given parameters.
	 *
	 * @param lemma The Lemma associated with the target token of the Use.
	 * @param pos The part of speech of the Use.
	 * @param useDate The date when the Use was published.
	 * @param grouping The grouping of the Use.
	 * @param csvId The ID of the Use in the CSV file.
	 * @param description The description of the Use.
	 * @param context The context of the Use.
	 * @param indexesTargetToken The indexes of the target token in the {@link BaseUse#getContext()} .
	 * @param indexesTargetSentence The indexes of the target sentence in the {@link BaseUse#getContext()} .
	 */
	public Use(Lemma lemma, String pos, String useDate, String grouping, String csvId, String description, String context, String indexesTargetToken, String indexesTargetSentence) {
		this.lemma = lemma;
		this.pos = pos;
		this.useDate = useDate;
		this.grouping = grouping;
		this.csvId = csvId;
		this.description = description;
		this.setContext(context);
		this.setIndexesTargetToken(indexesTargetToken);
		this.setIndexesTargetSentence(indexesTargetSentence);
	}

	public Set<UserAnnotation> getAnnotations() {
		Set<UserAnnotation> mergedAnnotations = new HashSet<>(annotationsAsUse1);
		mergedAnnotations.addAll(annotationsAsUse2);
		return mergedAnnotations;
	}

	@Override
	public String toString() {
		return "Sentence{" +
				"id=" + getId() +
				", pos='" + pos + '\'' +
				", grouping='" + grouping + '\'' +
				", csvId='" + csvId + '\'' +
				", description='" + description + '\'' +
				'}';
	}

	/**
	 * Two Use objects are considered equal if they have the same id and the same csv ID.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Use use = (Use) o;
		return getId() == use.getId() && getCsvId().equals(use.getCsvId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}

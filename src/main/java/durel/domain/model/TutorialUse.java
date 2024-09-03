package durel.domain.model;
import durel.domain.model.annotation.GoldAnnotation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
@Entity
@Table(name = "tutorial_sentence", schema = "public")
public class TutorialUse extends BaseUse implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tutorial_id", nullable = false)
	private Tutorial tutorial;

	@Column(name = "word", nullable = false, length = 40)
	private String word;

	@Column(name = "pair_id", nullable = false)
	private int pairId;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "id.use2")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<GoldAnnotation> tutorialGoldAnnotationsForRightTutorialSentenceId = new HashSet<>();
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "id.use1")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<GoldAnnotation> tutorialGoldAnnotationsForLeftTutorialSentenceId = new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		TutorialUse that = (TutorialUse) o;
		return Objects.equals(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +
				"id = " + getId() + ", " +
				"word = " + word + ", " +
				"pairId = " + pairId + ")";
	}
}

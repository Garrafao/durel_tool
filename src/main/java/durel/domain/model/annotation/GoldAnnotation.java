package durel.domain.model.annotation;

import durel.domain.model.TutorialUse;
import durel.domain.model.UsePair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Slf4j
@Entity
@Table(name = "tutorial_annotation", schema = "public")
public class GoldAnnotation implements Serializable, DefaultAnnotation<TutorialUse, UsePair<TutorialUse>> {
	
	@Serial
	private static final long serialVersionUID = 1L;

	@Setter(AccessLevel.NONE)
	@EmbeddedId
	private UsePair<TutorialUse> id;

	@Column(name = "vote", nullable = false)
	private Float judgment;

	@Column(name = "observation")
	private String comment;

	public GoldAnnotation(UsePair<TutorialUse> id, float judgment) {
		this.id = id;
		this.judgment = judgment;
	}

	public Set<TutorialUse> getUses() {
		return this.id.getUses();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		GoldAnnotation that = (GoldAnnotation) o;
		return getId() != null && Objects.equals(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +
				"EmbeddedId = " + id + ", " +
				"judgment = " + judgment + ")";
	}
}

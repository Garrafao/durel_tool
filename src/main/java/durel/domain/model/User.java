package durel.domain.model;

import durel.domain.model.annotation.UserAnnotation;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Slf4j
@Entity
@Table(name = "annotator", schema = "public")
public class User implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "username", unique = true, nullable = false, length = 20)
	private String username;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tutorial_id")
	private Tutorial tutorial;

	@Column(name = "tutorial_agreement")
	private Double tutorialAgreement;

	@Column(name = "passwd", nullable = false, length = 60)
	private String password;

	@Column(name = "role", nullable = false)
	@Enumerated(EnumType.STRING)
	private Role userRole;

	@Column(name = "email_address", nullable = false, length = 60)
	private String email;

	@Column(name = "other")
	private String other;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "registration_dt", length = 35)
	@CreationTimestamp
	private Date registrationDt;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "creator")
	private Set<Project> ownedProjects = new HashSet<>(0);

	@ManyToMany(mappedBy = "annotators", fetch = FetchType.LAZY)
	private Set<Project> visibleProjects = new HashSet<>(0);

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
	private Set<AnnotationSequence> annotationSequences = new HashSet<>(0);

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "id.annotator", cascade = CascadeType.ALL)
	private Set<UserAnnotation> userAnnotations = new HashSet<>(0);

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "creator")
	private Set<ComputationalAnnotationTask> computationalAnnotationTasks;

	public User(String username, String password, Role userRole, String email, String other) {
		this.username = username;
		this.password = password;
		this.userRole = userRole;
		this.email = email;
		this.other = other;
	}

	public enum Role {
		ADMIN, ANNOTATOR, RESEARCHER, OTHER, DRAFT, CANNOTATOR
	}

	@Override
	public String toString() {
		return "Annotator [username=" + username + ", tutorial=" + tutorial + ", role=" + userRole
				+ ", emailAddress=" + email
				+ ", other=" + other + ", registrationDt=" + registrationDt + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		User user = (User) o;
		return getUsername() != null && Objects.equals(getUsername(), user.getUsername());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}

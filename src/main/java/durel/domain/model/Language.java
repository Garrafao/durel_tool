package durel.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a language entity.
 * <p>
 * This entity holds information about different languages, including their code, name, and locale status.
 * Each language can be associated with one tutorial and multiple projects.
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
@AllArgsConstructor
@Slf4j
@Entity
@Table(name = "languages", schema = "public")
public class Language implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The unique code of the language.
     * <p>
     * This value is a two-character code (<a href="https://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>) that uniquely
     * identifies the language. It cannot be null.
     */
    @Id
    @Column(name="language_code", unique = true, nullable = false, length = 2)
    private String code;

    /**
     * The name of the language.
     * <p>
     * This value represents the name of the language and must be unique and cannot be null. Language names can be
     * both proper names and English names. Language names must not exceed 25 characters.
     */
    @Column(name = "language_name", unique = true, nullable = false, length = 25)
    private String name;

    /**
     * Indicates whether the language is a locale.
     * <p>
     * This boolean value specifies whether the language is used as a locale (even if only for one page).
     * It cannot be null.
     */
    @Column(name = "locale", nullable = false)
    private boolean isLocale;

    /**
     * The tutorial associated with the language.
     * <p>
     * This field references a tutorial that uses this language.
     * It is mapped by the {@code lang} field in the {@link Tutorial} entity.
     */
    @OneToOne(mappedBy = "lang")
    private Tutorial tutorial;

    /**
     * The list of projects associated with the language.
     * <p>
     * This field references a list of projects that use this language.
     * Projects are ordered by their name. The relationship is cascaded for all operations.
     */
    @OneToMany(mappedBy = "language", cascade = CascadeType.ALL)
    @OrderBy("projectName")
    private List<Project> projects = new ArrayList<>();

    /**
     * Two Language objects are considered equal if they have the same language code.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Language language = (Language) o;
        return getCode() != null && Objects.equals(getCode(), language.getCode());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "code = " + code + ", " +
                "name = " + name + ")";
    }
}

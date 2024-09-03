package durel.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a computational annotation task entity.
 * <p>
 * Holds information about the tasks such as the status, the selected computational annotator, project name,
 * and other task-specific details.
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
@Table(name = "task", schema = "public")
public class ComputationalAnnotationTask implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the computational annotation task.
     * <p>
     * This value is generated automatically and uniquely identifies the task entity.
     */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    /**
     * The current status of the task.
     * <p>
     * This value represents the status of the task and must be non-null.
     */
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * The selected annotator performing the task.
     * <p>
     * This value represents the type of annotator and must be non-null.
     */
    @Column(name = "annotator_type", nullable = false)
    private String selectedCompAnnotator;

    /**
     * The name of the project that is to be annotated.
     * <p>
     * This value represents the project name and must be non-null.
     */
    @Column(name = "project_name", nullable = false)
    private String projectName;

    /**
     * The lemma that is to be annotated.
     * <p>
     * This value represents the lemma and can be null if the whole project should be annotated.
     * </p>
     */
    @Column(name = "word")
    private String lemma;

    /**
     * The threshold values used to determine the return values.
     * <p>
     * This String represents the threshold values that are used to determine the annotations
     * in binary [1,4] and four-way annotations [1,2,3,4].
     * Can be null if cosine values should be returned.
     * <p>
     * Threshold values should be a comma separated list {@code "0.2,0.5,0.7"} of one or three values between 0 and 1.
     * See {@link ComputationalAnnotationTask#thresholdValuesToString(List)}
     * and {@link ComputationalAnnotationTask#thresholdValuesToList()}
     */
    @Column(name = "threshold_values")
    private String thresholdValues;

    /**
     * The total number of batches for the task.
     * <p>
     * Can be used to evaluate the progress of the annotation task.
     * This value is ignored during serialization and deserialization.
     */
    @JsonIgnore
    @Column(name = "total_batches")
    private int total_batches;

    /**
     * The number of completed batches for the task.
     * <p>
     * Can be used to evaluate the progress of the annotation task.
     * This value is ignored during serialization and deserialization.
     */
    @JsonIgnore
    @Column(name = "completed_batches")
    private int completed_batches;

    /**
     * The {@link User} who created the task.
     * <p>
     * This field references the user who created the task and cannot be null.
     * Fetch type is set to LAZY to load the user entity only when it is accessed.
     * This field is ignored during serialization and deserialization.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private User creator;

    /**
     * The creation timestamp of the task.
     * <p>
     * This value represents the date and time when the task was created.
     * The creation timestamp is automatically set.
     * This field is ignored during serialization and deserialization.
     */
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt")
    @CreationTimestamp
    private Date dt;

    public ComputationalAnnotationTask(String status, String annotatorType, String projectName, String word,
                                       User user, List<Float> thresholdValues) {
        this.status = status;
        this.selectedCompAnnotator = annotatorType;
        this.projectName = projectName;
        this.lemma = word;
        this.creator = user;
        this.thresholdValues = thresholdValuesToString(thresholdValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ComputationalAnnotationTask computationalAnnotationTask = (ComputationalAnnotationTask) o;
        return Objects.equals(getId(), computationalAnnotationTask.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "status = " + status + ", " +
                "annotatorType = " + selectedCompAnnotator + ", " +
                "projectName = " + projectName + ", " +
                "word = " + lemma + ", " +
                "dt = " + dt + ")";
    }

    /**
     * Converts a list of {@code Float} threshold values to a comma-separated {@code String}.
     *
     * @param thresholdValues A list of threshold values to be converted.
     * @return A {@code String} containing the comma-separated threshold values.
     */
    private String thresholdValuesToString(List<Float> thresholdValues) {
        return thresholdValues.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * Converts the comma-separated {@code String} of threshold values to a list of {@code Float}.
     *
     * @return A list of {@code Float} representing the threshold values.
     */
    public List<Float> thresholdValuesToList() {
        return Arrays.stream(thresholdValues.split(","))
                .map(Float::valueOf)
                .collect(Collectors.toList());
    }
}

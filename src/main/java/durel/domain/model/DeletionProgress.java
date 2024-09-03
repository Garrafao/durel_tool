package durel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deletion", schema = "public")
public class DeletionProgress implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)private int id;

    @Column(name = "project", nullable = false)
    private String entityName;

    @Column(name = "progress", nullable = false)
    private String progress;

    @Column(name = "creator", nullable = false)
    private String creator;

    public DeletionProgress(String entityName, String progress, String creator) {
        this.entityName = entityName;
        this.progress = progress;
        this.creator = creator;
    }

}

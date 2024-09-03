package durel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "upload", schema = "public")
public class UploadProgress implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Column(name = "project_name", nullable = false)
    private String project;

    @Column(name = "progress", nullable = false)
    private String progress;

    @Column(name = "creator", nullable = false)
    private String creator;

    public UploadProgress(String project, String progress, String creator) {
        this.project = project;
        this.progress = progress;
        this.creator = creator;
    }

}

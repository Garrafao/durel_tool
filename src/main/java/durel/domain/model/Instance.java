package durel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@Slf4j
@Entity
@Table(name = "pair", schema = "public")
public class Instance implements Serializable{
    
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Column(name = "lemma", nullable = false)
    private String lemma;

    @Embedded
    private UsePair<Use> usePair;

    @ManyToOne(optional = false)
    @JoinColumn(name = "projectname", nullable = false)
    private Project project;

    public Instance(Use identifierOne, Use identifierTwo) {
        this.usePair = new UsePair<>(identifierOne, identifierTwo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Instance instance = (Instance) o;
        return getId() == instance.getId() && getUsePair().equals(((Instance) o).getUsePair());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                usePair.toString() + ", " +
                "project = " + project + ")";
    }
}

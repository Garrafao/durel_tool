package durel.domain.repository;

import durel.domain.model.UsePairAndAnnotator;
import durel.domain.model.annotation.GoldAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoldAnnotationDAO extends JpaRepository<GoldAnnotation, UsePairAndAnnotator> {
    GoldAnnotation findById_Use1_IdAndId_Use2_Id(int use1Id, int use2Id);
}

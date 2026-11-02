package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmSeasonStage;

import java.util.List;
import java.util.Optional;

public interface MdmSeasonStageRepository extends JpaRepository<MdmSeasonStage, Long> {
    Optional<MdmSeasonStage> findBySeasonIdAndSlug(Long seasonId, String slug);
    List<MdmSeasonStage> findBySeasonIdOrderByStageOrderAsc(Long seasonId);
    Optional<MdmSeasonStage> findFirstBySeasonIdAndIsPlayoffTrue(Long seasonId);
}

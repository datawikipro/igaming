package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.MatchVisionBlueprint;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchVisionBlueprintRepository extends JpaRepository<MatchVisionBlueprint, Long> {

    Optional<MatchVisionBlueprint> findByMatchId(Long matchId);

    List<MatchVisionBlueprint> findByStatus(String status);

    List<MatchVisionBlueprint> findTop20ByOrderByCreatedAtDesc();
}

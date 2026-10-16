package pro.datawiki.igaming.capture.sofascore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.capture.sofascore.domain.SofaRawEventEntity;

import java.util.List;

@Repository
public interface SofaRawEventRepository extends JpaRepository<SofaRawEventEntity, Long> {

    List<SofaRawEventEntity> findBySportAndHomeTeamOrAwayTeamOrderByMatchDateDesc(
            String sport, String homeTeam, String awayTeam);
}

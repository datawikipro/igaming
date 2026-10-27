package pro.datawiki.igaming.capture.sofascore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.capture.sofascore.domain.SofaRawEventEntity;

import java.util.List;

@Repository
public interface SofaRawEventRepository extends JpaRepository<SofaRawEventEntity, Long> {

    List<SofaRawEventEntity> findBySportAndHomeTeamOrAwayTeamOrderByMatchDateDesc(
            String sport, String homeTeam, String awayTeam);

    @Query("SELECT e FROM SofaRawEventEntity e WHERE e.sport = :sport AND e.matchDate BETWEEN :startMs AND :endMs")
    List<SofaRawEventEntity> findBySportAndDateRange(
            @Param("sport") String sport,
            @Param("startMs") Long startMs,
            @Param("endMs") Long endMs);

    long countByMdmStatus(String mdmStatus);

    @Query("SELECT e FROM SofaRawEventEntity e WHERE e.mdmStatus = 'UNMAPPED' ORDER BY e.matchDate DESC")
    List<SofaRawEventEntity> findTopUnmappedForBackfill(org.springframework.data.domain.Pageable pageable);
}

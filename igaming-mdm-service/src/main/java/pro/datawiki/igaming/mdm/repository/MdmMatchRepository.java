package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.mdm.domain.MdmMatch;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MdmMatchRepository extends JpaRepository<MdmMatch, Long>, JpaSpecificationExecutor<MdmMatch> {

    Optional<MdmMatch> findBySofascoreId(Long sofascoreId);

    @Query("""
        SELECT m FROM MdmMatch m
        JOIN FETCH m.sport
        JOIN FETCH m.tournament
        JOIN FETCH m.homeTeam
        JOIN FETCH m.awayTeam
        WHERE m.id = :id
    """)
    Optional<MdmMatch> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT m FROM MdmMatch m
        WHERE m.sport.id = :sportId
          AND ((m.homeTeam.id = :homeId AND m.awayTeam.id = :awayId)
            OR (m.homeTeam.id = :awayId AND m.awayTeam.id = :homeId))
          AND m.startTime BETWEEN :windowStart AND :windowEnd
          AND (m.status IS NULL OR m.status != 'CANCELLED')
        ORDER BY m.startTime ASC
    """)
    List<MdmMatch> findCandidateMatches(
            @Param("sportId") Integer sportId,
            @Param("homeId") Long homeId,
            @Param("awayId") Long awayId,
            @Param("windowStart") OffsetDateTime windowStart,
            @Param("windowEnd") OffsetDateTime windowEnd
    );

    @Query("""
        SELECT m FROM MdmMatch m
        JOIN FETCH m.sport
        JOIN FETCH m.tournament
        JOIN FETCH m.homeTeam
        JOIN FETCH m.awayTeam
        WHERE m.sport.id = :sportId
          AND m.startTime BETWEEN :windowStart AND :windowEnd
          AND (m.status IS NULL OR m.status != 'CANCELLED')
        ORDER BY m.startTime ASC
    """)
    List<MdmMatch> findMatchesInWindow(
            @Param("sportId") Integer sportId,
            @Param("windowStart") OffsetDateTime windowStart,
            @Param("windowEnd") OffsetDateTime windowEnd
    );

    @Query("""
        SELECT m FROM MdmMatch m
        JOIN FETCH m.sport
        JOIN FETCH m.tournament
        JOIN FETCH m.homeTeam
        JOIN FETCH m.awayTeam
        WHERE m.sport.id = :sportId
          AND (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId)
          AND m.startTime BETWEEN :windowStart AND :windowEnd
          AND (m.status IS NULL OR m.status != 'CANCELLED')
        ORDER BY m.startTime ASC
    """)
    List<MdmMatch> findMatchesByTeamInWindow(
            @Param("sportId") Integer sportId,
            @Param("teamId") Long teamId,
            @Param("windowStart") OffsetDateTime windowStart,
            @Param("windowEnd") OffsetDateTime windowEnd
    );

    @Query("""
        SELECT m FROM MdmMatch m
        JOIN FETCH m.sport
        JOIN FETCH m.tournament
        JOIN FETCH m.homeTeam
        JOIN FETCH m.awayTeam
        WHERE m.sport.id = :sportId
          AND m.tournament.id = :tournamentId
          AND m.startTime BETWEEN :windowStart AND :windowEnd
          AND (m.status IS NULL OR m.status != 'CANCELLED')
        ORDER BY m.startTime ASC
    """)
    List<MdmMatch> findMatchesByTournamentInWindow(
            @Param("sportId") Integer sportId,
            @Param("tournamentId") Long tournamentId,
            @Param("windowStart") OffsetDateTime windowStart,
            @Param("windowEnd") OffsetDateTime windowEnd
    );

    @Query("""
        SELECT m FROM MdmMatch m
        JOIN FETCH m.sport
        JOIN FETCH m.tournament
        JOIN FETCH m.homeTeam
        JOIN FETCH m.awayTeam
        WHERE m.status IN ('SCHEDULED', 'LIVE', 'IN_PLAY')
          AND m.startTime >= :fromTime
        ORDER BY m.startTime ASC
    """)
    List<MdmMatch> findActiveUpcomingMatches(@Param("fromTime") OffsetDateTime fromTime);

    @Query("""
        SELECT m FROM MdmMatch m
        JOIN FETCH m.sport
        JOIN FETCH m.tournament
        JOIN FETCH m.homeTeam
        JOIN FETCH m.awayTeam
        WHERE m.status IN ('SCHEDULED', 'LIVE', 'IN_PLAY')
          AND m.startTime >= :fromTime
          AND m.startTime <= :toTime
        ORDER BY m.startTime ASC
    """)
    List<MdmMatch> findActiveUpcomingMatchesWindow(
            @Param("fromTime") OffsetDateTime fromTime,
            @Param("toTime") OffsetDateTime toTime
    );

    @Query("""
        SELECT count(m) FROM MdmMatch m
        WHERE m.status IN ('SCHEDULED', 'LIVE', 'IN_PLAY')
          AND m.startTime >= :fromTime
    """)
    long countActiveUpcomingMatches(@Param("fromTime") OffsetDateTime fromTime);

    @Query("SELECT DISTINCT m.sport.slug FROM MdmMatch m WHERE m.sport.slug IS NOT NULL ORDER BY m.sport.slug ASC")
    List<String> findDistinctSportSlugs();
}

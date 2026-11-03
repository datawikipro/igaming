/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.JpaSpecificationExecutor
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.stereotype.Repository
 */
package pro.datawiki.igaming.mdm.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.mdm.domain.MdmMatch;

@Repository
public interface MdmMatchRepository
extends JpaRepository<MdmMatch, Long>,
JpaSpecificationExecutor<MdmMatch> {
    public Optional<MdmMatch> findBySofascoreId(Long var1);

    @Query(value="    SELECT m FROM MdmMatch m\n    JOIN FETCH m.sport\n    JOIN FETCH m.tournament\n    JOIN FETCH m.homeTeam\n    JOIN FETCH m.awayTeam\n    WHERE m.id = :id\n")
    public Optional<MdmMatch> findByIdWithDetails(@Param(value="id") Long var1);

    @Query(value="    SELECT m FROM MdmMatch m\n    WHERE m.sport.id = :sportId\n      AND ((m.homeTeam.id = :homeId AND m.awayTeam.id = :awayId)\n        OR (m.homeTeam.id = :awayId AND m.awayTeam.id = :homeId))\n      AND m.startTime BETWEEN :windowStart AND :windowEnd\n      AND (m.status IS NULL OR m.status != 'CANCELLED')\n    ORDER BY m.startTime ASC\n")
    public List<MdmMatch> findCandidateMatches(@Param(value="sportId") Integer var1, @Param(value="homeId") Long var2, @Param(value="awayId") Long var3, @Param(value="windowStart") OffsetDateTime var4, @Param(value="windowEnd") OffsetDateTime var5);

    @Query(value="    SELECT m FROM MdmMatch m\n    JOIN FETCH m.sport\n    JOIN FETCH m.tournament\n    JOIN FETCH m.homeTeam\n    JOIN FETCH m.awayTeam\n    WHERE m.sport.id = :sportId\n      AND m.startTime BETWEEN :windowStart AND :windowEnd\n      AND (m.status IS NULL OR m.status != 'CANCELLED')\n    ORDER BY m.startTime ASC\n")
    public List<MdmMatch> findMatchesInWindow(@Param(value="sportId") Integer var1, @Param(value="windowStart") OffsetDateTime var2, @Param(value="windowEnd") OffsetDateTime var3);

    @Query(value="    SELECT m FROM MdmMatch m\n    JOIN FETCH m.sport\n    JOIN FETCH m.tournament\n    JOIN FETCH m.homeTeam\n    JOIN FETCH m.awayTeam\n    WHERE m.status IN ('SCHEDULED', 'LIVE', 'IN_PLAY')\n      AND m.startTime >= :fromTime\n    ORDER BY m.startTime ASC\n")
    public List<MdmMatch> findActiveUpcomingMatches(@Param(value="fromTime") OffsetDateTime var1);

    @Query(value="    SELECT m FROM MdmMatch m\n    JOIN FETCH m.sport\n    JOIN FETCH m.tournament\n    JOIN FETCH m.homeTeam\n    JOIN FETCH m.awayTeam\n    WHERE m.status IN ('SCHEDULED', 'LIVE', 'IN_PLAY')\n      AND m.startTime >= :fromTime\n      AND m.startTime <= :toTime\n    ORDER BY m.startTime ASC\n")
    public List<MdmMatch> findActiveUpcomingMatchesWindow(@Param(value="fromTime") OffsetDateTime var1, @Param(value="toTime") OffsetDateTime var2);

    @Query(value="    SELECT count(m) FROM MdmMatch m\n    WHERE m.status IN ('SCHEDULED', 'LIVE', 'IN_PLAY')\n      AND m.startTime >= :fromTime\n")
    public long countActiveUpcomingMatches(@Param(value="fromTime") OffsetDateTime var1);

    @Query(value="SELECT DISTINCT m.sport.slug FROM MdmMatch m WHERE m.sport.slug IS NOT NULL ORDER BY m.sport.slug ASC")
    public List<String> findDistinctSportSlugs();
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.stereotype.Repository
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournamentTeam;

@Repository
public interface MdmTournamentTeamRepository
extends JpaRepository<MdmTournamentTeam, Long> {
    public Optional<MdmTournamentTeam> findByTournamentIdAndTeamId(Long var1, Long var2);

    public boolean existsByTournamentIdAndTeamId(Long var1, Long var2);

    @Query(value="SELECT tt.team.id FROM MdmTournamentTeam tt WHERE tt.tournament.id = :tournamentId AND tt.isActive = true")
    public Set<Long> findActiveTeamIdsByTournamentId(@Param(value="tournamentId") Long var1);

    @Query(value="SELECT t FROM MdmTournamentTeam tt JOIN tt.team t WHERE tt.tournament.id = :tournamentId AND tt.isActive = true")
    public List<MdmTeam> findActiveTeamsByTournamentId(@Param(value="tournamentId") Long var1);

    public long countByTournamentIdAndIsActiveTrue(Long var1);
}


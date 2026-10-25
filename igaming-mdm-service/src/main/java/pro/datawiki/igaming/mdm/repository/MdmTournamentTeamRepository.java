package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.mdm.domain.MdmTournamentTeam;

import java.util.Optional;
import java.util.Set;

@Repository
public interface MdmTournamentTeamRepository extends JpaRepository<MdmTournamentTeam, Long> {

    Optional<MdmTournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    @Query("SELECT tt.team.id FROM MdmTournamentTeam tt WHERE tt.tournament.id = :tournamentId AND tt.isActive = true")
    Set<Long> findActiveTeamIdsByTournamentId(@Param("tournamentId") Long tournamentId);

    long countByTournamentIdAndIsActiveTrue(Long tournamentId);
}

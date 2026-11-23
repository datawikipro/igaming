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

    @Query("SELECT t FROM MdmTournamentTeam tt JOIN tt.team t WHERE tt.tournament.id = :tournamentId AND tt.isActive = true")
    java.util.List<pro.datawiki.igaming.mdm.domain.MdmTeam> findActiveTeamsByTournamentId(@Param("tournamentId") Long tournamentId);

    long countByTournamentIdAndIsActiveTrue(Long tournamentId);

    @Query("SELECT COUNT(tt) > 0 FROM MdmTournamentTeam tt WHERE tt.team.id = :teamId AND tt.tournament.gender = 'FEMALE'")
    boolean existsByTeamIdAndFemaleTournament(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(tt) > 0 FROM MdmTournamentTeam tt WHERE tt.team.id = :teamId AND tt.tournament.country.id = :countryId")
    boolean existsByTeamIdAndCountryId(@Param("teamId") Long teamId, @Param("countryId") Integer countryId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "INSERT INTO mdm_tournament_team (tournament_id, team_id, sport_id, is_active, created_at, updated_at) " +
                   "VALUES (:tournamentId, :teamId, :sportId, true, NOW(), NOW()) " +
                   "ON CONFLICT (tournament_id, team_id) DO NOTHING", nativeQuery = true)
    int registerParticipantIdempotent(@Param("tournamentId") Long tournamentId,
                                      @Param("teamId") Long teamId,
                                      @Param("sportId") Integer sportId);
}

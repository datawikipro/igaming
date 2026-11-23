package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmTeamMembership;

import java.util.List;
import java.util.Optional;

public interface MdmTeamMembershipRepository extends JpaRepository<MdmTeamMembership, Long> {
    List<MdmTeamMembership> findByTeamIdAndIsActiveTrue(Long teamId);

    @Query("SELECT m FROM MdmTeamMembership m JOIN FETCH m.player p LEFT JOIN FETCH p.nationality WHERE m.team.id = :teamId AND m.isActive = true")
    List<MdmTeamMembership> findActiveMembershipsWithPlayer(@Param("teamId") Long teamId);

    List<MdmTeamMembership> findByPlayerId(Long playerId);
    List<MdmTeamMembership> findByPlayerIdAndIsActiveTrue(Long playerId);
    Optional<MdmTeamMembership> findByPlayerIdAndMembershipTypeAndIsActiveTrue(Long playerId, String membershipType);
}

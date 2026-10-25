package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmTeamMembership;

import java.util.List;
import java.util.Optional;

public interface MdmTeamMembershipRepository extends JpaRepository<MdmTeamMembership, Long> {
    List<MdmTeamMembership> findByTeamIdAndIsActiveTrue(Long teamId);
    List<MdmTeamMembership> findByPlayerIdAndIsActiveTrue(Long playerId);
    Optional<MdmTeamMembership> findByPlayerIdAndMembershipTypeAndIsActiveTrue(Long playerId, String membershipType);
}

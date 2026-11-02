package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmSeason;

import java.util.List;
import java.util.Optional;

public interface MdmSeasonRepository extends JpaRepository<MdmSeason, Long> {
    Optional<MdmSeason> findByTournamentIdAndSlug(Long tournamentId, String slug);
    List<MdmSeason> findByTournamentId(Long tournamentId);
    Optional<MdmSeason> findFirstByTournamentIdAndStatus(Long tournamentId, String status);
}

package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmTournamentTranslation;

import java.util.List;
import java.util.Optional;

public interface MdmTournamentTranslationRepository extends JpaRepository<MdmTournamentTranslation, Long> {
    Optional<MdmTournamentTranslation> findByTournamentIdAndLang(Long tournamentId, String lang);
    List<MdmTournamentTranslation> findByTournamentId(Long tournamentId);
}

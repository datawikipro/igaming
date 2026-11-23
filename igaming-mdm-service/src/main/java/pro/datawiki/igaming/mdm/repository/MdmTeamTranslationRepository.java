package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmTeamTranslation;

import java.util.List;
import java.util.Optional;

public interface MdmTeamTranslationRepository extends JpaRepository<MdmTeamTranslation, Long> {
    Optional<MdmTeamTranslation> findByTeamIdAndLang(Long teamId, String lang);
    List<MdmTeamTranslation> findByTeamId(Long teamId);
    Optional<MdmTeamTranslation> findFirstByNameIgnoreCase(String name);
    List<MdmTeamTranslation> findAllByTeamIdIn(java.util.Collection<Long> teamIds);
    List<MdmTeamTranslation> findAllByTeamIdInAndLang(java.util.Collection<Long> teamIds, String lang);
}

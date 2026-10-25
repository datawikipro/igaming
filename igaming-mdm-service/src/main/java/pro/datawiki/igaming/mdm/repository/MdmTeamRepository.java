package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmTeam;

import java.util.List;
import java.util.Optional;

public interface MdmTeamRepository extends JpaRepository<MdmTeam, Long> {
    Optional<MdmTeam> findBySportIdAndSlug(Integer sportId, String slug);
    Optional<MdmTeam> findBySofascoreId(Long sofascoreId);
    Optional<MdmTeam> findBySportIdAndNormalizedSignatureAndAgeCategoryAndGender(
            Integer sportId, String normalizedSignature, String ageCategory, String gender
    );
    List<MdmTeam> findBySportIdAndIsNationalTrue(Integer sportId);
}

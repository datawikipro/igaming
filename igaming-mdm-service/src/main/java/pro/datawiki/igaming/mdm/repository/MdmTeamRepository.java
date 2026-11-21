package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmTeam;

import java.util.List;
import java.util.Optional;

public interface MdmTeamRepository extends JpaRepository<MdmTeam, Long>, MdmTeamRepositoryCustom {
    Optional<MdmTeam> findBySportIdAndSlug(Integer sportId, String slug);
    Optional<MdmTeam> findFirstBySportIdAndCanonicalNameIgnoreCase(Integer sportId, String canonicalName);
    Optional<MdmTeam> findFirstBySportIdAndCountryIdAndCanonicalNameIgnoreCase(Integer sportId, Integer countryId, String canonicalName);
    Optional<MdmTeam> findFirstBySportIdAndCountryIdAndSlug(Integer sportId, Integer countryId, String slug);
    Optional<MdmTeam> findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(Integer sportId, Integer categoryId, String canonicalName);
    Optional<MdmTeam> findFirstBySportIdAndCategoryIdAndSlug(Integer sportId, Integer categoryId, String slug);
    Optional<MdmTeam> findBySofascoreId(Long sofascoreId);
    Optional<MdmTeam> findFirstBySportIdAndNormalizedSignatureAndAgeCategoryAndGender(
            Integer sportId, String normalizedSignature, String ageCategory, String gender
    );
    List<MdmTeam> findBySportIdAndIsNationalTrue(Integer sportId);
    List<MdmTeam> findBySportIdAndCountryId(Integer sportId, Integer countryId);
    List<MdmTeam> findBySportIdAndCategoryId(Integer sportId, Integer categoryId);

    @Query("SELECT t FROM MdmTeam t WHERE t.sport.id = :sportId AND lower(t.canonicalName) = lower(:name) AND lower(t.countryName) = lower(:countryName)")
    Optional<MdmTeam> findFirstBySportIdAndNameAndCountry(@Param("sportId") Integer sportId,
                                                          @Param("name") String name,
                                                          @Param("countryName") String countryName);
}

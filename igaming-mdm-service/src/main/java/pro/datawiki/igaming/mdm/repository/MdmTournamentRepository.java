package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmTournament;

import java.util.List;
import java.util.Optional;

public interface MdmTournamentRepository extends JpaRepository<MdmTournament, Long> {
    Optional<MdmTournament> findBySportIdAndCategoryIdAndSlug(Integer sportId, Integer categoryId, String slug);
    Optional<MdmTournament> findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(Integer sportId, Integer categoryId, String canonicalName);
    Optional<MdmTournament> findFirstBySportIdAndCanonicalNameIgnoreCase(Integer sportId, String canonicalName);
    Optional<MdmTournament> findFirstBySportIdAndSlug(Integer sportId, String slug);
    List<MdmTournament> findBySportIdAndCanonicalNameContainingIgnoreCase(Integer sportId, String part);
    Optional<MdmTournament> findBySofascoreId(Long sofascoreId);
    List<MdmTournament> findBySportIdAndIsFeaturedTrue(Integer sportId);

    /**
     * Country-aware lookup: finds tournament by sport + name + country (case-insensitive).
     * Used when resolving ambiguous generic league names like "Premier League", "Serie A".
     */
    @Query("SELECT t FROM MdmTournament t WHERE t.sport.id = :sportId AND lower(t.canonicalName) = lower(:name) AND lower(t.countryName) = lower(:countryName)")
    Optional<MdmTournament> findFirstBySportIdAndNameAndCountry(@Param("sportId") Integer sportId,
                                                                 @Param("name") String name,
                                                                 @Param("countryName") String countryName);

    /**
     * Country-aware lookup by slug + country.
     */
    @Query("SELECT t FROM MdmTournament t WHERE t.sport.id = :sportId AND t.slug = :slug AND lower(t.countryName) = lower(:countryName)")
    Optional<MdmTournament> findFirstBySportIdAndSlugAndCountry(@Param("sportId") Integer sportId,
                                                                 @Param("slug") String slug,
                                                                 @Param("countryName") String countryName);
}

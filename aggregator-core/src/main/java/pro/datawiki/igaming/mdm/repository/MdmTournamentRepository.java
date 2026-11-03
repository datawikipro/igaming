/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmTournament;

public interface MdmTournamentRepository
extends JpaRepository<MdmTournament, Long> {
    public Optional<MdmTournament> findBySportIdAndCategoryIdAndSlug(Integer var1, Integer var2, String var3);

    public Optional<MdmTournament> findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(Integer var1, Integer var2, String var3);

    public Optional<MdmTournament> findFirstBySportIdAndCanonicalNameIgnoreCase(Integer var1, String var2);

    public Optional<MdmTournament> findFirstBySportIdAndSlug(Integer var1, String var2);

    public List<MdmTournament> findBySportIdAndCanonicalNameContainingIgnoreCase(Integer var1, String var2);

    public Optional<MdmTournament> findBySofascoreId(Long var1);

    public List<MdmTournament> findBySportIdAndIsFeaturedTrue(Integer var1);

    public Optional<MdmTournament> findFirstBySportIdAndCountryIdAndCanonicalNameIgnoreCase(Integer var1, Integer var2, String var3);

    public Optional<MdmTournament> findFirstBySportIdAndCountryIdAndSlug(Integer var1, Integer var2, String var3);

    public List<MdmTournament> findBySportIdAndCountryId(Integer var1, Integer var2);

    public List<MdmTournament> findBySportId(Integer var1);

    @Query(value="SELECT t FROM MdmTournament t WHERE t.sport.id = :sportId AND lower(t.canonicalName) = lower(:name) AND lower(t.countryName) = lower(:countryName)")
    public Optional<MdmTournament> findFirstBySportIdAndNameAndCountry(@Param(value="sportId") Integer var1, @Param(value="name") String var2, @Param(value="countryName") String var3);

    @Query(value="SELECT t FROM MdmTournament t WHERE t.sport.id = :sportId AND t.slug = :slug AND lower(t.countryName) = lower(:countryName)")
    public Optional<MdmTournament> findFirstBySportIdAndSlugAndCountry(@Param(value="sportId") Integer var1, @Param(value="slug") String var2, @Param(value="countryName") String var3);
}


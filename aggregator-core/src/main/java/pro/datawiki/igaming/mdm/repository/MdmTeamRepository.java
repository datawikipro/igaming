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
import pro.datawiki.igaming.mdm.domain.MdmTeam;

public interface MdmTeamRepository
extends JpaRepository<MdmTeam, Long> {
    public Optional<MdmTeam> findBySportIdAndSlug(Integer var1, String var2);

    public Optional<MdmTeam> findFirstBySportIdAndCanonicalNameIgnoreCase(Integer var1, String var2);

    public Optional<MdmTeam> findFirstBySportIdAndCountryIdAndCanonicalNameIgnoreCase(Integer var1, Integer var2, String var3);

    public Optional<MdmTeam> findFirstBySportIdAndCountryIdAndSlug(Integer var1, Integer var2, String var3);

    public Optional<MdmTeam> findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(Integer var1, Integer var2, String var3);

    public Optional<MdmTeam> findFirstBySportIdAndCategoryIdAndSlug(Integer var1, Integer var2, String var3);

    public Optional<MdmTeam> findBySofascoreId(Long var1);

    public Optional<MdmTeam> findFirstBySportIdAndNormalizedSignatureAndAgeCategoryAndGender(Integer var1, String var2, String var3, String var4);

    public List<MdmTeam> findBySportIdAndIsNationalTrue(Integer var1);

    public List<MdmTeam> findBySportIdAndCountryId(Integer var1, Integer var2);

    public List<MdmTeam> findBySportIdAndCategoryId(Integer var1, Integer var2);

    @Query(value="SELECT t FROM MdmTeam t WHERE t.sport.id = :sportId AND lower(t.canonicalName) = lower(:name) AND lower(t.countryName) = lower(:countryName)")
    public Optional<MdmTeam> findFirstBySportIdAndNameAndCountry(@Param(value="sportId") Integer var1, @Param(value="name") String var2, @Param(value="countryName") String var3);
}


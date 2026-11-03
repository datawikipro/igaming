/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmCategory;

public interface MdmCategoryRepository
extends JpaRepository<MdmCategory, Integer> {
    public Optional<MdmCategory> findBySportIdAndSlug(Integer var1, String var2);

    public Optional<MdmCategory> findFirstBySportIdAndSlugIgnoreCase(Integer var1, String var2);

    public Optional<MdmCategory> findBySofascoreId(Long var1);
}


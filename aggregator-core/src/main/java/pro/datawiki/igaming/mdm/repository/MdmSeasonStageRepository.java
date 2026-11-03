/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmSeasonStage;

public interface MdmSeasonStageRepository
extends JpaRepository<MdmSeasonStage, Long> {
    public Optional<MdmSeasonStage> findBySeasonIdAndSlug(Long var1, String var2);

    public List<MdmSeasonStage> findBySeasonIdOrderByStageOrderAsc(Long var1);

    public Optional<MdmSeasonStage> findFirstBySeasonIdAndIsPlayoffTrue(Long var1);
}


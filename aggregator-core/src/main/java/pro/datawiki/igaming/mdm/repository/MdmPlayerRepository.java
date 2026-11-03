/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmPlayer;

public interface MdmPlayerRepository
extends JpaRepository<MdmPlayer, Long> {
    public Optional<MdmPlayer> findBySportIdAndSlug(Integer var1, String var2);

    public Optional<MdmPlayer> findBySofascoreId(Long var1);
}


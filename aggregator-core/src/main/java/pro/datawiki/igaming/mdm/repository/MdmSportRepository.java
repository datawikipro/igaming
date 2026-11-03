/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmSport;

public interface MdmSportRepository
extends JpaRepository<MdmSport, Integer> {
    public Optional<MdmSport> findBySlug(String var1);

    public Optional<MdmSport> findByDomainKey(String var1);
}


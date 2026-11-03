/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmExternalSystem;

public interface MdmExternalSystemRepository
extends JpaRepository<MdmExternalSystem, Integer> {
    public Optional<MdmExternalSystem> findByCode(String var1);
}


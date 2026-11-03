/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.mdm.domain.MdmOrganization;

@Repository
public interface MdmOrganizationRepository
extends JpaRepository<MdmOrganization, Long> {
    public Optional<MdmOrganization> findBySlug(String var1);

    public Optional<MdmOrganization> findByCanonicalNameIgnoreCase(String var1);
}


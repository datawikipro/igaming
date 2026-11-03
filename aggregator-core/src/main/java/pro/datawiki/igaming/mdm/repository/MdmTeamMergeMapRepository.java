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
import pro.datawiki.igaming.mdm.domain.MdmTeamMergeMap;

@Repository
public interface MdmTeamMergeMapRepository
extends JpaRepository<MdmTeamMergeMap, Long> {
    public Optional<MdmTeamMergeMap> findByLocalId(Long var1);
}


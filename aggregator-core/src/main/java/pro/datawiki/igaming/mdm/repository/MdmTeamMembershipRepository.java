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
import pro.datawiki.igaming.mdm.domain.MdmTeamMembership;

public interface MdmTeamMembershipRepository
extends JpaRepository<MdmTeamMembership, Long> {
    public List<MdmTeamMembership> findByTeamIdAndIsActiveTrue(Long var1);

    public List<MdmTeamMembership> findByPlayerIdAndIsActiveTrue(Long var1);

    public Optional<MdmTeamMembership> findByPlayerIdAndMembershipTypeAndIsActiveTrue(Long var1, String var2);
}


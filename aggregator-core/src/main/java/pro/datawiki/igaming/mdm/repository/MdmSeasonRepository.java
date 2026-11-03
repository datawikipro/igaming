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
import pro.datawiki.igaming.mdm.domain.MdmSeason;

public interface MdmSeasonRepository
extends JpaRepository<MdmSeason, Long> {
    public Optional<MdmSeason> findByTournamentIdAndSlug(Long var1, String var2);

    public List<MdmSeason> findByTournamentId(Long var1);

    public Optional<MdmSeason> findFirstByTournamentIdAndStatus(Long var1, String var2);
}


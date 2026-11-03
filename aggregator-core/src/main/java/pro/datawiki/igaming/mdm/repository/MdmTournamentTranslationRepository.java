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
import pro.datawiki.igaming.mdm.domain.MdmTournamentTranslation;

public interface MdmTournamentTranslationRepository
extends JpaRepository<MdmTournamentTranslation, Long> {
    public Optional<MdmTournamentTranslation> findByTournamentIdAndLang(Long var1, String var2);

    public List<MdmTournamentTranslation> findByTournamentId(Long var1);

    public Optional<MdmTournamentTranslation> findFirstByNameIgnoreCase(String var1);
}


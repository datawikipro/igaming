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
import pro.datawiki.igaming.mdm.domain.MdmTeamTranslation;

public interface MdmTeamTranslationRepository
extends JpaRepository<MdmTeamTranslation, Long> {
    public Optional<MdmTeamTranslation> findByTeamIdAndLang(Long var1, String var2);

    public List<MdmTeamTranslation> findByTeamId(Long var1);

    public Optional<MdmTeamTranslation> findFirstByNameIgnoreCase(String var1);
}


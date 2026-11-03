/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmCountry;

public interface MdmCountryRepository
extends JpaRepository<MdmCountry, Integer> {
    public Optional<MdmCountry> findFirstByCanonicalNameIgnoreCase(String var1);

    public Optional<MdmCountry> findFirstByIsoCodeIgnoreCase(String var1);

    public Optional<MdmCountry> findFirstBySofascoreId(Integer var1);
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.transaction.annotation.Transactional
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.MdmCountryIngestionMap;

public interface MdmCountryIngestionMapRepository
extends JpaRepository<MdmCountryIngestionMap, Long> {
    public Optional<MdmCountryIngestionMap> findByExternalSystemIdAndRawKey(Integer var1, String var2);

    public List<MdmCountryIngestionMap> findByExternalSystemId(Integer var1);

    @Modifying
    @Transactional
    @Query(value="INSERT INTO mdm_country_ingestion_map (external_system_id, raw_key, canonical_country_id, raw_name)\nVALUES (:sysId, :rawKey, :countryId, :rawName)\nON CONFLICT (external_system_id, raw_key) DO NOTHING\n", nativeQuery=true)
    public int insertIgnoreConflict(@Param(value="sysId") Integer var1, @Param(value="rawKey") String var2, @Param(value="countryId") Integer var3, @Param(value="rawName") String var4);
}


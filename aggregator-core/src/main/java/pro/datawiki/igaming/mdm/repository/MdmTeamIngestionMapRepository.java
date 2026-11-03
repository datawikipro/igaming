/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmTeamIngestionMap;

public interface MdmTeamIngestionMapRepository
extends JpaRepository<MdmTeamIngestionMap, Long> {
    @Query(value="SELECT m FROM MdmTeamIngestionMap m WHERE m.externalSystem.id = :externalSystemId AND LOWER(m.rawKey) = LOWER(:rawKey) ORDER BY m.id DESC")
    public List<MdmTeamIngestionMap> findAllByExternalSystemIdAndRawKey(@Param(value="externalSystemId") Integer var1, @Param(value="rawKey") String var2);

    default public Optional<MdmTeamIngestionMap> findByExternalSystemIdAndRawKey(Integer externalSystemId, String rawKey) {
        List<MdmTeamIngestionMap> list = this.findAllByExternalSystemIdAndRawKey(externalSystemId, rawKey);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Query(value="SELECT m FROM MdmTeamIngestionMap m WHERE m.externalSystem.id = :externalSystemId AND m.sourceId = :sourceId ORDER BY m.id DESC")
    public List<MdmTeamIngestionMap> findAllByExternalSystemIdAndSourceId(@Param(value="externalSystemId") Integer var1, @Param(value="sourceId") String var2);

    default public Optional<MdmTeamIngestionMap> findFirstByExternalSystemIdAndSourceId(Integer externalSystemId, String sourceId) {
        List<MdmTeamIngestionMap> list = this.findAllByExternalSystemIdAndSourceId(externalSystemId, sourceId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Modifying
    @Query(value="INSERT INTO mdm_team_ingestion_map (external_system_id, raw_key, source_id, canonical_team_id, raw_name, is_verified, created_at) VALUES (:externalSystemId, :rawKey, :sourceId, :canonicalTeamId, :rawName, true, NOW()) ON CONFLICT (external_system_id, raw_key) DO UPDATE SET canonical_team_id = EXCLUDED.canonical_team_id, source_id = COALESCE(EXCLUDED.source_id, mdm_team_ingestion_map.source_id)", nativeQuery=true)
    public int upsertMapping(@Param(value="externalSystemId") Integer var1, @Param(value="rawKey") String var2, @Param(value="sourceId") String var3, @Param(value="canonicalTeamId") Long var4, @Param(value="rawName") String var5);

    @Query(value="SELECT m FROM MdmTeamIngestionMap m JOIN FETCH m.canonicalTeam t JOIN FETCH t.sport LEFT JOIN FETCH t.category WHERE m.externalSystem.id = :externalSystemId")
    public List<MdmTeamIngestionMap> findByExternalSystemIdWithFetch(@Param(value="externalSystemId") Integer var1);

    public List<MdmTeamIngestionMap> findByExternalSystemId(Integer var1);

    @Modifying
    @Query(value="INSERT INTO mdm_team_ingestion_map (external_system_id, raw_key, canonical_team_id, raw_name, is_verified, created_at) VALUES (:externalSystemId, :rawKey, :canonicalTeamId, :rawName, true, NOW()) ON CONFLICT (external_system_id, raw_key) DO NOTHING", nativeQuery=true)
    public int insertIgnoreConflict(@Param(value="externalSystemId") Integer var1, @Param(value="rawKey") String var2, @Param(value="canonicalTeamId") Long var3, @Param(value="rawName") String var4);
}


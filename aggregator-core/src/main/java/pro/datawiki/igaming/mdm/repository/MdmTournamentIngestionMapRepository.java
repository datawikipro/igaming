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
import pro.datawiki.igaming.mdm.domain.MdmTournamentIngestionMap;

public interface MdmTournamentIngestionMapRepository
extends JpaRepository<MdmTournamentIngestionMap, Long> {
    @Query(value="SELECT m FROM MdmTournamentIngestionMap m WHERE m.externalSystem.id = :externalSystemId AND LOWER(m.rawKey) = LOWER(:rawKey) ORDER BY m.id DESC")
    public List<MdmTournamentIngestionMap> findAllByExternalSystemIdAndRawKey(@Param(value="externalSystemId") Integer var1, @Param(value="rawKey") String var2);

    default public Optional<MdmTournamentIngestionMap> findByExternalSystemIdAndRawKey(Integer externalSystemId, String rawKey) {
        List<MdmTournamentIngestionMap> list = this.findAllByExternalSystemIdAndRawKey(externalSystemId, rawKey);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Query(value="SELECT m FROM MdmTournamentIngestionMap m WHERE m.externalSystem.id = :externalSystemId AND m.sourceId = :sourceId ORDER BY m.id DESC")
    public List<MdmTournamentIngestionMap> findAllByExternalSystemIdAndSourceId(@Param(value="externalSystemId") Integer var1, @Param(value="sourceId") String var2);

    default public Optional<MdmTournamentIngestionMap> findFirstByExternalSystemIdAndSourceId(Integer externalSystemId, String sourceId) {
        List<MdmTournamentIngestionMap> list = this.findAllByExternalSystemIdAndSourceId(externalSystemId, sourceId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Modifying
    @Query(value="INSERT INTO mdm_tournament_ingestion_map (external_system_id, raw_key, source_id, canonical_tournament_id, raw_name, created_at) VALUES (:externalSystemId, :rawKey, :sourceId, :canonicalTournamentId, :rawName, NOW()) ON CONFLICT (external_system_id, raw_key) DO UPDATE SET canonical_tournament_id = EXCLUDED.canonical_tournament_id, source_id = COALESCE(EXCLUDED.source_id, mdm_tournament_ingestion_map.source_id)", nativeQuery=true)
    public int upsertMapping(@Param(value="externalSystemId") Integer var1, @Param(value="rawKey") String var2, @Param(value="sourceId") String var3, @Param(value="canonicalTournamentId") Long var4, @Param(value="rawName") String var5);

    @Query(value="SELECT m FROM MdmTournamentIngestionMap m JOIN FETCH m.canonicalTournament t JOIN FETCH t.sport LEFT JOIN FETCH t.category WHERE m.externalSystem.id = :externalSystemId")
    public List<MdmTournamentIngestionMap> findByExternalSystemIdWithFetch(@Param(value="externalSystemId") Integer var1);

    public List<MdmTournamentIngestionMap> findByExternalSystemId(Integer var1);

    @Modifying
    @Query(value="INSERT INTO mdm_tournament_ingestion_map (external_system_id, raw_key, canonical_tournament_id, raw_name, created_at) VALUES (:externalSystemId, :rawKey, :canonicalTournamentId, :rawName, NOW()) ON CONFLICT (external_system_id, raw_key) DO NOTHING", nativeQuery=true)
    public int insertIgnoreConflict(@Param(value="externalSystemId") Integer var1, @Param(value="rawKey") String var2, @Param(value="canonicalTournamentId") Long var3, @Param(value="rawName") String var4);
}


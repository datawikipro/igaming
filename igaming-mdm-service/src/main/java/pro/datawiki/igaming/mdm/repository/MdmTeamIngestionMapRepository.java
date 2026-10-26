package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmTeamIngestionMap;

import java.util.List;
import java.util.Optional;

public interface MdmTeamIngestionMapRepository extends JpaRepository<MdmTeamIngestionMap, Long> {
    Optional<MdmTeamIngestionMap> findByExternalSystemIdAndRawKey(Integer externalSystemId, String rawKey);

    @Query("SELECT m FROM MdmTeamIngestionMap m JOIN FETCH m.canonicalTeam t JOIN FETCH t.sport LEFT JOIN FETCH t.category WHERE m.externalSystem.id = :externalSystemId")
    List<MdmTeamIngestionMap> findByExternalSystemIdWithFetch(@Param("externalSystemId") Integer externalSystemId);

    List<MdmTeamIngestionMap> findByExternalSystemId(Integer externalSystemId);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "INSERT INTO mdm_team_ingestion_map (external_system_id, raw_key, canonical_team_id, raw_name, is_verified, created_at) " +
            "VALUES (:externalSystemId, :rawKey, :canonicalTeamId, :rawName, true, NOW()) " +
            "ON CONFLICT (external_system_id, raw_key) DO NOTHING", nativeQuery = true)
    int insertIgnoreConflict(
            @Param("externalSystemId") Integer externalSystemId,
            @Param("rawKey") String rawKey,
            @Param("canonicalTeamId") Long canonicalTeamId,
            @Param("rawName") String rawName
    );
}

package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmTournamentIngestionMap;

import java.util.List;
import java.util.Optional;

public interface MdmTournamentIngestionMapRepository extends JpaRepository<MdmTournamentIngestionMap, Long> {
    @Query("SELECT m FROM MdmTournamentIngestionMap m WHERE m.externalSystem.id = :externalSystemId AND LOWER(m.rawKey) = LOWER(:rawKey) ORDER BY m.id DESC")
    List<MdmTournamentIngestionMap> findAllByExternalSystemIdAndRawKey(@Param("externalSystemId") Integer externalSystemId, @Param("rawKey") String rawKey);

    default Optional<MdmTournamentIngestionMap> findByExternalSystemIdAndRawKey(Integer externalSystemId, String rawKey) {
        List<MdmTournamentIngestionMap> list = findAllByExternalSystemIdAndRawKey(externalSystemId, rawKey);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Query("SELECT m FROM MdmTournamentIngestionMap m JOIN FETCH m.canonicalTournament t JOIN FETCH t.sport LEFT JOIN FETCH t.category WHERE m.externalSystem.id = :externalSystemId")
    List<MdmTournamentIngestionMap> findByExternalSystemIdWithFetch(@Param("externalSystemId") Integer externalSystemId);

    List<MdmTournamentIngestionMap> findByExternalSystemId(Integer externalSystemId);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "INSERT INTO mdm_tournament_ingestion_map (external_system_id, raw_key, canonical_tournament_id, raw_name, created_at) " +
            "VALUES (:externalSystemId, :rawKey, :canonicalTournamentId, :rawName, NOW()) " +
            "ON CONFLICT (external_system_id, raw_key) DO NOTHING", nativeQuery = true)
    int insertIgnoreConflict(
            @Param("externalSystemId") Integer externalSystemId,
            @Param("rawKey") String rawKey,
            @Param("canonicalTournamentId") Long canonicalTournamentId,
            @Param("rawName") String rawName
    );
}

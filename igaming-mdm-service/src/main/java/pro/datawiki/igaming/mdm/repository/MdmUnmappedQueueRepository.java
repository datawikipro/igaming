package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmUnmappedQueue;

import java.util.List;
import java.util.Optional;

public interface MdmUnmappedQueueRepository extends JpaRepository<MdmUnmappedQueue, Long> {

    Optional<MdmUnmappedQueue> findByExternalSystemCodeAndEntityTypeAndRawKey(
            String externalSystemCode, String entityType, String rawKey);

    List<MdmUnmappedQueue> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);

    List<MdmUnmappedQueue> findByStatusAndEntityTypeOrderByCreatedAtAsc(String status, String entityType, Pageable pageable);

    List<MdmUnmappedQueue> findByStatusAndExternalSystemCodeOrderByCreatedAtAsc(String status, String externalSystemCode, Pageable pageable);

    List<MdmUnmappedQueue> findByStatusAndEntityTypeAndExternalSystemCodeOrderByCreatedAtAsc(
            String status, String entityType, String externalSystemCode, Pageable pageable);

    @Modifying
    @Query(value = """
        INSERT INTO mdm_unmapped_queue (
            external_system_code, entity_type, raw_key, source_id, raw_name,
            sport_id, sport_slug, country_id, country_name,
            context_parent_key, context_parent_name, status, created_at, updated_at
        ) VALUES (
            :externalSystemCode, :entityType, :rawKey, :sourceId, :rawName,
            :sportId, :sportSlug, :countryId, :countryName,
            :contextParentKey, :contextParentName, 'PENDING', NOW(), NOW()
        )
        ON CONFLICT (external_system_code, entity_type, raw_key)
        DO UPDATE SET
            source_id = COALESCE(:sourceId, mdm_unmapped_queue.source_id),
            raw_name = COALESCE(:rawName, mdm_unmapped_queue.raw_name),
            sport_id = COALESCE(:sportId, mdm_unmapped_queue.sport_id),
            sport_slug = COALESCE(:sportSlug, mdm_unmapped_queue.sport_slug),
            country_id = COALESCE(:countryId, mdm_unmapped_queue.country_id),
            country_name = COALESCE(:countryName, mdm_unmapped_queue.country_name),
            context_parent_key = COALESCE(:contextParentKey, mdm_unmapped_queue.context_parent_key),
            context_parent_name = COALESCE(:contextParentName, mdm_unmapped_queue.context_parent_name),
            updated_at = NOW()
        """, nativeQuery = true)
    int upsertUnmapped(
            @Param("externalSystemCode") String externalSystemCode,
            @Param("entityType") String entityType,
            @Param("rawKey") String rawKey,
            @Param("sourceId") String sourceId,
            @Param("rawName") String rawName,
            @Param("sportId") Integer sportId,
            @Param("sportSlug") String sportSlug,
            @Param("countryId") Integer countryId,
            @Param("countryName") String countryName,
            @Param("contextParentKey") String contextParentKey,
            @Param("contextParentName") String contextParentName
    );

    @Modifying
    @Query("UPDATE MdmUnmappedQueue q SET q.status = :status, q.resolutionNotes = :notes, q.updatedAt = CURRENT_TIMESTAMP WHERE q.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("notes") String notes);
}

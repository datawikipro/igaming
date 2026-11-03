/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmUnmappedQueue;

public interface MdmUnmappedQueueRepository
extends JpaRepository<MdmUnmappedQueue, Long> {
    public Optional<MdmUnmappedQueue> findByExternalSystemCodeAndEntityTypeAndRawKey(String var1, String var2, String var3);

    public List<MdmUnmappedQueue> findByStatusOrderByCreatedAtAsc(String var1, Pageable var2);

    public List<MdmUnmappedQueue> findByStatusAndEntityTypeOrderByCreatedAtAsc(String var1, String var2, Pageable var3);

    public List<MdmUnmappedQueue> findByStatusAndExternalSystemCodeOrderByCreatedAtAsc(String var1, String var2, Pageable var3);

    public List<MdmUnmappedQueue> findByStatusAndEntityTypeAndExternalSystemCodeOrderByCreatedAtAsc(String var1, String var2, String var3, Pageable var4);

    @Modifying
    @Query(value="INSERT INTO mdm_unmapped_queue (\n    external_system_code, entity_type, raw_key, source_id, raw_name,\n    sport_id, sport_slug, country_id, country_name,\n    context_parent_key, context_parent_name, status, created_at, updated_at\n) VALUES (\n    :externalSystemCode, :entityType, :rawKey, :sourceId, :rawName,\n    :sportId, :sportSlug, :countryId, :countryName,\n    :contextParentKey, :contextParentName, 'PENDING', NOW(), NOW()\n)\nON CONFLICT (external_system_code, entity_type, raw_key)\nDO UPDATE SET\n    source_id = COALESCE(:sourceId, mdm_unmapped_queue.source_id),\n    raw_name = COALESCE(:rawName, mdm_unmapped_queue.raw_name),\n    sport_id = COALESCE(:sportId, mdm_unmapped_queue.sport_id),\n    sport_slug = COALESCE(:sportSlug, mdm_unmapped_queue.sport_slug),\n    country_id = COALESCE(:countryId, mdm_unmapped_queue.country_id),\n    country_name = COALESCE(:countryName, mdm_unmapped_queue.country_name),\n    context_parent_key = COALESCE(:contextParentKey, mdm_unmapped_queue.context_parent_key),\n    context_parent_name = COALESCE(:contextParentName, mdm_unmapped_queue.context_parent_name),\n    updated_at = NOW()\n", nativeQuery=true)
    public int upsertUnmapped(@Param(value="externalSystemCode") String var1, @Param(value="entityType") String var2, @Param(value="rawKey") String var3, @Param(value="sourceId") String var4, @Param(value="rawName") String var5, @Param(value="sportId") Integer var6, @Param(value="sportSlug") String var7, @Param(value="countryId") Integer var8, @Param(value="countryName") String var9, @Param(value="contextParentKey") String var10, @Param(value="contextParentName") String var11);

    @Modifying
    @Query(value="UPDATE MdmUnmappedQueue q SET q.status = :status, q.resolutionNotes = :notes, q.updatedAt = CURRENT_TIMESTAMP WHERE q.id = :id")
    public int updateStatus(@Param(value="id") Long var1, @Param(value="status") String var2, @Param(value="notes") String var3);
}


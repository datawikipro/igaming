package pro.datawiki.igaming.llm.admin.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmTask;
import pro.datawiki.igaming.llm.admin.dto.TaskGroupCount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LlmTaskRepository extends JpaRepository<LlmTask, UUID> {

    @Query("""
        SELECT new pro.datawiki.igaming.llm.admin.dto.TaskGroupCount(t.modelId, t.modelName, t.modelName, t.providerType, t.status, COUNT(t))
        FROM LlmTask t
        GROUP BY t.modelId, t.modelName, t.providerType, t.status
    """)
    List<TaskGroupCount> getGroupCounts();

    @Query("""
        SELECT new pro.datawiki.igaming.llm.admin.dto.TaskGroupCount(t.modelId, t.modelName, t.modelName, t.providerType, 'COMPLETED_HOUR', COUNT(t))
        FROM LlmTask t
        WHERE t.status = 'COMPLETED' AND t.updatedAt >= :since
        GROUP BY t.modelId, t.modelName, t.providerType
    """)
    List<TaskGroupCount> getCompletedLastHour(@Param("since") LocalDateTime since);

    @Query("""
        SELECT t FROM LlmTask t
        WHERE t.promptHash = :hash
          AND t.status = 'COMPLETED'
          AND (t.permanent = true OR t.expiresAt > CURRENT_TIMESTAMP)
        ORDER BY t.createdAt DESC
        LIMIT 1
    """)
    Optional<LlmTask> findCachedByHash(@Param("hash") String promptHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
        SELECT t FROM LlmTask t
        WHERE t.providerType = :providerType
          AND t.modelName = :modelName
          AND t.status = 'PENDING'
        ORDER BY t.createdAt ASC
        LIMIT 1
    """)
    Optional<LlmTask> claimNextTask(
            @Param("providerType") String providerType,
            @Param("modelName") String modelName
    );
}

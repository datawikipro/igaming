package pro.datawiki.igaming.llm.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmQueueSubscription;

import java.util.List;

@Repository
public interface LlmQueueSubscriptionRepository extends JpaRepository<LlmQueueSubscription, Long> {

    @Query("""
        SELECT s.queueName FROM LlmQueueSubscription s
        WHERE s.model.modelId = :modelName
          AND s.active = true
        ORDER BY s.priority ASC
    """)
    List<String> findActiveQueueNamesByModelName(@Param("modelName") String modelName);
}

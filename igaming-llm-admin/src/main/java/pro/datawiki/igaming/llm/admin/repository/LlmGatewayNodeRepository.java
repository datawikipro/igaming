package pro.datawiki.igaming.llm.admin.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;

import java.util.List;
import java.util.Optional;

@Repository
public interface LlmGatewayNodeRepository extends JpaRepository<LlmGatewayNode, Long> {

    Optional<LlmGatewayNode> findByName(String name);

    List<LlmGatewayNode> findByActiveTrue();

    @Query("SELECT n FROM LlmGatewayNode n WHERE n.active = true AND n.model.provider.name = :providerType")
    List<LlmGatewayNode> findActiveNodesByProviderType(@Param("providerType") String providerType);

    Optional<LlmGatewayNode> findByLeasedByPod(String leasedByPod);

    /**
     * Find the first IDLE node for the given provider and lock it at DB level (SELECT FOR UPDATE).
     * This prevents the race condition where multiple pods simultaneously read the same IDLE node
     * before any transaction commits. The lock is held until the surrounding transaction commits.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM LlmGatewayNode n WHERE n.active = true AND n.model.provider.name = :providerType AND n.status = 'IDLE' ORDER BY n.id ASC")
    List<LlmGatewayNode> findAvailableNodesForUpdate(@Param("providerType") String providerType);

    /**
     * Non-locking version for read-only listing (health checks, UI, etc.)
     */
    @Query("SELECT n FROM LlmGatewayNode n WHERE n.active = true AND n.model.provider.name = :providerType AND n.status = 'IDLE'")
    List<LlmGatewayNode> findAvailableNodes(@Param("providerType") String providerType);

    List<LlmGatewayNode> findByModelId(Long modelId);
}

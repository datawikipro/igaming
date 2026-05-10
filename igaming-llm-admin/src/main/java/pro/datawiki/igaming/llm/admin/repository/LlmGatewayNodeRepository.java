package pro.datawiki.igaming.llm.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;

import java.util.List;
import java.util.Optional;

@Repository
public interface LlmGatewayNodeRepository extends JpaRepository<LlmGatewayNode, Long> {

    Optional<LlmGatewayNode> findByName(String name);

    List<LlmGatewayNode> findByActiveTrue();

    @Query("SELECT n FROM LlmGatewayNode n WHERE n.active = true AND n.providerType = :providerType")
    List<LlmGatewayNode> findActiveNodesByProviderType(String providerType);

    Optional<LlmGatewayNode> findByLeasedByPod(String leasedByPod);

    @Query("SELECT n FROM LlmGatewayNode n WHERE n.active = true AND n.providerType = :providerType AND n.status = 'HEALTHY' AND n.leasedByPod IS NULL")
    List<LlmGatewayNode> findAvailableNodes(String providerType);
}

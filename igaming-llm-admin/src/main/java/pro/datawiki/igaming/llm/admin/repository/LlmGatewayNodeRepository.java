package pro.datawiki.igaming.llm.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT n FROM LlmGatewayNode n WHERE n.active = true AND n.model.provider.name = :providerType AND n.status = 'HEALTHY' AND n.leasedByPod IS NULL")
    List<LlmGatewayNode> findAvailableNodes(@Param("providerType") String providerType);

    List<LlmGatewayNode> findByModelId(Long modelId);
}

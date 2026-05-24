package pro.datawiki.igaming.llm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.domain.LlmModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface LlmModelRepository extends JpaRepository<LlmModel, Long> {

    List<LlmModel> findByProviderIdAndActiveTrue(Long providerId);

    Optional<LlmModel> findByModelIdAndActiveTrue(String modelId);

    @Query("SELECT m FROM LlmModel m JOIN FETCH m.provider WHERE m.modelId = :modelId AND m.active = true")
    List<LlmModel> findWithProviderByModelId(@Param("modelId") String modelId);

    @Query("SELECT m FROM LlmModel m WHERE m.provider.name = :providerName AND m.active = true ORDER BY m.id ASC LIMIT 1")
    Optional<LlmModel> findFirstByProviderName(@Param("providerName") String providerName);
}

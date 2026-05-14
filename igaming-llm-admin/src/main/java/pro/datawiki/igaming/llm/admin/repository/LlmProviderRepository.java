package pro.datawiki.igaming.llm.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmProvider;

import java.util.Optional;

@Repository
public interface LlmProviderRepository extends JpaRepository<LlmProvider, Long> {
    Optional<LlmProvider> findByNameAndActiveTrue(String name);
}

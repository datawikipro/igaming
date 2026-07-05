package pro.datawiki.accounts.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.accounts.domain.entity.LlmQuotaSnapshot;

import java.util.List;
import java.util.Optional;

@Repository
public interface LlmQuotaSnapshotRepository extends JpaRepository<LlmQuotaSnapshot, Long> {

    Optional<LlmQuotaSnapshot> findTopByLlmCredentialIdOrderByTimestampDesc(Long llmCredentialId);

    List<LlmQuotaSnapshot> findTop10ByLlmCredentialIdOrderByTimestampDesc(Long llmCredentialId);
}

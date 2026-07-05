package pro.datawiki.accounts.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.accounts.domain.entity.LlmCredential;

import java.util.List;

@Repository
public interface LlmCredentialRepository extends JpaRepository<LlmCredential, Long> {

    List<LlmCredential> findByAccountId(Long accountId);

    List<LlmCredential> findByIsActiveTrue();
}

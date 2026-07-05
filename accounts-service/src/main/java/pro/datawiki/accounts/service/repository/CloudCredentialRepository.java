package pro.datawiki.accounts.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.accounts.domain.entity.CloudCredential;

import java.util.List;

@Repository
public interface CloudCredentialRepository extends JpaRepository<CloudCredential, Long> {

    List<CloudCredential> findByAccountId(Long accountId);

    List<CloudCredential> findByIsActiveTrueOrderByPriorityAsc();
}

package pro.datawiki.igaming.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.infra.entity.CloudAccount;

import java.util.List;

@Repository
public interface CloudAccountRepository extends JpaRepository<CloudAccount, Long> {
    List<CloudAccount> findByIsActiveTrueOrderByPriorityAsc();
    List<CloudAccount> findByIsActiveTrueAndProviderOrderByPriorityAsc(String provider);
}

package pro.datawiki.accounts.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.accounts.domain.entity.CloudSnapshot;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloudSnapshotRepository extends JpaRepository<CloudSnapshot, Long> {

    Optional<CloudSnapshot> findTopByCloudCredentialIdOrderByTimestampDesc(Long cloudCredentialId);

    List<CloudSnapshot> findTop10ByCloudCredentialIdOrderByTimestampDesc(Long cloudCredentialId);
}

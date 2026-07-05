package pro.datawiki.accounts.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.accounts.domain.entity.ManagedServer;

import java.util.List;

@Repository
public interface ManagedServerRepository extends JpaRepository<ManagedServer, String> {

    List<ManagedServer> findByAccount_Id(Long accountId);

    List<ManagedServer> findByStatusNot(String status);
}

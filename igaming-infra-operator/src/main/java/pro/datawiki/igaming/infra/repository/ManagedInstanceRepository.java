package pro.datawiki.igaming.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.infra.entity.ManagedInstance;

import java.util.List;

public interface ManagedInstanceRepository extends JpaRepository<ManagedInstance, String> {
    List<ManagedInstance> findByStatus(String status);
}

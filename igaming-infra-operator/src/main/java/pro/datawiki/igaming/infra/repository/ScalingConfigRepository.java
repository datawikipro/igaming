package pro.datawiki.igaming.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.infra.entity.ScalingConfig;

public interface ScalingConfigRepository extends JpaRepository<ScalingConfig, String> {
}

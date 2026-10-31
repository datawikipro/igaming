package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.AiUserStrategySetting;

@Repository
public interface AiUserStrategySettingRepository extends JpaRepository<AiUserStrategySetting, Long> {
}

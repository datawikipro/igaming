package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.MatchExternalContextEntity;

import java.util.List;

@Repository
public interface MatchExternalContextRepository extends JpaRepository<MatchExternalContextEntity, Long> {

    @Query("""
            SELECT c FROM MatchExternalContextEntity c
            WHERE c.oddsQualityGrade IN ('INSIGHT_WARNING', 'ELITE_VALUE', 'PUBLIC_TRAP')
               OR c.newsRadarTriggered = true
               OR ABS(COALESCE(c.lineDriftPct, 0)) >= 6.0
               OR c.weatherRiskLevel IN ('HEAVY_RAIN', 'HIGH_WIND', 'EXTREME_HEAT', 'FREEZING')
               OR ABS(COALESCE(c.metroBiasIndex, 0)) >= 1.0
            ORDER BY
               CASE WHEN c.oddsQualityGrade = 'INSIGHT_WARNING' THEN 0
                    WHEN c.oddsQualityGrade = 'ELITE_VALUE' THEN 1
                    WHEN c.newsRadarTriggered = true THEN 2
                    ELSE 3 END,
               c.updatedAt DESC
            """)
    List<MatchExternalContextEntity> findRadarAnomalies(Pageable pageable);
}

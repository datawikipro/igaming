package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.AiPolicyIntervention;

import java.util.List;
import java.util.Map;

@Repository
public interface AiPolicyInterventionRepository extends JpaRepository<AiPolicyIntervention, Long> {

    List<AiPolicyIntervention> findByMatchId(Long matchId);

    List<AiPolicyIntervention> findByRecommendationId(Long recommendationId);

    Page<AiPolicyIntervention> findByOrderByCreatedAtDesc(Pageable pageable);

    Page<AiPolicyIntervention> findByRuleCodeOrderByCreatedAtDesc(String ruleCode, Pageable pageable);

    @Query(value = """
        SELECT
            rule_code as ruleCode,
            COUNT(*) as totalCount,
            COUNT(*) FILTER (WHERE result_status = 'WON') as wonCount,
            COUNT(*) FILTER (WHERE result_status = 'LOST') as lostCount,
            COUNT(*) FILTER (WHERE result_status = 'VOID') as voidCount,
            COUNT(*) FILTER (WHERE result_status = 'PENDING') as pendingCount,
            COALESCE(SUM(policy_impact_pnl), 0) as netPolicyImpactRub,
            COALESCE(SUM(raw_stake_rub - applied_stake_rub) FILTER (WHERE result_status = 'LOST'), 0) as totalLossesPreventedRub
        FROM ai_policy_intervention
        GROUP BY rule_code
        ORDER BY totalCount DESC
    """, nativeQuery = true)
    List<Map<String, Object>> getPolicyInterventionSummary();
}

package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.AiStrategyVote;

import java.util.List;
import java.util.Map;

@Repository
public interface AiStrategyVoteRepository extends JpaRepository<AiStrategyVote, Long> {

    List<AiStrategyVote> findByRecommendationId(Long recommendationId);

    List<AiStrategyVote> findByRecommendationIdIn(List<Long> recommendationIds);

    List<AiStrategyVote> findByMatchId(Long matchId);

    List<AiStrategyVote> findByStrategySlug(String strategySlug);

    @Query(value = """
        SELECT 
            v.strategy_slug as strategySlug,
            t.icon as icon,
            v.strategy_name as strategyName,
            COUNT(*) FILTER (WHERE v.vote_decision = 'PICK') as totalPicks,
            COUNT(*) FILTER (WHERE v.result_status = 'WON') as wonCount,
            COUNT(*) FILTER (WHERE v.result_status = 'LOST') as lostCount,
            COUNT(*) FILTER (WHERE v.result_status = 'VOID') as voidCount,
            ROUND(COUNT(*) FILTER (WHERE v.result_status = 'WON')::numeric / NULLIF(COUNT(*) FILTER (WHERE v.result_status IN ('WON', 'LOST')), 0) * 100, 1) as winratePct,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric, 2), 0) as totalPnlRub,
            ROUND(AVG(v.recommended_odd) FILTER (WHERE v.vote_decision = 'PICK')::numeric, 2) as avgOdd
        FROM ai_strategy_vote v
        JOIN ai_strategy_template t ON v.strategy_slug = t.slug
        GROUP BY v.strategy_slug, t.icon, v.strategy_name, t.sort_order
        ORDER BY t.sort_order ASC
    """, nativeQuery = true)
    List<Map<String, Object>> getMultiStrategyPerformanceSummary();
}

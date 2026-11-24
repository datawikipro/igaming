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

    @Query("SELECT DISTINCT v.recommendationId FROM AiStrategyVote v")
    List<Long> findDistinctRecommendationIds();

    @Query(value = """
        SELECT 
            v.strategy_slug as strategySlug,
            t.icon as icon,
            v.strategy_name as strategyName,
            COUNT(*) FILTER (WHERE v.vote_decision = 'PICK') as totalPicks,
            COUNT(*) FILTER (WHERE v.result_status = 'WON') as wonCount,
            COUNT(*) FILTER (WHERE v.result_status = 'LOST') as lostCount,
            COUNT(*) FILTER (WHERE v.result_status = 'PUSH') as pushCount,
            COUNT(*) FILTER (WHERE v.result_status IN ('VOID', 'CANCELLED', 'POSTPONED')) as voidCount,
            ROUND(COUNT(*) FILTER (WHERE v.result_status = 'WON')::numeric / NULLIF(COUNT(*) FILTER (WHERE v.result_status IN ('WON', 'LOST')), 0) * 100, 1) as winratePct,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric, 2), 0) as totalPnlRub,
            COALESCE(ROUND(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH'))::numeric, 2), 0) as turnoverRub,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric / NULLIF(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH')), 0) * 100, 1), 0.0) as roiPct,
            ROUND(AVG(v.recommended_odd) FILTER (WHERE v.vote_decision = 'PICK')::numeric, 2) as avgOdd
        FROM ai_strategy_vote v
        JOIN ai_strategy_template t ON v.strategy_slug = t.slug
        GROUP BY v.strategy_slug, t.icon, v.strategy_name, t.sort_order
        ORDER BY t.sort_order ASC
    """, nativeQuery = true)
    List<Map<String, Object>> getMultiStrategyPerformanceSummary();

    @Query(value = """
        SELECT 
            v.strategy_slug as strategySlug,
            t.icon as icon,
            v.strategy_name as strategyName,
            COUNT(*) FILTER (WHERE v.vote_decision = 'PICK') as totalPicks,
            COUNT(*) FILTER (WHERE v.result_status = 'WON') as wonCount,
            COUNT(*) FILTER (WHERE v.result_status = 'LOST') as lostCount,
            COUNT(*) FILTER (WHERE v.result_status = 'PUSH') as pushCount,
            COUNT(*) FILTER (WHERE v.result_status IN ('VOID', 'CANCELLED', 'POSTPONED')) as voidCount,
            ROUND(COUNT(*) FILTER (WHERE v.result_status = 'WON')::numeric / NULLIF(COUNT(*) FILTER (WHERE v.result_status IN ('WON', 'LOST')), 0) * 100, 1) as winratePct,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric, 2), 0) as totalPnlRub,
            COALESCE(ROUND(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH'))::numeric, 2), 0) as turnoverRub,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric / NULLIF(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH')), 0) * 100, 1), 0.0) as roiPct,
            ROUND(AVG(v.recommended_odd) FILTER (WHERE v.vote_decision = 'PICK')::numeric, 2) as avgOdd
        FROM ai_strategy_vote v
        JOIN ai_strategy_template t ON v.strategy_slug = t.slug
        WHERE v.model_version = :version
        GROUP BY v.strategy_slug, t.icon, v.strategy_name, t.sort_order
        ORDER BY t.sort_order ASC
    """, nativeQuery = true)
    List<Map<String, Object>> getMultiStrategyPerformanceSummaryByVersion(@org.springframework.data.repository.query.Param("version") String version);

    @Query(value = """
        SELECT 
            v.strategy_slug as strategySlug,
            t.icon as icon,
            v.strategy_name as strategyName,
            COUNT(*) FILTER (WHERE v.vote_decision = 'PICK') as totalPicks,
            COUNT(*) FILTER (WHERE v.result_status = 'WON') as wonCount,
            COUNT(*) FILTER (WHERE v.result_status = 'LOST') as lostCount,
            COUNT(*) FILTER (WHERE v.result_status = 'PUSH') as pushCount,
            COUNT(*) FILTER (WHERE v.result_status IN ('VOID', 'CANCELLED', 'POSTPONED')) as voidCount,
            ROUND(COUNT(*) FILTER (WHERE v.result_status = 'WON')::numeric / NULLIF(COUNT(*) FILTER (WHERE v.result_status IN ('WON', 'LOST')), 0) * 100, 1) as winratePct,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric, 2), 0) as totalPnlRub,
            COALESCE(ROUND(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH'))::numeric, 2), 0) as turnoverRub,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric / NULLIF(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH')), 0) * 100, 1), 0.0) as roiPct,
            ROUND(AVG(v.recommended_odd) FILTER (WHERE v.vote_decision = 'PICK')::numeric, 2) as avgOdd
        FROM ai_strategy_vote v
        JOIN match_ai_recommendation r ON r.id = v.recommendation_id
        JOIN ai_strategy_template t ON v.strategy_slug = t.slug
        WHERE LOWER(r.sport_slug) = LOWER(:sportSlug) AND v.model_version = :version
        GROUP BY v.strategy_slug, t.icon, v.strategy_name, t.sort_order
        ORDER BY t.sort_order ASC
    """, nativeQuery = true)
    List<Map<String, Object>> getMultiStrategyPerformanceSummaryBySportAndVersion(
            @org.springframework.data.repository.query.Param("sportSlug") String sportSlug,
            @org.springframework.data.repository.query.Param("version") String version);

    @Query(value = """
        SELECT 
            v.strategy_slug as strategySlug,
            t.icon as icon,
            v.strategy_name as strategyName,
            COUNT(*) FILTER (WHERE v.vote_decision = 'PICK') as totalPicks,
            COUNT(*) FILTER (WHERE v.result_status = 'WON') as wonCount,
            COUNT(*) FILTER (WHERE v.result_status = 'LOST') as lostCount,
            COUNT(*) FILTER (WHERE v.result_status = 'PUSH') as pushCount,
            COUNT(*) FILTER (WHERE v.result_status IN ('VOID', 'CANCELLED', 'POSTPONED')) as voidCount,
            ROUND(COUNT(*) FILTER (WHERE v.result_status = 'WON')::numeric / NULLIF(COUNT(*) FILTER (WHERE v.result_status IN ('WON', 'LOST')), 0) * 100, 1) as winratePct,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric, 2), 0) as totalPnlRub,
            COALESCE(ROUND(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH'))::numeric, 2), 0) as turnoverRub,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric / NULLIF(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH')), 0) * 100, 1), 0.0) as roiPct,
            ROUND(AVG(v.recommended_odd) FILTER (WHERE v.vote_decision = 'PICK')::numeric, 2) as avgOdd
        FROM ai_strategy_vote v
        JOIN match_ai_recommendation r ON r.id = v.recommendation_id
        JOIN ai_strategy_template t ON v.strategy_slug = t.slug
        WHERE LOWER(r.sport_slug) = LOWER(:sportSlug)
        GROUP BY v.strategy_slug, t.icon, v.strategy_name, t.sort_order
        ORDER BY t.sort_order ASC
    """, nativeQuery = true)
    List<Map<String, Object>> getMultiStrategyPerformanceSummaryBySport(@org.springframework.data.repository.query.Param("sportSlug") String sportSlug);

    @Query(value = """
        SELECT 
            COALESCE(r.sport_slug, 'unknown') as sportSlug,
            v.strategy_slug as strategySlug,
            t.icon as icon,
            v.strategy_name as strategyName,
            COUNT(*) FILTER (WHERE v.vote_decision = 'PICK') as totalPicks,
            COUNT(*) FILTER (WHERE v.result_status = 'WON') as wonCount,
            COUNT(*) FILTER (WHERE v.result_status = 'LOST') as lostCount,
            COUNT(*) FILTER (WHERE v.result_status = 'PUSH') as pushCount,
            COUNT(*) FILTER (WHERE v.result_status IN ('VOID', 'CANCELLED', 'POSTPONED')) as voidCount,
            ROUND(COUNT(*) FILTER (WHERE v.result_status = 'WON')::numeric / NULLIF(COUNT(*) FILTER (WHERE v.result_status IN ('WON', 'LOST')), 0) * 100, 1) as winratePct,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric, 2), 0) as totalPnlRub,
            COALESCE(ROUND(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH'))::numeric, 2), 0) as turnoverRub,
            COALESCE(ROUND(SUM(v.actual_pnl)::numeric / NULLIF(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'PUSH')), 0) * 100, 1), 0.0) as roiPct,
            ROUND(AVG(v.recommended_odd) FILTER (WHERE v.vote_decision = 'PICK')::numeric, 2) as avgOdd
        FROM ai_strategy_vote v
        JOIN match_ai_recommendation r ON r.id = v.recommendation_id
        JOIN ai_strategy_template t ON v.strategy_slug = t.slug
        GROUP BY r.sport_slug, v.strategy_slug, t.icon, v.strategy_name, t.sort_order
        ORDER BY r.sport_slug, t.sort_order ASC
    """, nativeQuery = true)
    List<Map<String, Object>> getAllSportsPerformanceBreakdown();
}

package pro.datawiki.igaming.analyst.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.dto.AnalystStatsDto;
import pro.datawiki.igaming.analyst.dto.ModelVersionDto;
import pro.datawiki.igaming.analyst.entity.AiModelVersion;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiModelVersionRepository;
import pro.datawiki.igaming.analyst.repository.BookmakerQueryFilter;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.dto.bookmaker.BookmakerCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Dedicated statistics and reporting service for AI Analyst recommendations.
 * Aggregates performance KPIs across confidence tiers, sports, bookmakers, and stages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalystStatisticsService {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final AiModelVersionRepository aiModelVersionRepository;
    private final JdbcTemplate jdbcTemplate;

    public enum MarketCategory {
        DRAWS_AND_OT {
            @Override
            public String sqlCondition(String col) {
                return String.format(
                        "(%s IN ('DRAW', 'X', 'PX', 'DC_1X', 'DC_X2', '1X', 'X2', 'DOUBLE_CHANCE_1X', 'DOUBLE_CHANCE_X2', 'HT_DRAW', 'HALFTIME_DRAW') " +
                        "OR %s LIKE 'DRAW_%%' OR %s LIKE '%%_DRAW' OR %s LIKE 'HT_DRAW%%' OR %s LIKE 'HALFTIME_DRAW%%')",
                        col, col, col, col, col
                );
            }
        },
        ASIAN_HANDICAP {
            @Override
            public String sqlCondition(String col) {
                return String.format(
                        "(%s LIKE 'AH_%%' OR %s LIKE 'HANDICAP_%%' OR %s LIKE 'ASIAN_%%' OR %s LIKE '%%_AH_%%' OR %s LIKE '%%_HANDICAP_%%')",
                        col, col, col, col, col
                );
            }
        },
        TOTALS_CORRIDORS {
            @Override
            public String sqlCondition(String col) {
                return String.format(
                        "(%s LIKE 'TOTAL_%%' OR %s LIKE 'TO_%%' OR %s LIKE 'TU_%%' OR %s LIKE 'OVER_%%' OR %s LIKE 'UNDER_%%' " +
                        "OR %s LIKE '%%_TOTAL_%%' OR %s LIKE '%%_OVER_%%' OR %s LIKE '%%_UNDER_%%')",
                        col, col, col, col, col, col, col, col
                );
            }
        };

        public abstract String sqlCondition(String col);

        public static Optional<MarketCategory> fromString(String val) {
            if (val == null || val.isBlank()) return Optional.empty();
            try {
                return Optional.of(MarketCategory.valueOf(val.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    private static final String ACTIVE_OPPORTUNITY_CONDITION = """
        (
            conviction_tier != 'PASS'
            AND recommended_market != 'PASS'
            AND (evaluation_stage IS NULL OR evaluation_stage NOT IN (
                'SETTLED', 'AWAITING_SETTLEMENT', 'CANCELLED', 'SKIPPED',
                'CANCELLED_SCHEDULE_CONFLICT', 'CANCELLED_SYNTHETIC_COLLISION',
                'SCHEDULE_CONFLICT_PENDING_RESOLUTION'
            ))
            AND (final_score IS NULL OR TRIM(final_score) = '')
            AND (
                match_start_time > NOW() AT TIME ZONE 'UTC'
                OR (
                    match_start_time <= NOW() AT TIME ZONE 'UTC'
                    AND (
                        (UPPER(sport_name) = 'TABLE_TENNIS' AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '30 minutes')
                        OR (UPPER(sport_name) = 'SHORT_FOOTBALL' AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '40 minutes')
                        OR (UPPER(sport_name) IN ('MMA', 'COMBAT', 'BOXING') AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '50 minutes')
                        OR (UPPER(sport_name) = 'FOOTBALL' AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '120 minutes')
                        OR (UPPER(sport_name) IN ('BASKETBALL', 'BASKETBALL_48M') AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '150 minutes')
                        OR (UPPER(sport_name) = 'HOCKEY' AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '160 minutes')
                        OR (UPPER(sport_name) = 'TENNIS' AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '180 minutes')
                        OR (match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '120 minutes')
                    )
                )
            )
        )
    """;

    private static final String STRATEGY_ACTIVE_OPPORTUNITY_CONDITION = """
        (
            v.result_status = 'PENDING'
            AND v.vote_decision = 'PICK'
            AND v.market != 'PASS'
            AND (r.evaluation_stage IS NULL OR r.evaluation_stage NOT IN (
                'SETTLED', 'AWAITING_SETTLEMENT', 'CANCELLED_SCHEDULE_CONFLICT',
                'CANCELLED_SYNTHETIC_COLLISION', 'SCHEDULE_CONFLICT_PENDING_RESOLUTION'
            ))
            AND (r.final_score IS NULL OR TRIM(r.final_score) = '')
            AND (
                r.match_start_time > NOW() AT TIME ZONE 'UTC'
                OR (
                    r.match_start_time <= NOW() AT TIME ZONE 'UTC'
                    AND (
                        (UPPER(r.sport_name) = 'TABLE_TENNIS' AND r.match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '30 minutes')
                        OR (UPPER(r.sport_name) = 'SHORT_FOOTBALL' AND r.match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '40 minutes')
                        OR (UPPER(r.sport_name) IN ('MMA', 'COMBAT', 'BOXING') AND r.match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '50 minutes')
                        OR (UPPER(r.sport_name) = 'FOOTBALL' AND r.match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '120 minutes')
                        OR (UPPER(r.sport_name) IN ('BASKETBALL', 'BASKETBALL_48M') AND r.match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '150 minutes')
                        OR (UPPER(r.sport_name) = 'HOCKEY' AND r.match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '160 minutes')
                        OR (UPPER(r.sport_name) = 'TENNIS' AND r.match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '180 minutes')
                        OR (r.match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '120 minutes')
                    )
                )
            )
        )
    """;

    public List<ModelVersionDto> getModelVersions() {
        List<AiModelVersion> entities = aiModelVersionRepository.findAllByOrderByReleaseDateDesc();
        List<ModelVersionDto> dtos = new ArrayList<>();

        for (AiModelVersion v : entities) {
            AnalystStatsDto stats = getPerformanceStats(null, null, null, null, null, null, null, null, v.getVersionCode(), StrategyEngineService.DEFAULT_STRATEGY_SLUG);
            long won = stats != null ? stats.getWon() : 0L;
            long lost = stats != null ? stats.getLost() : 0L;
            long voidCount = stats != null ? stats.getVoidCount() : 0L;
            long settled = won + lost + voidCount;

            ModelVersionDto dto = ModelVersionDto.builder()
                    .versionCode(v.getVersionCode())
                    .displayName(v.getDisplayName())
                    .ideologyVersion(v.getIdeologyVersion())
                    .vectorVersion(v.getVectorVersion())
                    .loaderVersion(v.getLoaderVersion())
                    .iterationNumber(v.getIterationNumber())
                    .releaseDate(v.getReleaseDate())
                    .isActive(v.isActive())
                    .description(v.getDescription())
                    .changelog(v.getChangelog())
                    .totalPicks(settled)
                    .wonCount(won)
                    .lostCount(lost)
                    .voidCount(voidCount)
                    .winratePct(stats != null ? stats.getWinratePct() : 0.0)
                    .roiPct(stats != null ? stats.getRoiPct() : 0.0)
                    .totalPnlRub(stats != null ? stats.getTotalPnl() : BigDecimal.ZERO)
                    .build();
            dtos.add(dto);
        }

        return dtos;
    }

    public AnalystStatsDto getPerformanceStats() {
        return getPerformanceStats(null, null, null, null, null, null, null, null, null, StrategyEngineService.DEFAULT_STRATEGY_SLUG);
    }

    public AnalystStatsDto getPerformanceStats(
            String status, String sport, String stage, String timeRange,
            String oddRange, String bookmaker, String marketCategory, String query) {
        return getPerformanceStats(status, sport, stage, timeRange, oddRange, bookmaker, marketCategory, query, null, StrategyEngineService.DEFAULT_STRATEGY_SLUG);
    }

    public AnalystStatsDto getPerformanceStats(
            String status, String sport, String stage, String timeRange,
            String oddRange, String bookmaker, String marketCategory, String query, String version) {
        return getPerformanceStats(status, sport, stage, timeRange, oddRange, bookmaker, marketCategory, query, version, StrategyEngineService.DEFAULT_STRATEGY_SLUG);
    }

    public AnalystStatsDto getPerformanceStats(
            String status, String sport, String stage, String timeRange,
            String oddRange, String bookmaker, String marketCategory, String query, String version, String strategy) {

        String effectiveStrategy = (strategy != null && !strategy.isBlank()) ? strategy.trim().toLowerCase(Locale.ROOT) : StrategyEngineService.DEFAULT_STRATEGY_SLUG;
        boolean isStrategyMode = !"RAW".equalsIgnoreCase(effectiveStrategy) && !"ALL".equalsIgnoreCase(effectiveStrategy);

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (isStrategyMode) {
            where.append(" AND v.strategy_slug = ? ");
            params.add(effectiveStrategy);
        }

        if (version != null && !version.isBlank() && !"ALL".equalsIgnoreCase(version)) {
            where.append(isStrategyMode ? " AND v.model_version = ? " : " AND model_version = ? ");
            params.add(version.trim());
        }

        if (sport != null && !sport.isBlank() && !"ALL".equalsIgnoreCase(sport)) {
            where.append(isStrategyMode ? " AND UPPER(r.sport_name) = UPPER(?) " : " AND UPPER(sport_name) = UPPER(?) ");
            params.add(sport.trim());
        }

        if (bookmaker != null && !bookmaker.isBlank() && !"ALL".equalsIgnoreCase(bookmaker)) {
            String bm = bookmaker.trim();
            String prefix = isStrategyMode ? "r." : "";
            if ("HAS_BOOKMAKER".equalsIgnoreCase(bm) || "WITH_ODDS".equalsIgnoreCase(bm) || "BOOKMAKERS_ONLY".equalsIgnoreCase(bm)) {
                where.append(String.format(" AND ((%savailable_bookmakers IS NOT NULL AND %savailable_bookmakers != '') OR (%sbest_bookmaker IS NOT NULL AND %sbest_bookmaker NOT IN ('NONE', 'Market Consensus', 'Market Consensus (Fair)', ''))) ", prefix, prefix, prefix, prefix));
            } else {
                List<String> terms = BookmakerQueryFilter.resolveSearchTerms(bm);
                if (!terms.isEmpty()) {
                    where.append(" AND (");
                    for (int i = 0; i < terms.size(); i++) {
                        if (i > 0) where.append(" OR ");
                        where.append(String.format("LOWER(%savailable_bookmakers) LIKE ? OR LOWER(%sbest_bookmaker) LIKE ?", prefix, prefix));
                        String pattern = "%" + terms.get(i).toLowerCase(Locale.ROOT) + "%";
                        params.add(pattern);
                        params.add(pattern);
                    }
                    where.append(") ");
                }
            }
        }

        if (query != null && !query.isBlank()) {
            String qPattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            if (isStrategyMode) {
                where.append(" AND (LOWER(r.team1_name) LIKE ? OR LOWER(r.team2_name) LIKE ? OR LOWER(r.league_name) LIKE ? OR LOWER(v.market) LIKE ? OR LOWER(r.core_hypothesis) LIKE ?) ");
            } else {
                where.append(" AND (LOWER(team1_name) LIKE ? OR LOWER(team2_name) LIKE ? OR LOWER(league_name) LIKE ? OR LOWER(recommended_market) LIKE ? OR LOWER(core_hypothesis) LIKE ?) ");
            }
            params.add(qPattern);
            params.add(qPattern);
            params.add(qPattern);
            params.add(qPattern);
            params.add(qPattern);
        }

        String pendingBase = isStrategyMode ? STRATEGY_ACTIVE_OPPORTUNITY_CONDITION : ACTIVE_OPPORTUNITY_CONDITION;
        StringBuilder pendingCond = new StringBuilder(pendingBase);

        String rPrefix = isStrategyMode ? "r." : "";

        if (stage != null && !stage.isBlank() && !"ALL".equalsIgnoreCase(stage)) {
            String st = stage.toUpperCase(Locale.ROOT).trim();
            if ("IN_PLAY".equals(st)) {
                pendingCond.append(String.format(" AND %smatch_start_time <= NOW() AT TIME ZONE 'UTC' ", rPrefix));
            } else if ("WAIT_KICKOFF".equals(st)) {
                pendingCond.append(String.format(" AND %sevaluation_stage = 'WAIT_KICKOFF' ", rPrefix));
            } else if ("KICKOFF_VERIFIED".equals(st)) {
                pendingCond.append(String.format(" AND %sevaluation_stage = 'KICKOFF_VERIFIED' ", rPrefix));
            } else if ("LINEUP_CONFIRMED".equals(st)) {
                pendingCond.append(String.format(" AND %sevaluation_stage IN ('LINEUP_CONFIRMED', 'STAGE_2_PREVIEW') ", rPrefix));
            } else if ("PROJECTED".equals(st)) {
                pendingCond.append(String.format(" AND %slineup_status = 'PROJECTED' ", rPrefix));
            } else if ("PRE_LINEUP".equals(st)) {
                pendingCond.append(String.format(" AND (%sevaluation_stage = 'PRE_LINEUP' OR (%smatch_start_time > NOW() AT TIME ZONE 'UTC' AND (%sevaluation_stage IS NULL OR %sevaluation_stage NOT IN ('LINEUP_CONFIRMED', 'STAGE_2_PREVIEW', 'KICKOFF_VERIFIED', 'WAIT_KICKOFF')))) ", rPrefix, rPrefix, rPrefix, rPrefix));
            }
        }

        if (timeRange != null && !timeRange.isBlank() && !"ALL".equalsIgnoreCase(timeRange)) {
            String tr = timeRange.toUpperCase(Locale.ROOT).trim();
            if ("LIVE".equals(tr)) {
                pendingCond.append(String.format(" AND %smatch_start_time <= NOW() AT TIME ZONE 'UTC' ", rPrefix));
            } else if ("1H".equals(tr) || "T_1H".equals(tr)) {
                pendingCond.append(String.format(" AND %smatch_start_time > NOW() AT TIME ZONE 'UTC' AND %smatch_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '1 hour' ", rPrefix, rPrefix));
            } else if ("4H".equals(tr) || "T_4H".equals(tr)) {
                pendingCond.append(String.format(" AND %smatch_start_time > NOW() AT TIME ZONE 'UTC' AND %smatch_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '4 hours' ", rPrefix, rPrefix));
            } else if ("24H".equals(tr) || "T_24H".equals(tr) || "TODAY".equals(tr)) {
                pendingCond.append(String.format(" AND %smatch_start_time > NOW() AT TIME ZONE 'UTC' AND %smatch_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '24 hours' ", rPrefix, rPrefix));
            } else if ("MORE".equals(tr) || "T_MORE".equals(tr)) {
                pendingCond.append(String.format(" AND %smatch_start_time > (NOW() AT TIME ZONE 'UTC') + INTERVAL '24 hours' ", rPrefix));
            }
        }

        String oddCol = isStrategyMode ? "v.recommended_odd" : "recommended_odd";
        if (oddRange != null && !oddRange.isBlank() && !"ALL".equalsIgnoreCase(oddRange)) {
            String or = oddRange.trim();
            if ("1.3-1.8".equals(or) || "ODD_1_3_1_8".equalsIgnoreCase(or)) {
                pendingCond.append(String.format(" AND %s >= 1.30 AND %s <= 1.80 ", oddCol, oddCol));
            } else if ("1.8-2.2".equals(or) || "ODD_1_8_2_2".equalsIgnoreCase(or)) {
                pendingCond.append(String.format(" AND %s > 1.80 AND %s <= 2.20 ", oddCol, oddCol));
            } else if ("2.2-3.0".equals(or) || "ODD_2_2_3_0".equalsIgnoreCase(or)) {
                pendingCond.append(String.format(" AND %s > 2.20 AND %s <= 3.00 ", oddCol, oddCol));
            } else if ("3.0+".equals(or) || "3.0_PLUS".equalsIgnoreCase(or) || "3+".equals(or)) {
                pendingCond.append(String.format(" AND %s > 3.00 ", oddCol));
            }
        }

        String marketCol = isStrategyMode ? "v.market" : "recommended_market";
        if (marketCategory != null && !marketCategory.isBlank() && !"ALL".equalsIgnoreCase(marketCategory)) {
            MarketCategory.fromString(marketCategory).ifPresent(mc ->
                    pendingCond.append(" AND ").append(mc.sqlCondition(marketCol)).append(" ")
            );
        }

        String fromClause = isStrategyMode
                ? "FROM ai_strategy_vote v JOIN match_ai_recommendation r ON r.id = v.recommendation_id"
                : "FROM match_ai_recommendation";
        String tierCol = isStrategyMode ? "COALESCE(r.conviction_tier, 'ML_60')" : "COALESCE(conviction_tier, 'ML_60')";
        String wonCond = isStrategyMode ? "v.result_status = 'WON'" : "result_status = 'WON'";
        String lostCond = isStrategyMode ? "v.result_status = 'LOST'" : "result_status = 'LOST'";
        String voidCond = isStrategyMode ? "v.result_status = 'VOID'" : "result_status = 'VOID'";
        String waitingCond = isStrategyMode ? "v.result_status = 'WAITING'" : "result_status = 'WAITING'";
        String skippedCond = isStrategyMode ? "(v.vote_decision = 'PASS' OR v.result_status = 'SKIPPED')" : "result_status = 'SKIPPED'";
        String pnlExpr = isStrategyMode ? "COALESCE(SUM(v.actual_pnl) FILTER (WHERE v.vote_decision = 'PICK'), 0)" : "COALESCE(SUM(actual_pnl), 0)";
        String oddExpr = isStrategyMode ? "AVG(v.recommended_odd) FILTER (WHERE v.vote_decision = 'PICK')" : "AVG(recommended_odd) FILTER (WHERE conviction_tier != 'PASS' AND conviction_tier != 'WAIT')";
        String totalCountExpr = isStrategyMode ? "COUNT(*) FILTER (WHERE v.vote_decision = 'PICK')" : "COUNT(*)";
        String turnoverExpr = isStrategyMode ? "COALESCE(SUM(v.stake_rub) FILTER (WHERE v.result_status IN ('WON', 'LOST', 'VOID')), 0)" : "0";

        String sql = String.format("""
            SELECT
                %s as tier,
                %s as total_count,
                COUNT(*) FILTER (WHERE %s) as won_count,
                COUNT(*) FILTER (WHERE %s) as lost_count,
                COUNT(*) FILTER (WHERE %s) as void_count,
                COUNT(*) FILTER (WHERE %s) as pending_count,
                COUNT(*) FILTER (WHERE %s) as waiting_count,
                COUNT(*) FILTER (WHERE %s) as skipped_count,
                %s as total_pnl_rub,
                %s as turnover_rub,
                %s as avg_odd
            %s
            %s
            GROUP BY %s
        """, tierCol, totalCountExpr, wonCond, lostCond, voidCond, pendingCond, waitingCond, skippedCond, pnlExpr, turnoverExpr, oddExpr, fromClause, where, tierCol);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        AnalystStatsDto.TierStatsDto ml90 = buildTierStats("ML_90", rows);
        AnalystStatsDto.TierStatsDto ml80 = buildTierStats("ML_80", rows);
        AnalystStatsDto.TierStatsDto ml70 = buildTierStats("ML_70", rows);
        AnalystStatsDto.TierStatsDto ml60 = buildTierStats("ML_60", rows);
        AnalystStatsDto.TierStatsDto pass = buildTierStats("PASS", rows);
        AnalystStatsDto.TierStatsDto waitTier = buildTierStats("WAIT", rows);
        long total = ml90.getTotal() + ml80.getTotal() + ml70.getTotal() + ml60.getTotal() + pass.getTotal() + waitTier.getTotal();
        long won = ml90.getWon() + ml80.getWon() + ml70.getWon() + ml60.getWon();
        long lost = ml90.getLost() + ml80.getLost() + ml70.getLost() + ml60.getLost();
        long voidCount = ml90.getVoidCount() + ml80.getVoidCount() + ml70.getVoidCount() + ml60.getVoidCount();
        long pending = ml90.getPending() + ml80.getPending() + ml70.getPending() + ml60.getPending();
        long waiting = waitTier.getTotal();
        long skipped = pass.getSkipped();
        long settled = won + lost + voidCount;
        double winrate = (won + lost) > 0 ? (won * 100.0) / (won + lost) : 0.0;

        BigDecimal totalPnl = ml90.getTotalPnl()
                .add(ml80.getTotalPnl())
                .add(ml70.getTotalPnl())
                .add(ml60.getTotalPnl());

        double totalTurnover = 0.0;
        for (Map<String, Object> r : rows) {
            totalTurnover += ((Number) r.getOrDefault("turnover_rub", 0.0)).doubleValue();
        }
        if (totalTurnover <= 0.0) {
            totalTurnover = (ml90.getSettled() * 1000.0)
                    + (ml80.getSettled() * 500.0)
                    + (ml70.getSettled() * 250.0)
                    + (ml60.getSettled() * 100.0);
        }
        double roi = totalTurnover > 0 ? (totalPnl.doubleValue() / totalTurnover) * 100.0 : 0.0;

        // Facets breakdown query
        String facetSql = isStrategyMode ? String.format("""
            SELECT
                COUNT(*) FILTER (WHERE %s) as total_active,
                COUNT(*) FILTER (WHERE %s AND ((r.available_bookmakers IS NOT NULL AND r.available_bookmakers != '') OR (r.best_bookmaker IS NOT NULL AND r.best_bookmaker NOT IN ('NONE', 'Market Consensus', 'Market Consensus (Fair)', '')))) as active_with_bk,
                -- Stages
                COUNT(*) FILTER (WHERE %s AND (r.evaluation_stage = 'IN_PLAY' OR r.match_start_time <= NOW() AT TIME ZONE 'UTC')) as stage_in_play,
                COUNT(*) FILTER (WHERE %s AND r.evaluation_stage = 'WAIT_KICKOFF') as stage_wait_kickoff,
                COUNT(*) FILTER (WHERE %s AND r.evaluation_stage = 'KICKOFF_VERIFIED') as stage_kickoff_verified,
                COUNT(*) FILTER (WHERE %s AND r.evaluation_stage IN ('LINEUP_CONFIRMED', 'STAGE_2_PREVIEW')) as stage_lineup_confirmed,
                COUNT(*) FILTER (WHERE %s AND r.lineup_status = 'PROJECTED') as stage_projected,
                COUNT(*) FILTER (WHERE %s AND (r.evaluation_stage = 'PRE_LINEUP' OR (r.match_start_time > NOW() AT TIME ZONE 'UTC' AND (r.evaluation_stage IS NULL OR r.evaluation_stage NOT IN ('LINEUP_CONFIRMED', 'STAGE_2_PREVIEW', 'KICKOFF_VERIFIED', 'WAIT_KICKOFF'))))) as stage_pre_lineup,
                -- Time ranges
                COUNT(*) FILTER (WHERE %s AND r.match_start_time <= NOW() AT TIME ZONE 'UTC') as time_live,
                COUNT(*) FILTER (WHERE %s AND r.match_start_time > NOW() AT TIME ZONE 'UTC' AND r.match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '1 hour') as time_1h,
                COUNT(*) FILTER (WHERE %s AND r.match_start_time > NOW() AT TIME ZONE 'UTC' AND r.match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '4 hours') as time_4h,
                COUNT(*) FILTER (WHERE %s AND r.match_start_time > NOW() AT TIME ZONE 'UTC' AND r.match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '24 hours') as time_24h,
                COUNT(*) FILTER (WHERE %s AND r.match_start_time > (NOW() AT TIME ZONE 'UTC') + INTERVAL '24 hours') as time_more,
                -- Odds ranges
                COUNT(*) FILTER (WHERE %s AND v.recommended_odd >= 1.30 AND v.recommended_odd <= 1.80) as odd_13_18,
                COUNT(*) FILTER (WHERE %s AND v.recommended_odd > 1.80 AND v.recommended_odd <= 2.20) as odd_18_22,
                COUNT(*) FILTER (WHERE %s AND v.recommended_odd > 2.20 AND v.recommended_odd <= 3.00) as odd_22_30,
                COUNT(*) FILTER (WHERE %s AND v.recommended_odd > 3.00) as odd_30_plus,
                -- Market categories
                COUNT(*) FILTER (WHERE %s AND %s) as strat_draws,
                COUNT(*) FILTER (WHERE %s AND %s) as strat_handicap,
                COUNT(*) FILTER (WHERE %s AND %s) as strat_totals
            FROM ai_strategy_vote v
            JOIN match_ai_recommendation r ON r.id = v.recommendation_id
            %s
        """,
            pendingCond, pendingCond,
            pendingCond, pendingCond, pendingCond, pendingCond, pendingCond, pendingCond,
            pendingCond, pendingCond, pendingCond, pendingCond, pendingCond,
            pendingCond, pendingCond, pendingCond, pendingCond,
            pendingCond, MarketCategory.DRAWS_AND_OT.sqlCondition("v.market"),
            pendingCond, MarketCategory.ASIAN_HANDICAP.sqlCondition("v.market"),
            pendingCond, MarketCategory.TOTALS_CORRIDORS.sqlCondition("v.market"),
            where)
        : String.format("""
            SELECT
                COUNT(*) FILTER (WHERE %s) as total_active,
                COUNT(*) FILTER (WHERE %s AND ((available_bookmakers IS NOT NULL AND available_bookmakers != '') OR (best_bookmaker IS NOT NULL AND best_bookmaker NOT IN ('NONE', 'Market Consensus', 'Market Consensus (Fair)', '')))) as active_with_bk,
                -- Stages
                COUNT(*) FILTER (WHERE %s AND (evaluation_stage = 'IN_PLAY' OR match_start_time <= NOW() AT TIME ZONE 'UTC')) as stage_in_play,
                COUNT(*) FILTER (WHERE %s AND evaluation_stage = 'WAIT_KICKOFF') as stage_wait_kickoff,
                COUNT(*) FILTER (WHERE %s AND evaluation_stage = 'KICKOFF_VERIFIED') as stage_kickoff_verified,
                COUNT(*) FILTER (WHERE %s AND evaluation_stage IN ('LINEUP_CONFIRMED', 'STAGE_2_PREVIEW')) as stage_lineup_confirmed,
                COUNT(*) FILTER (WHERE %s AND lineup_status = 'PROJECTED') as stage_projected,
                COUNT(*) FILTER (WHERE %s AND (evaluation_stage = 'PRE_LINEUP' OR (match_start_time > NOW() AT TIME ZONE 'UTC' AND (evaluation_stage IS NULL OR evaluation_stage NOT IN ('LINEUP_CONFIRMED', 'STAGE_2_PREVIEW', 'KICKOFF_VERIFIED', 'WAIT_KICKOFF'))))) as stage_pre_lineup,
                -- Time ranges
                COUNT(*) FILTER (WHERE %s AND match_start_time <= NOW() AT TIME ZONE 'UTC') as time_live,
                COUNT(*) FILTER (WHERE %s AND match_start_time > NOW() AT TIME ZONE 'UTC' AND match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '1 hour') as time_1h,
                COUNT(*) FILTER (WHERE %s AND match_start_time > NOW() AT TIME ZONE 'UTC' AND match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '4 hours') as time_4h,
                COUNT(*) FILTER (WHERE %s AND match_start_time > NOW() AT TIME ZONE 'UTC' AND match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '24 hours') as time_24h,
                COUNT(*) FILTER (WHERE %s AND match_start_time > (NOW() AT TIME ZONE 'UTC') + INTERVAL '24 hours') as time_more,
                -- Odds ranges
                COUNT(*) FILTER (WHERE %s AND recommended_odd >= 1.30 AND recommended_odd <= 1.80) as odd_13_18,
                COUNT(*) FILTER (WHERE %s AND recommended_odd > 1.80 AND recommended_odd <= 2.20) as odd_18_22,
                COUNT(*) FILTER (WHERE %s AND recommended_odd > 2.20 AND recommended_odd <= 3.00) as odd_22_30,
                COUNT(*) FILTER (WHERE %s AND recommended_odd > 3.00) as odd_30_plus,
                -- Strategies
                COUNT(*) FILTER (WHERE %s AND %s) as strat_draws,
                COUNT(*) FILTER (WHERE %s AND %s) as strat_handicap,
                COUNT(*) FILTER (WHERE %s AND %s) as strat_totals
            FROM match_ai_recommendation
            %s
        """,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
            ACTIVE_OPPORTUNITY_CONDITION, MarketCategory.DRAWS_AND_OT.sqlCondition("recommended_market"),
            ACTIVE_OPPORTUNITY_CONDITION, MarketCategory.ASIAN_HANDICAP.sqlCondition("recommended_market"),
            ACTIVE_OPPORTUNITY_CONDITION, MarketCategory.TOTALS_CORRIDORS.sqlCondition("recommended_market"),
            where);
        List<Map<String, Object>> facetRows = jdbcTemplate.queryForList(facetSql, params.toArray());
        Map<String, Long> stageCounts = new LinkedHashMap<>();
        Map<String, Long> timeRangeCounts = new LinkedHashMap<>();
        Map<String, Long> oddRangeCounts = new LinkedHashMap<>();
        Map<String, Long> strategyCounts = new LinkedHashMap<>();
        Long activeWithBookmakerCount = 0L;

        if (!facetRows.isEmpty()) {
            Map<String, Object> fRow = facetRows.get(0);
            activeWithBookmakerCount = ((Number) fRow.getOrDefault("active_with_bk", 0)).longValue();
            stageCounts.put("IN_PLAY", ((Number) fRow.getOrDefault("stage_in_play", 0)).longValue());
            stageCounts.put("WAIT_KICKOFF", ((Number) fRow.getOrDefault("stage_wait_kickoff", 0)).longValue());
            stageCounts.put("KICKOFF_VERIFIED", ((Number) fRow.getOrDefault("stage_kickoff_verified", 0)).longValue());
            stageCounts.put("LINEUP_CONFIRMED", ((Number) fRow.getOrDefault("stage_lineup_confirmed", 0)).longValue());
            stageCounts.put("PROJECTED", ((Number) fRow.getOrDefault("stage_projected", 0)).longValue());
            stageCounts.put("PRE_LINEUP", ((Number) fRow.getOrDefault("stage_pre_lineup", 0)).longValue());
            timeRangeCounts.put("LIVE", ((Number) fRow.getOrDefault("time_live", 0)).longValue());
            timeRangeCounts.put("1H", ((Number) fRow.getOrDefault("time_1h", 0)).longValue());
            timeRangeCounts.put("4H", ((Number) fRow.getOrDefault("time_4h", 0)).longValue());
            timeRangeCounts.put("24H", ((Number) fRow.getOrDefault("time_24h", 0)).longValue());
            timeRangeCounts.put("MORE", ((Number) fRow.getOrDefault("time_more", 0)).longValue());
            oddRangeCounts.put("1.3-1.8", ((Number) fRow.getOrDefault("odd_13_18", 0)).longValue());
            oddRangeCounts.put("1.8-2.2", ((Number) fRow.getOrDefault("odd_18_22", 0)).longValue());
            oddRangeCounts.put("2.2-3.0", ((Number) fRow.getOrDefault("odd_22_30", 0)).longValue());
            oddRangeCounts.put("3.0+", ((Number) fRow.getOrDefault("odd_30_plus", 0)).longValue());
            strategyCounts.put("DRAWS_AND_OT", ((Number) fRow.getOrDefault("strat_draws", 0)).longValue());
            strategyCounts.put("ASIAN_HANDICAP", ((Number) fRow.getOrDefault("strat_handicap", 0)).longValue());
            strategyCounts.put("TOTALS_CORRIDORS", ((Number) fRow.getOrDefault("strat_totals", 0)).longValue());
        }

        // Sports breakdown query
        String sportCol = isStrategyMode ? "COALESCE(r.sport_name, 'OTHER')" : "COALESCE(sport_name, 'OTHER')";
        String sportOddExpr = isStrategyMode ? "AVG(v.recommended_odd) FILTER (WHERE v.vote_decision = 'PICK')" : "AVG(recommended_odd) FILTER (WHERE conviction_tier != 'PASS')";

        String sportSql = String.format("""
            SELECT
                %s as sport,
                %s as total_count,
                COUNT(*) FILTER (WHERE %s) as won_count,
                COUNT(*) FILTER (WHERE %s) as lost_count,
                COUNT(*) FILTER (WHERE %s) as void_count,
                COUNT(*) FILTER (WHERE %s) as pending_count,
                COUNT(*) FILTER (WHERE %s) as skipped_count,
                %s as total_pnl_rub,
                %s as turnover_rub,
                %s as avg_odd
            %s
            %s
            GROUP BY %s
            ORDER BY total_pnl_rub DESC
        """, sportCol, totalCountExpr, wonCond, lostCond, voidCond, pendingCond, skippedCond, pnlExpr, turnoverExpr, sportOddExpr, fromClause, where, sportCol);
        List<Map<String, Object>> sportRows = jdbcTemplate.queryForList(sportSql, params.toArray());
        Map<String, AnalystStatsDto.SportStatsDto> sportsMap = new java.util.LinkedHashMap<>();

        for (Map<String, Object> r : sportRows) {
            String sp = (String) r.get("sport");
            long spTotal = ((Number) r.getOrDefault("total_count", 0)).longValue();
            long spWon = ((Number) r.getOrDefault("won_count", 0)).longValue();
            long spLost = ((Number) r.getOrDefault("lost_count", 0)).longValue();
            long spVoid = ((Number) r.getOrDefault("void_count", 0)).longValue();
            long spPending = ((Number) r.getOrDefault("pending_count", 0)).longValue();
            long spSkipped = ((Number) r.getOrDefault("skipped_count", 0)).longValue();

            BigDecimal spPnl = BigDecimal.valueOf(((Number) r.getOrDefault("total_pnl_rub", 0.0)).doubleValue())
                    .setScale(2, RoundingMode.HALF_UP);
            double spAvgOdd = r.get("avg_odd") != null ? ((Number) r.get("avg_odd")).doubleValue() : 0.0;
            long spSettled = spWon + spLost + spVoid;
            double spWinrate = (spWon + spLost) > 0 ? (spWon * 100.0) / (spWon + spLost) : 0.0;
            double spTurnover = ((Number) r.getOrDefault("turnover_rub", 0.0)).doubleValue();
            if (spTurnover <= 0.0) {
                spTurnover = spSettled * 1000.0;
            }
            double spRoi = spTurnover > 0 ? (spPnl.doubleValue() / spTurnover) * 100.0 : 0.0;

            sportsMap.put(sp, AnalystStatsDto.SportStatsDto.builder()
                    .sport(sp)
                    .total(spTotal)
                    .settled(spSettled)
                    .won(spWon)
                    .lost(spLost)
                    .voidCount(spVoid)
                    .pending(spPending)
                    .skipped(spSkipped)
                    .winratePct(Math.round(spWinrate * 10.0) / 10.0)
                    .totalPnl(spPnl)
                    .roiPct(Math.round(spRoi * 10.0) / 10.0)
                    .avgOdds(Math.round(spAvgOdd * 100.0) / 100.0)
                    .build());
        }

        return AnalystStatsDto.builder()
                .totalEvaluations(total)
                .totalSettled(settled)
                .won(won)
                .lost(lost)
                .voidCount(voidCount)
                .pending(pending)
                .waiting(waiting)
                .skipped(skipped)
                .winratePct(Math.round(winrate * 10.0) / 10.0)
                .totalPnl(totalPnl)
                .roiPct(Math.round(roi * 10.0) / 10.0)
                .ml90(ml90)
                .ml80(ml80)
                .ml70(ml70)
                .ml60(ml60)
                .pass(pass)
                .waitTier(waitTier)
                .sports(sportsMap)
                .stageCounts(stageCounts)
                .timeRangeCounts(timeRangeCounts)
                .oddRangeCounts(oddRangeCounts)
                .strategyCounts(strategyCounts)
                .activeWithBookmakerCount(activeWithBookmakerCount)
                .build();
    }

    private AnalystStatsDto.TierStatsDto buildTierStats(String targetTier, List<Map<String, Object>> rows) {
        for (Map<String, Object> r : rows) {
            String tier = (String) r.get("tier");
            if (targetTier.equalsIgnoreCase(tier)) {
                long total = ((Number) r.getOrDefault("total_count", 0)).longValue();
                long won = ((Number) r.getOrDefault("won_count", 0)).longValue();
                long lost = ((Number) r.getOrDefault("lost_count", 0)).longValue();
                long voidCount = ((Number) r.getOrDefault("void_count", 0)).longValue();
                long pending = ((Number) r.getOrDefault("pending_count", 0)).longValue();
                long skipped = ((Number) r.getOrDefault("skipped_count", 0)).longValue();

                BigDecimal pnl = BigDecimal.valueOf(((Number) r.getOrDefault("total_pnl_rub", 0.0)).doubleValue())
                        .setScale(2, RoundingMode.HALF_UP);
                double avgOdd = r.get("avg_odd") != null ? ((Number) r.get("avg_odd")).doubleValue() : 0.0;
                long settled = won + lost + voidCount;
                double winrate = (won + lost) > 0 ? (won * 100.0) / (won + lost) : 0.0;
                double unitStake = 100.0;

                if ("ML_90".equalsIgnoreCase(targetTier)) {
                    unitStake = 1000.0;
                } else if ("ML_80".equalsIgnoreCase(targetTier)) {
                    unitStake = 500.0;
                } else if ("ML_70".equalsIgnoreCase(targetTier)) {
                    unitStake = 250.0;
                } else if ("ML_60".equalsIgnoreCase(targetTier)) {
                    unitStake = 100.0;
                }

                double turnover = ((Number) r.getOrDefault("turnover_rub", 0.0)).doubleValue();
                if (turnover <= 0.0) {
                    turnover = settled * unitStake;
                }
                double roi = (turnover > 0) ? (pnl.doubleValue() / turnover) * 100.0 : 0.0;

                return AnalystStatsDto.TierStatsDto.builder()
                        .tier(targetTier)
                        .total(total)
                        .settled(settled)
                        .won(won)
                        .lost(lost)
                        .voidCount(voidCount)
                        .pending(pending)
                        .skipped(skipped)
                        .winratePct(Math.round(winrate * 10.0) / 10.0)
                        .totalPnl(pnl)
                        .roiPct(Math.round(roi * 10.0) / 10.0)
                        .avgOdds(Math.round(avgOdd * 100.0) / 100.0)
                        .build();
            }
        }

        return AnalystStatsDto.TierStatsDto.builder()
                .tier(targetTier)
                .total(0).settled(0).won(0).lost(0).voidCount(0).pending(0).skipped(0)
                .winratePct(0.0).totalPnl(BigDecimal.ZERO).roiPct(0.0).avgOdds(0.0)
                .build();
    }

    /**
     * Aggregates bookmakers offering lines across current active matches with metadata.
     */
    public List<Map<String, Object>> getAvailableBookmakersStats() {
        List<MatchAiRecommendation> active = recommendationRepository.findByEvaluationStageNotIn(List.of("SETTLED", "CANCELLED", "SKIPPED"));
        Map<String, Integer> counts = new HashMap<>();

        for (MatchAiRecommendation rec : active) {
            Set<String> seenForMatch = new HashSet<>();

            if (rec.getAvailableBookmakers() != null && !rec.getAvailableBookmakers().isBlank()) {
                String[] parts = rec.getAvailableBookmakers().split(",");
                for (String p : parts) {
                    String clean = p.trim().toLowerCase(Locale.ROOT);
                    if (!clean.isEmpty()) {
                        seenForMatch.add(clean);
                    }
                }
            }

            if (rec.getBestBookmaker() != null && !rec.getBestBookmaker().isBlank()
                    && !"NONE".equalsIgnoreCase(rec.getBestBookmaker())
                    && !rec.getBestBookmaker().contains("Consensus")) {
                seenForMatch.add(rec.getBestBookmaker().trim().toLowerCase(Locale.ROOT));
            }

            for (String bk : seenForMatch) {
                counts.put(bk, counts.getOrDefault(bk, 0) + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            String key = e.getKey();
            int count = e.getValue();
            Optional<BookmakerCode> bCode = BookmakerCode.fromSlug(key);
            String displayName = bCode.map(BookmakerCode::getDisplayName)
                    .orElseGet(() -> Character.toUpperCase(key.charAt(0)) + key.substring(1));
            String jurisdiction = bCode.map(BookmakerCode::getJurisdiction).orElse("GLOBAL");
            String flag = bCode.map(BookmakerCode::getFlagEmoji).orElse("🌐");

            result.add(Map.of(
                    "id", key,
                    "displayName", displayName,
                    "jurisdiction", jurisdiction,
                    "flag", flag,
                    "activeCount", count
            ));
            processed.add(key);
            bCode.ifPresent(b -> processed.add(b.getSlug()));
        }

        for (BookmakerCode b : BookmakerCode.values()) {
            if (!processed.contains(b.getSlug())) {
                result.add(Map.of(
                        "id", b.getSlug(),
                        "displayName", b.getDisplayName(),
                        "jurisdiction", b.getJurisdiction(),
                        "flag", b.getFlagEmoji(),
                        "activeCount", 0
                ));
                processed.add(b.getSlug());
            }
        }

        result.sort((a, b) -> {
            int ca = (Integer) a.get("activeCount");
            int cb = (Integer) b.get("activeCount");
            if (cb != ca) return Integer.compare(cb, ca);
            return ((String) a.get("displayName")).compareToIgnoreCase((String) b.get("displayName"));
        });
        return result;
    }
}

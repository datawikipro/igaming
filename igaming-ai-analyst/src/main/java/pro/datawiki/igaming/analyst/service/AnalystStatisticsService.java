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

    private static final String ACTIVE_OPPORTUNITY_CONDITION = """
        (
            result_status = 'PENDING'
            AND conviction_tier != 'PASS'
            AND recommended_market != 'PASS'
            AND (evaluation_stage IS NULL OR evaluation_stage NOT IN (
                'SETTLED', 'AWAITING_SETTLEMENT', 'CANCELLED_SCHEDULE_CONFLICT',
                'CANCELLED_SYNTHETIC_COLLISION', 'SCHEDULE_CONFLICT_PENDING_RESOLUTION'
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
                        OR (UPPER(sport_name) = 'BASKETBALL' AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '130 minutes')
                        OR (UPPER(sport_name) = 'HOCKEY' AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '160 minutes')
                        OR (UPPER(sport_name) = 'TENNIS' AND match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '180 minutes')
                        OR (match_start_time >= (NOW() AT TIME ZONE 'UTC') - INTERVAL '120 minutes')
                    )
                )
            )
        )
    """;

    public List<ModelVersionDto> getModelVersions() {
        List<AiModelVersion> entities = aiModelVersionRepository.findAllByOrderByReleaseDateDesc();
        List<ModelVersionDto> dtos = new ArrayList<>();

        for (AiModelVersion v : entities) {
            AnalystStatsDto stats = getPerformanceStats(null, null, null, null, null, null, null, null, v.getVersionCode());
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
        return getPerformanceStats(null, null, null, null, null, null, null, null, null);
    }

    public AnalystStatsDto getPerformanceStats(
            String status, String sport, String stage, String timeRange,
            String oddRange, String bookmaker, String marketCategory, String query) {
        return getPerformanceStats(status, sport, stage, timeRange, oddRange, bookmaker, marketCategory, query, null);
    }

    public AnalystStatsDto getPerformanceStats(
            String status, String sport, String stage, String timeRange,
            String oddRange, String bookmaker, String marketCategory, String query, String version) {

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (version != null && !version.isBlank() && !"ALL".equalsIgnoreCase(version)) {
            where.append(" AND model_version = ? ");
            params.add(version.trim());
        }

        if (sport != null && !sport.isBlank() && !"ALL".equalsIgnoreCase(sport)) {
            where.append(" AND UPPER(sport_name) = UPPER(?) ");
            params.add(sport.trim());
        }

        if (bookmaker != null && !bookmaker.isBlank() && !"ALL".equalsIgnoreCase(bookmaker)) {
            String bm = bookmaker.trim();
            if ("HAS_BOOKMAKER".equalsIgnoreCase(bm) || "WITH_ODDS".equalsIgnoreCase(bm) || "BOOKMAKERS_ONLY".equalsIgnoreCase(bm)) {
                where.append(" AND ((available_bookmakers IS NOT NULL AND available_bookmakers != '') OR (best_bookmaker IS NOT NULL AND best_bookmaker NOT IN ('NONE', 'Market Consensus', 'Market Consensus (Fair)', ''))) ");
            } else {
                List<String> terms = BookmakerQueryFilter.resolveSearchTerms(bm);
                if (!terms.isEmpty()) {
                    where.append(" AND (");
                    for (int i = 0; i < terms.size(); i++) {
                        if (i > 0) where.append(" OR ");
                        where.append("LOWER(available_bookmakers) LIKE ? OR LOWER(best_bookmaker) LIKE ?");
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
            where.append(" AND (LOWER(team1_name) LIKE ? OR LOWER(team2_name) LIKE ? OR LOWER(league_name) LIKE ? OR LOWER(recommended_market) LIKE ? OR LOWER(core_hypothesis) LIKE ?) ");
            params.add(qPattern);
            params.add(qPattern);
            params.add(qPattern);
            params.add(qPattern);
            params.add(qPattern);
        }

        StringBuilder pendingCond = new StringBuilder(ACTIVE_OPPORTUNITY_CONDITION);

        if (stage != null && !stage.isBlank() && !"ALL".equalsIgnoreCase(stage)) {
            String st = stage.toUpperCase(Locale.ROOT).trim();
            if ("IN_PLAY".equals(st)) {
                pendingCond.append(" AND match_start_time <= NOW() AT TIME ZONE 'UTC' ");
            } else if ("WAIT_KICKOFF".equals(st)) {
                pendingCond.append(" AND evaluation_stage = 'WAIT_KICKOFF' ");
            } else if ("KICKOFF_VERIFIED".equals(st)) {
                pendingCond.append(" AND evaluation_stage = 'KICKOFF_VERIFIED' ");
            } else if ("LINEUP_CONFIRMED".equals(st)) {
                pendingCond.append(" AND evaluation_stage IN ('LINEUP_CONFIRMED', 'STAGE_2_PREVIEW') ");
            } else if ("PROJECTED".equals(st)) {
                pendingCond.append(" AND lineup_status = 'PROJECTED' ");
            } else if ("PRE_LINEUP".equals(st)) {
                pendingCond.append(" AND (evaluation_stage = 'PRE_LINEUP' OR (match_start_time > NOW() AT TIME ZONE 'UTC' AND (evaluation_stage IS NULL OR evaluation_stage NOT IN ('LINEUP_CONFIRMED', 'STAGE_2_PREVIEW', 'KICKOFF_VERIFIED', 'WAIT_KICKOFF')))) ");
            }
        }

        if (timeRange != null && !timeRange.isBlank() && !"ALL".equalsIgnoreCase(timeRange)) {
            String tr = timeRange.toUpperCase(Locale.ROOT).trim();
            if ("LIVE".equals(tr)) {
                pendingCond.append(" AND match_start_time <= NOW() AT TIME ZONE 'UTC' ");
            } else if ("1H".equals(tr) || "T_1H".equals(tr)) {
                pendingCond.append(" AND match_start_time > NOW() AT TIME ZONE 'UTC' AND match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '1 hour' ");
            } else if ("4H".equals(tr) || "T_4H".equals(tr)) {
                pendingCond.append(" AND match_start_time > NOW() AT TIME ZONE 'UTC' AND match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '4 hours' ");
            } else if ("24H".equals(tr) || "T_24H".equals(tr) || "TODAY".equals(tr)) {
                pendingCond.append(" AND match_start_time > NOW() AT TIME ZONE 'UTC' AND match_start_time <= (NOW() AT TIME ZONE 'UTC') + INTERVAL '24 hours' ");
            } else if ("MORE".equals(tr) || "T_MORE".equals(tr)) {
                pendingCond.append(" AND match_start_time > (NOW() AT TIME ZONE 'UTC') + INTERVAL '24 hours' ");
            }
        }

        if (oddRange != null && !oddRange.isBlank() && !"ALL".equalsIgnoreCase(oddRange)) {
            String or = oddRange.trim();
            if ("1.3-1.8".equals(or) || "ODD_1_3_1_8".equalsIgnoreCase(or)) {
                pendingCond.append(" AND recommended_odd >= 1.30 AND recommended_odd <= 1.80 ");
            } else if ("1.8-2.2".equals(or) || "ODD_1_8_2_2".equalsIgnoreCase(or)) {
                pendingCond.append(" AND recommended_odd > 1.80 AND recommended_odd <= 2.20 ");
            } else if ("2.2-3.0".equals(or) || "ODD_2_2_3_0".equalsIgnoreCase(or)) {
                pendingCond.append(" AND recommended_odd > 2.20 AND recommended_odd <= 3.00 ");
            } else if ("3.0+".equals(or) || "3.0_PLUS".equalsIgnoreCase(or) || "3+".equals(or)) {
                pendingCond.append(" AND recommended_odd > 3.00 ");
            }
        }

        if (marketCategory != null && !marketCategory.isBlank() && !"ALL".equalsIgnoreCase(marketCategory)) {
            String mc = marketCategory.trim();
            if ("DRAWS_AND_OT".equalsIgnoreCase(mc)) {
                pendingCond.append(" AND (recommended_market ILIKE '%DRAW%' OR recommended_market = 'PX' OR recommended_market ILIKE '%X%') ");
            } else if ("ASIAN_HANDICAP".equalsIgnoreCase(mc)) {
                pendingCond.append(" AND (recommended_market ILIKE '%AH_%' OR recommended_market ILIKE '%HANDICAP%') ");
            } else if ("TOTALS_CORRIDORS".equalsIgnoreCase(mc)) {
                pendingCond.append(" AND (recommended_market ILIKE '%TOTAL_%' OR recommended_market ILIKE '%OVER%' OR recommended_market ILIKE '%UNDER%') ");
            }
        }

        String sql = String.format("""
            SELECT
                COALESCE(conviction_tier, 'ML_60') as tier,
                COUNT(*) as total_count,
                COUNT(*) FILTER (WHERE result_status = 'WON') as won_count,
                COUNT(*) FILTER (WHERE result_status = 'LOST') as lost_count,
                COUNT(*) FILTER (WHERE result_status = 'VOID') as void_count,
                COUNT(*) FILTER (WHERE %s) as pending_count,
                COUNT(*) FILTER (WHERE result_status = 'WAITING') as waiting_count,
                COUNT(*) FILTER (WHERE result_status = 'SKIPPED') as skipped_count,
                COALESCE(SUM(actual_pnl), 0) as total_pnl_rub,
                AVG(recommended_odd) FILTER (WHERE conviction_tier != 'PASS' AND conviction_tier != 'WAIT') as avg_odd
            FROM match_ai_recommendation
            %s
            GROUP BY COALESCE(conviction_tier, 'ML_60')
        """, pendingCond, where);
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

        double totalTurnover = (ml90.getSettled() * 1000.0)
                + (ml80.getSettled() * 500.0)
                + (ml70.getSettled() * 250.0)
                + (ml60.getSettled() * 100.0);
        double roi = totalTurnover > 0 ? (totalPnl.doubleValue() / totalTurnover) * 100.0 : 0.0;

        // Facets breakdown query
        String facetSql = String.format("""
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
                COUNT(*) FILTER (WHERE %s AND (recommended_market ILIKE '%%DRAW%%' OR recommended_market = 'PX' OR recommended_market ILIKE '%%X%%')) as strat_draws,
                COUNT(*) FILTER (WHERE %s AND (recommended_market ILIKE '%%AH_%%' OR recommended_market ILIKE '%%HANDICAP%%')) as strat_handicap,
                COUNT(*) FILTER (WHERE %s AND (recommended_market ILIKE '%%TOTAL_%%' OR recommended_market ILIKE '%%OVER%%' OR recommended_market ILIKE '%%UNDER%%')) as strat_totals
            FROM match_ai_recommendation
            %s
        """,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
            ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION, ACTIVE_OPPORTUNITY_CONDITION,
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
        String sportSql = String.format("""
            SELECT
                COALESCE(sport_name, 'OTHER') as sport,
                COUNT(*) as total_count,
                COUNT(*) FILTER (WHERE result_status = 'WON') as won_count,
                COUNT(*) FILTER (WHERE result_status = 'LOST') as lost_count,
                COUNT(*) FILTER (WHERE result_status = 'VOID') as void_count,
                COUNT(*) FILTER (WHERE %s) as pending_count,
                COUNT(*) FILTER (WHERE result_status = 'SKIPPED') as skipped_count,
                COALESCE(SUM(actual_pnl), 0) as total_pnl_rub,
                AVG(recommended_odd) FILTER (WHERE conviction_tier != 'PASS') as avg_odd
            FROM match_ai_recommendation
            %s
            GROUP BY COALESCE(sport_name, 'OTHER')
            ORDER BY total_pnl_rub DESC
        """, pendingCond, where);
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
            double spRoi = spSettled > 0 ? (spPnl.doubleValue() / (spSettled * 1000.0)) * 100.0 : 0.0;

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

                double roi = (settled > 0 && unitStake > 0) ? (pnl.doubleValue() / (settled * unitStake)) * 100.0 : 0.0;

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
        List<MatchAiRecommendation> active = recommendationRepository.findByResultStatus("PENDING");
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

        Map<String, String[]> meta = Map.ofEntries(
                Map.entry("winline", new String[]{"Winline", "RU", "🇷🇺"}),
                Map.entry("fon-bet-ru", new String[]{"Фонбет (RU)", "RU", "🇷🇺"}),
                Map.entry("fon-bet-by", new String[]{"Фонбет (BY)", "BY", "🇧🇾"}),
                Map.entry("fon-bet-kz", new String[]{"Фонбет (KZ)", "KZ", "🇰🇿"}),
                Map.entry("fonbet", new String[]{"Фонбет", "RU", "🇷🇺"}),
                Map.entry("pari", new String[]{"Pari", "RU", "🇷🇺"}),
                Map.entry("leon", new String[]{"Leon", "RU", "🇷🇺"}),
                Map.entry("betboom", new String[]{"BetBoom", "RU", "🇷🇺"}),
                Map.entry("ligastavok", new String[]{"Лига Ставок", "RU", "🇷🇺"}),
                Map.entry("betcity", new String[]{"Бетсити (RU)", "RU", "🇷🇺"}),
                Map.entry("betcity-com", new String[]{"Betcity (COM)", "COM", "🌐"}),
                Map.entry("tennisi", new String[]{"Tennisi", "RU", "🇷🇺"}),
                Map.entry("olimpbet", new String[]{"Олимп (RU)", "RU", "🇷🇺"}),
                Map.entry("olimpbet-kz", new String[]{"Олимп (KZ)", "KZ", "🇰🇿"}),
                Map.entry("baltbet", new String[]{"Балтбет", "RU", "🇷🇺"}),
                Map.entry("zenit", new String[]{"Зенит", "RU", "🇷🇺"}),
                Map.entry("marathonbet", new String[]{"Марафон (RU)", "RU", "🇷🇺"}),
                Map.entry("marathonbet-by", new String[]{"Марафон (BY)", "BY", "🇧🇾"}),
                Map.entry("marathonbet-com", new String[]{"Марафон (COM)", "COM", "🌐"}),
                Map.entry("pinnacle", new String[]{"Pinnacle", "COM", "🌐"}),
                Map.entry("1xbet", new String[]{"1xBet", "COM", "🌐"}),
                Map.entry("1xbit", new String[]{"1xBit", "INT", "🪙"}),
                Map.entry("betano", new String[]{"Betano", "GLOBAL", "🌐"}),
                Map.entry("bet365", new String[]{"Bet365", "GLOBAL", "🌐"}),
                Map.entry("leovegas", new String[]{"LeoVegas", "GLOBAL", "🌐"}),
                Map.entry("unibet", new String[]{"Unibet", "GLOBAL", "🌐"}),
                Map.entry("mrgreen", new String[]{"Mr Green", "GLOBAL", "🌐"}),
                Map.entry("casumo", new String[]{"Casumo", "GLOBAL", "🌐"}),
                Map.entry("paf", new String[]{"Paf", "GLOBAL", "🌐"}),
                Map.entry("888sport", new String[]{"888sport", "GLOBAL", "🌐"}),
                Map.entry("betsson", new String[]{"Betsson", "GLOBAL", "🌐"}),
                Map.entry("betsafe", new String[]{"Betsafe", "GLOBAL", "🌐"}),
                Map.entry("nordicbet", new String[]{"NordicBet", "GLOBAL", "🌐"}),
                Map.entry("expekt", new String[]{"Expekt", "GLOBAL", "🌐"}),
                Map.entry("stoiximan", new String[]{"Stoiximan", "GLOBAL", "🌐"}),
                Map.entry("sunbet", new String[]{"Sunbet", "ZA", "🇿🇦"}),
                Map.entry("superbet", new String[]{"Superbet", "RO", "🇷🇴"}),
                Map.entry("svenskaspel", new String[]{"Svenska Spel", "SE", "🇸🇪"}),
                Map.entry("melbet", new String[]{"Melbet", "RU", "🇷🇺"}),
                Map.entry("mostbet", new String[]{"Mostbet", "COM", "🌐"}),
                Map.entry("22bet", new String[]{"22Bet", "COM", "🌐"}),
                Map.entry("bcgame", new String[]{"BC.Game", "CRYPTO", "🪙"}),
                Map.entry("stake", new String[]{"Stake", "CRYPTO", "🪙"})
        );
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            String key = e.getKey();
            int count = e.getValue();
            String[] m = meta.get(key);
            String displayName = m != null ? m[0] : (Character.toUpperCase(key.charAt(0)) + key.substring(1));
            String jurisdiction = m != null ? m[1] : "GLOBAL";
            String flag = m != null ? m[2] : "🌐";

            result.add(Map.of(
                    "id", key,
                    "displayName", displayName,
                    "jurisdiction", jurisdiction,
                    "flag", flag,
                    "activeCount", count
            ));
            processed.add(key);
        }

        for (Map.Entry<String, String[]> e : meta.entrySet()) {
            if (!processed.contains(e.getKey())) {
                result.add(Map.of(
                        "id", e.getKey(),
                        "displayName", e.getValue()[0],
                        "jurisdiction", e.getValue()[1],
                        "flag", e.getValue()[2],
                        "activeCount", 0
                ));
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

package pro.datawiki.igaming.analyst.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSettlementService {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final BigDecimal DEFAULT_UNIT_STAKE = BigDecimal.valueOf(1000.0);

    /**
     * Periodically settles finished matches every 5 minutes.
     */
    @Scheduled(cron = "${analyst.scheduling.settle-cron:0 */5 * * * *}")
    @Transactional
    public void settleFinishedMatches() {
        // Look back for matches started at least 105 minutes ago
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(105);
        List<MatchAiRecommendation> pending = recommendationRepository.findByResultStatusAndMatchStartTimeBefore("PENDING", cutoff);

        if (pending.isEmpty()) {
            return;
        }

        log.info("Checking settlement for {} pending AI recommendations...", pending.size());
        for (MatchAiRecommendation rec : pending) {
            settleSingleRecommendation(rec);
        }
    }

    @Transactional
    public boolean settleSingleRecommendation(MatchAiRecommendation rec) {
        if (rec.getMatchId() == null) {
            return false;
        }

        String sql = "SELECT final_score1, final_score2, match_result, is_live FROM match_record WHERE id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, rec.getMatchId());
        if (rows.isEmpty()) {
            return false;
        }

        Map<String, Object> matchData = rows.get(0);
        String s1 = (String) matchData.get("final_score1");
        String s2 = (String) matchData.get("final_score2");
        Boolean isLive = (Boolean) matchData.get("is_live");

        if (s1 == null || s2 == null || Boolean.TRUE.equals(isLive)) {
            return false; // Match not yet finished or scores not recorded
        }

        try {
            int score1 = Integer.parseInt(s1.trim());
            int score2 = Integer.parseInt(s2.trim());
            String finalScore = score1 + ":" + score2;
            rec.setFinalScore(finalScore);

            SettlementOutcome outcome = evaluateBetOutcome(rec.getRecommendedMarket(), score1, score2);

            BigDecimal odd = rec.getRecommendedOdd() != null ? rec.getRecommendedOdd() : BigDecimal.valueOf(1.90);

            if (outcome == SettlementOutcome.WON) {
                rec.setResultStatus("WON");
                BigDecimal pnl = odd.subtract(BigDecimal.ONE).multiply(DEFAULT_UNIT_STAKE).setScale(2, RoundingMode.HALF_UP);
                BigDecimal roi = odd.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100.0)).setScale(2, RoundingMode.HALF_UP);
                rec.setActualPnl(pnl);
                rec.setRoiPct(roi);
            } else if (outcome == SettlementOutcome.LOST) {
                rec.setResultStatus("LOST");
                rec.setActualPnl(DEFAULT_UNIT_STAKE.negate());
                rec.setRoiPct(BigDecimal.valueOf(-100.0));
            } else if (outcome == SettlementOutcome.VOID) {
                rec.setResultStatus("VOID");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
            }

            recommendationRepository.save(rec);
            log.info("Settled match #{} ({}-{}): Bet='{}', Score='{}', Result={}, PnL={}",
                    rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(),
                    rec.getRecommendedMarket(), finalScore, rec.getResultStatus(), rec.getActualPnl());
            return true;
        } catch (NumberFormatException e) {
            log.warn("Cannot parse scores for match #{}: {} - {}", rec.getMatchId(), s1, s2);
            return false;
        }
    }

    public enum SettlementOutcome {
        WON, LOST, VOID, UNRESOLVED
    }

    public SettlementOutcome evaluateBetOutcome(String market, int s1, int s2) {
        if (market == null) return SettlementOutcome.UNRESOLVED;
        String m = market.toLowerCase().trim();

        int diff = s1 - s2; // positive = team1 won
        int totalGoals = s1 + s2;

        // Totals
        if (m.contains("over 2.5") || m.contains("тб 2.5") || m.contains("тотал больше 2.5")) {
            return totalGoals >= 3 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.contains("under 2.5") || m.contains("тм 2.5") || m.contains("тотал меньше 2.5")) {
            return totalGoals <= 2 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.contains("over 1.5") || m.contains("тб 1.5")) {
            return totalGoals >= 2 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.contains("under 3.5") || m.contains("тм 3.5")) {
            return totalGoals <= 3 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // BTTS
        if (m.contains("btts yes") || m.contains("обе забьют: да") || m.contains("оз да")) {
            return (s1 > 0 && s2 > 0) ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.contains("btts no") || m.contains("обе забьют: нет") || m.contains("оз нет")) {
            return (s1 == 0 || s2 == 0) ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // Asian Handicap 0 / DNB
        if (m.contains("(0)") || m.contains("dnb 1") || m.contains("фора 1 (0)") || m.contains("ah 1 (0)")) {
            if (diff > 0) return SettlementOutcome.WON;
            if (diff == 0) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.contains("dnb 2") || m.contains("фора 2 (0)") || m.contains("ah 2 (0)")) {
            if (diff < 0) return SettlementOutcome.WON;
            if (diff == 0) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }

        // Double Chance / AH +0.5
        if (m.contains("1x") || m.contains("+0.5") && (m.contains("1") || m.contains("хозяева"))) {
            return diff >= 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.contains("x2") || m.contains("+0.5") && (m.contains("2") || m.contains("гости"))) {
            return diff <= 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // Handicap +1.0
        if (m.contains("+1.0") && (m.contains("1") || m.contains("хозяева"))) {
            if (diff >= 0) return SettlementOutcome.WON;
            if (diff == -1) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.contains("+1.0") && (m.contains("2") || m.contains("гости") || m.contains("brest") || m.contains("genoa"))) {
            if (diff <= 0) return SettlementOutcome.WON;
            if (diff == 1) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }

        // Handicap +2.0
        if (m.contains("+2.0") && (m.contains("1") || m.contains("хозяева") || m.contains("telstar"))) {
            if (diff >= -1) return SettlementOutcome.WON;
            if (diff == -2) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }

        // Handicap +2.5
        if (m.contains("+2.5") && (m.contains("2") || m.contains("lokeren"))) {
            return (s2 + 2.5 > s1) ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // Handicap -1.0
        if (m.contains("-1.0") && (m.contains("1") || m.contains("philadelphia"))) {
            if (diff > 1) return SettlementOutcome.WON;
            if (diff == 1) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }

        // 1X2 Outrights
        if (m.equals("p1") || m.equals("п1") || m.contains("победа 1")) {
            return diff > 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("p2") || m.equals("п2") || m.contains("победа 2")) {
            return diff < 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("x") || m.equals("х") || m.contains("ничья")) {
            return diff == 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        return SettlementOutcome.UNRESOLVED;
    }
}

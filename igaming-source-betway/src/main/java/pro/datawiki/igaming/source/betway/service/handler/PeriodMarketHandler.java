package pro.datawiki.igaming.source.betway.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Halves and Periods (1st Half, 2nd Half, 1st Period, etc.)
 * when scope is not FULL_MATCH.
 */
@Component
public class PeriodMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        if (mName.contains("BOTH TEAMS TO SCORE") || mName.contains("BTTS") || mName.contains("BOTH TEAMS SCORE")) return false;
        if (mName.contains("CORRECT SCORE") || mName.contains("EXACT SCORE")) return false;
        if (mName.contains("HALF TIME / FULL TIME") || mName.contains("HT/FT") || mName.contains("HT / FT") || mName.contains("DOUBLE RESULT")) return false;
        if (mName.contains("DOUBLE CHANCE")) return false;
        if (mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("TIE NO BET")) return false;

        BetScope scope = resolveScope(mName);
        return scope != BetScope.FULL_MATCH;
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String scopeSuffix = "_" + scope.name().toLowerCase();

        if (mName.contains("HANDICAP") || mName.contains("SPREAD")) {
            handlePeriodHandicap(market, event, scope, scopeSuffix, items);
        } else if (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER")) {
            handlePeriodTotal(market, event, scope, scopeSuffix, items);
        } else if (mName.contains("RESULT") || mName.contains("WINNER") || mName.contains("1X2") || mName.contains("MONEYLINE")) {
            handlePeriodResult(market, event, scope, scopeSuffix, items);
        }
    }

    private void handlePeriodResult(BetwayMarketDto market, BetwayEventDto event, BetScope scope, String scopeSuffix, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY")) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "period_result" + scopeSuffix, oName, odds, betType);
            }
        }
    }

    private void handlePeriodTotal(BetwayMarketDto market, BetwayEventDto event, BetScope scope, String scopeSuffix, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ")) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.MATCH, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ")) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.MATCH, false, points);
            }

            if (betType != null) {
                addOddItem(items, "period_total" + scopeSuffix, oName, odds, betType);
            }
        }
    }

    private void handlePeriodHandicap(BetwayMarketDto market, BetwayEventDto event, BetScope scope, String scopeSuffix, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, false, hdp);
            } else if (isTeam2(upper, event)) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, "period_handicap" + scopeSuffix, oName, odds, betType);
            }
        }
    }

    private boolean isTeam1(String outcomeName, BetwayEventDto event) {
        if (event.getHomeTeam() != null && outcomeName.contains(event.getHomeTeam().toUpperCase())) return true;
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME");
    }

    private boolean isTeam2(String outcomeName, BetwayEventDto event) {
        if (event.getAwayTeam() != null && outcomeName.contains(event.getAwayTeam().toUpperCase())) return true;
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY");
    }
}

package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Handler for Period/Half/Quarter specific markets (1st Half 1X2, 2nd Half 1X2, Period 1..3 Winner) for Betano.
 */
@Component
public class BetanoPeriodMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        if (mName.contains("DOUBLE CHANCE") || mName.contains("DRAW NO BET") || mName.contains("DNB")) return false;
        if (mName.contains("TOTAL") || mName.contains("HANDICAP")) return false;

        BetScope scope = resolveScope(mName);
        if (scope == BetScope.FULL_MATCH) return false;

        return mName.contains("WINNER") ||
               mName.contains("1X2") ||
               mName.contains("RESULT") ||
               mName.contains("VENCEDOR") ||
               mName.contains("MONEYLINE");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("period_result", scope);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") ||
                       upper.contains("EMPATE") || "OT_DRAW".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isTeam1(String outcomeName, BetanoEventDto event) {
        if (event != null && event.getHomeTeam() != null) {
            String home = event.getHomeTeam().toUpperCase();
            if (outcomeName.contains(home) || (home.length() >= 3 && home.contains(outcomeName))) return true;
        }
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME") || outcomeName.startsWith("TEAM 1") || outcomeName.startsWith("TEAM1") || outcomeName.startsWith("CASA");
    }

    private boolean isTeam2(String outcomeName, BetanoEventDto event) {
        if (event != null && event.getAwayTeam() != null) {
            String away = event.getAwayTeam().toUpperCase();
            if (outcomeName.contains(away) || (away.length() >= 3 && away.contains(outcomeName))) return true;
        }
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY") || outcomeName.startsWith("TEAM 2") || outcomeName.startsWith("TEAM2") || outcomeName.startsWith("FORA");
    }
}

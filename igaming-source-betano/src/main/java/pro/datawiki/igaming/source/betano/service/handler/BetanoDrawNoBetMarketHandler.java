package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Handler for Draw No Bet (DNB / Tie No Bet / Empate Anula) markets for Betano.
 * Draw No Bet maps to Handicap 0.0 for TEAM1 or TEAM2.
 */
@Component
public class BetanoDrawNoBetMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        return mName.contains("DRAW NO BET") ||
               mName.contains("DNB") ||
               mName.contains("TIE NO BET") ||
               mName.contains("EMPATE ANULA") ||
               mName.contains("EMPATE NO VALIDO");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("draw_no_bet", scope);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.MATCH);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isTeam1(String outcomeName, BetanoEventDto event) {
        if (event.getHomeTeam() != null) {
            String home = event.getHomeTeam().toUpperCase();
            if (outcomeName.contains(home) || (home.length() >= 3 && home.contains(outcomeName))) return true;
        }
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME") || outcomeName.startsWith("TEAM 1") || outcomeName.startsWith("TEAM1") || outcomeName.startsWith("CASA");
    }

    private boolean isTeam2(String outcomeName, BetanoEventDto event) {
        if (event.getAwayTeam() != null) {
            String away = event.getAwayTeam().toUpperCase();
            if (outcomeName.contains(away) || (away.length() >= 3 && away.contains(outcomeName))) return true;
        }
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY") || outcomeName.startsWith("TEAM 2") || outcomeName.startsWith("TEAM2") || outcomeName.startsWith("FORA");
    }
}

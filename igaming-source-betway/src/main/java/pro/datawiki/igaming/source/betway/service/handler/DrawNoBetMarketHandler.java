package pro.datawiki.igaming.source.betway.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Draw No Bet (DNB) markets.
 * Draw No Bet maps to Handicap 0.0 for TEAM1 or TEAM2.
 */
@Component
public class DrawNoBetMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        return mName.contains("DRAW NO BET") || mName.contains("DNB");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = "draw_no_bet" + (scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase()));

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.MATCH);
            } else if (isTeam2(upper, event)) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
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

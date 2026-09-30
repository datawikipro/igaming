package pro.datawiki.igaming.source.betway.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;

/**
 * Handler for 1X2, Match Winner, Moneyline, and Draw No Bet markets.
 */
@Component
public class MatchResultMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false; // Handled by EsportsMarketHandler
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        if (mName.contains("DOUBLE CHANCE")) return false;
        if (mName.contains("DRAW NO BET") || mName.contains("DNB")) return false;

        return mName.contains("MATCH WINNER") ||
               mName.contains("WIN / DRAW / WIN") ||
               mName.contains("WIN-DRAW-WIN") ||
               mName.contains("1X2") ||
               mName.contains("MONEYLINE") ||
               mName.contains("MATCH RESULT");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        boolean isDnb = mName.contains("DRAW NO BET") || mName.contains("DNB");

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upperOutcome = oName.toUpperCase();

            BetType betType = null;
            if (isDnb) {
                if (isTeam1(upperOutcome, event)) {
                    betType = map1X2Record("1", scope, StatType.MATCH);
                } else if (isTeam2(upperOutcome, event)) {
                    betType = map1X2Record("2", scope, StatType.MATCH);
                }
            } else {
                if (isTeam1(upperOutcome, event) || "1".equals(upperOutcome) || upperOutcome.startsWith("HOME")) {
                    betType = map1X2Record("1", scope, StatType.MATCH);
                } else if (isTeam2(upperOutcome, event) || "2".equals(upperOutcome) || upperOutcome.startsWith("AWAY")) {
                    betType = map1X2Record("2", scope, StatType.MATCH);
                } else if ("X".equals(upperOutcome) || upperOutcome.contains("DRAW") || upperOutcome.contains("TIE")) {
                    betType = map1X2Record("X", scope, StatType.MATCH);
                }
            }

            if (betType != null) {
                addOddItem(items, isDnb ? "draw_no_bet" : "1x2", oName, odds, betType);
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

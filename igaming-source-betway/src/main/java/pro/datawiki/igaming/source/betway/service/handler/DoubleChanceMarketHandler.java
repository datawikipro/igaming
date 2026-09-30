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
 * Handler for Double Chance markets (1X, 12, X2).
 */
@Component
public class DoubleChanceMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        return mName.contains("DOUBLE CHANCE");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            boolean isHome = (event.getHomeTeam() != null && upper.contains(event.getHomeTeam().toUpperCase())) || upper.contains("HOME") || upper.startsWith("1 ") || upper.endsWith(" 1");
            boolean isAway = (event.getAwayTeam() != null && upper.contains(event.getAwayTeam().toUpperCase())) || upper.contains("AWAY") || upper.startsWith("2 ") || upper.endsWith(" 2");
            boolean isDraw = upper.contains("DRAW") || upper.contains("TIE") || upper.contains(" X") || upper.startsWith("X ") || upper.equals("X");

            BetType betType = null;
            if (upper.contains("1X") || (isHome && isDraw && !isAway)) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (upper.contains("12") || (isHome && isAway && !isDraw)) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (upper.contains("X2") || upper.contains("2X") || (isAway && isDraw && !isHome)) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "double_chance", oName, odds, betType);
            }
        }
    }
}

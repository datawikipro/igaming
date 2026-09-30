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
        String group = formatGroupName("double_chance", scope);

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            boolean isHome = isHomeComponent(upper, event);
            boolean isAway = isAwayComponent(upper, event);
            boolean isDraw = isDrawComponent(upper);

            BetType betType = null;
            if (upper.contains("1X") || upper.equals("1/X") || upper.equals("1-X") || (isHome && isDraw && !isAway)) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (upper.contains("12") || upper.equals("1/2") || upper.equals("1-2") || (isHome && isAway && !isDraw)) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (upper.contains("X2") || upper.contains("2X") || upper.equals("X/2") || upper.equals("X-2") || (isAway && isDraw && !isHome)) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isHomeComponent(String upper, BetwayEventDto event) {
        if (event.getHomeTeam() != null) {
            String home = event.getHomeTeam().toUpperCase();
            if (upper.contains(home) || (home.length() >= 3 && home.contains(upper.replace("OR DRAW", "").replace("OR AWAY", "").trim()))) {
                return true;
            }
        }
        return upper.contains("HOME") || upper.startsWith("1 ") || upper.endsWith(" 1") || upper.contains(" 1 ") || upper.startsWith("1/");
    }

    private boolean isAwayComponent(String upper, BetwayEventDto event) {
        if (event.getAwayTeam() != null) {
            String away = event.getAwayTeam().toUpperCase();
            if (upper.contains(away) || (away.length() >= 3 && away.contains(upper.replace("OR DRAW", "").replace("OR HOME", "").trim()))) {
                return true;
            }
        }
        return upper.contains("AWAY") || upper.startsWith("2 ") || upper.endsWith(" 2") || upper.contains(" 2 ") || upper.endsWith("/2");
    }

    private boolean isDrawComponent(String upper) {
        return upper.contains("DRAW") || upper.contains("TIE") || upper.contains(" X") || upper.startsWith("X ") || upper.equals("X") || upper.contains("/X");
    }
}

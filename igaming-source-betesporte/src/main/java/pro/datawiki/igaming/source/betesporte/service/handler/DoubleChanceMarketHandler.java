package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

/**
 * Handler for Double Chance markets (1X, 12, X2).
 */
@Component
@Order(90)
public class DoubleChanceMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO") || mName.contains("CARD") || mName.contains("CART")) return false;

        return mName.contains("DOUBLE CHANCE") ||
               mName.contains("DUPLA CHANCE") ||
               mName.contains("CHANCE DUPLA") ||
               mName.contains("DUPLA HIPOTESE") ||
               mName.contains("DUPLA HIPÓTESE");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.equals("1X") || upper.contains("1 OR X") || upper.contains("1 OU X") ||
                (isHome(upper, event) && isDraw(upper))) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (upper.equals("12") || upper.contains("1 OR 2") || upper.contains("1 OU 2") ||
                       (isHome(upper, event) && isAway(upper, event))) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (upper.equals("X2") || upper.equals("2X") || upper.contains("X OR 2") || upper.contains("X OU 2") ||
                       (isDraw(upper) && isAway(upper, event))) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                String group = formatGroupName("double_chance", scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isHome(String name, BetesporteEventDto event) {
        if (event.getHomeTeam() != null && name.contains(event.getHomeTeam().toUpperCase())) return true;
        return name.startsWith("HOME") || name.startsWith("CASA") || name.contains("1");
    }

    private boolean isAway(String name, BetesporteEventDto event) {
        if (event.getAwayTeam() != null && name.contains(event.getAwayTeam().toUpperCase())) return true;
        return name.startsWith("AWAY") || name.startsWith("FORA") || name.contains("2");
    }

    private boolean isDraw(String name) {
        return name.contains("DRAW") || name.contains("EMPATE") || name.contains(" TIE") || name.contains("X");
    }
}

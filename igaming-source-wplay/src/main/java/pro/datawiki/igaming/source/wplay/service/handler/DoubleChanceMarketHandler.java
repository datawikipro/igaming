package pro.datawiki.igaming.source.wplay.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;

/**
 * Handler for Double Chance markets:
 * - Doble Oportunidad / Doble Chance (Spanish)
 * - Double Chance (English)
 * Maps to 1X, 12, X2.
 */
@Component
@Order(90)
public class DoubleChanceMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CÓRNER") ||
            mName.contains("CARD") || mName.contains("TARJETA") || mName.contains("AMARILLA")) {
            return false;
        }

        return mName.contains("DOUBLE CHANCE") ||
               mName.contains("DOBLE OPORTUNIDAD") ||
               mName.contains("DOBLE CHANCE") ||
               mName.contains("DOBLE POSIBILIDAD") ||
               mName.contains("OPORTUNIDAD DOBLE");
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("double_chance", scope);

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.equals("1X") || upper.contains("1 OR X") || upper.contains("1 O X") ||
                upper.contains("1 Ó X") || upper.contains("1/X") ||
                (isTeam1(upper, event) && isDraw(upper))) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (upper.equals("12") || upper.contains("1 OR 2") || upper.contains("1 O 2") ||
                       upper.contains("1 Ó 2") || upper.contains("1/2") ||
                       (isTeam1(upper, event) && isTeam2(upper, event))) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (upper.equals("X2") || upper.equals("2X") || upper.contains("X OR 2") ||
                       upper.contains("X O 2") || upper.contains("X Ó 2") || upper.contains("2 O X") ||
                       upper.contains("X/2") || upper.contains("2/X") ||
                       (isDraw(upper) && isTeam2(upper, event))) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

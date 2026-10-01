package pro.datawiki.igaming.source.caliente.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;
import pro.datawiki.igaming.source.caliente.dto.CalienteMarketDto;
import pro.datawiki.igaming.source.caliente.dto.CalienteOutcomeDto;

import java.util.List;

/**
 * Handler for Double Chance markets (1X, 12, X2) in Caliente.
 */
@Component
public class DoubleChanceMarketHandler extends AbstractCalienteMarketHandler {

    @Override
    public boolean supports(CalienteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CARD") || mName.contains("TARJETA")) return false;

        return mName.contains("DOUBLE CHANCE") ||
               mName.contains("DOBLE OPORTUNIDAD") ||
               mName.contains("CHANCE DOBLE");
    }

    @Override
    public void handle(CalienteMarketDto market, CalienteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        for (CalienteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.equals("1X") || upper.contains("1 OR X") || upper.contains("1 O X") || upper.contains("1 U X") ||
                (isHome(upper, event) && isDraw(upper))) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (upper.equals("12") || upper.contains("1 OR 2") || upper.contains("1 O 2") || upper.contains("1 U 2") ||
                       (isHome(upper, event) && isAway(upper, event))) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (upper.equals("X2") || upper.equals("2X") || upper.contains("X OR 2") || upper.contains("X O 2") || upper.contains("X U 2") ||
                       (isDraw(upper) && isAway(upper, event))) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "double_chance", oName, odds, betType);
            }
        }
    }

    private boolean isHome(String name, CalienteEventDto event) {
        if (event.getHomeTeam() != null && name.contains(event.getHomeTeam().toUpperCase())) return true;
        return name.startsWith("LOCAL") || name.startsWith("HOME") || name.contains("1");
    }

    private boolean isAway(String name, CalienteEventDto event) {
        if (event.getAwayTeam() != null && name.contains(event.getAwayTeam().toUpperCase())) return true;
        return name.startsWith("VISITANTE") || name.startsWith("AWAY") || name.contains("2");
    }

    private boolean isDraw(String name) {
        return name.contains("DRAW") || name.contains("EMPATE") || name.contains(" TIE") || name.contains("X");
    }
}

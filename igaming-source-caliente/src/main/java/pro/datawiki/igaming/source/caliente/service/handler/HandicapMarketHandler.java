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
 * Handler for Asian and European Handicap / Spread markets in Caliente.
 */
@Component
public class HandicapMarketHandler extends AbstractCalienteMarketHandler {

    @Override
    public boolean supports(CalienteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CARD") || mName.contains("TARJETA")) return false;

        return mName.contains("HANDICAP") ||
               mName.contains("HÁNDICAP") ||
               mName.contains("SPREAD") ||
               mName.contains("LÍNEA DE PUNTOS") ||
               mName.contains("LINEA DE PUNTOS") ||
               mName.contains("HÁNDICAP ASIÁTICO") ||
               mName.contains("HANDICAP ASIATICO");
    }

    @Override
    public void handle(CalienteMarketDto market, CalienteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (CalienteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
            if (hcp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("LOCAL") || upper.startsWith("HOME")) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, isAsian, hcp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("VISITANTE") || upper.startsWith("AWAY")) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, isAsian, hcp);
            }

            if (betType != null) {
                String group = formatGroupName(isAsian ? "asian_handicap" : "handicap", scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

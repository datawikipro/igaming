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
 * Handler for Asian and European Handicap / Spread markets:
 * - Hándicap / Hándicap Asiático (Spanish)
 * - Handicap / Asian Handicap / Point Spread (English)
 */
@Component
@Order(110)
public class HandicapMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CÓRNER") ||
            mName.contains("CARD") || mName.contains("TARJETA") || mName.contains("AMARILLA")) {
            return false;
        }
        if (mName.contains("DRAW NO BET") || mName.contains("APUESTA SIN EMPATE") ||
            mName.contains("EMPATE NO ACCIÓN") || mName.contains("EMPATE NO ACCION") ||
            mName.contains("EMPATE ANULA") || mName.contains("DNB")) {
            return false;
        }

        return mName.contains("HANDICAP") ||
               mName.contains("HÁNDICAP") ||
               mName.contains("SPREAD") ||
               mName.contains("POINT SPREAD") ||
               mName.contains("LÍNEA DE DINERO CON HÁNDICAP") ||
               mName.contains("VENTAJA DE GOLES");
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
            if (hcp == null) continue;

            String upper = oName.toUpperCase();
            boolean outcomeAsian = isAsian || isQuarterAsian(hcp);

            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") ||
                upper.startsWith("LOCAL") || upper.startsWith("CASA")) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, outcomeAsian, hcp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") ||
                       upper.startsWith("VISITANTE")) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, outcomeAsian, hcp);
            }

            if (betType != null) {
                String group = formatGroupName(outcomeAsian ? "asian_handicap" : "handicap", scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

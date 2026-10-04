package pro.datawiki.igaming.source.wplay.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;

/**
 * Handler for Corners markets (StatType.CORNERS):
 * - Corners / Tiros de esquina / Córners / Esquinas (Spanish/English)
 * Supports: 1X2, Totals (Over/Under), Handicap, First/Last corner, Double Chance, Draw No Bet.
 */
@Component
@Order(30)
public class CornersMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("CORNER") ||
               mName.contains("ESQUINA") ||
               mName.contains("CÓRNER") ||
               mName.contains("CORNERS") ||
               mName.contains("TIROS DE ESQUINA") ||
               mName.contains("TIRO DE ESQUINA");
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        boolean isTotal = mName.contains("TOTAL") || mName.contains("OVER") || mName.contains("UNDER") ||
                          mName.contains("MÁS") || mName.contains("MAS") || mName.contains("MENOS") ||
                          mName.contains("O/U") || mName.contains("OVER/UNDER");
        boolean isHandicap = mName.contains("HANDICAP") || mName.contains("HÁNDICAP") || mName.contains("SPREAD");
        boolean is1x2 = mName.contains("1X2") || mName.contains("RESULTADO") || mName.contains("GANADOR");
        boolean isDoubleChance = mName.contains("DOUBLE CHANCE") || mName.contains("DOBLE OPORTUNIDAD") || mName.contains("DOBLE CHANCE");
        boolean isDnb = mName.contains("DRAW NO BET") || mName.contains("APUESTA SIN EMPATE") || mName.contains("DNB");
        boolean isFirstCorner = mName.contains("FIRST CORNER") || mName.contains("PRIMER CÓRNER") || mName.contains("PRIMER ESQUINA");
        boolean isLastCorner = mName.contains("LAST CORNER") || mName.contains("ÚLTIMO CÓRNER") || mName.contains("ULTIMO CORNER");

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            if (isTotal) {
                Double points = extractNumber(oName, outcome.getHandicap(), mName);
                if (points == null) continue;
                BetType betType = null;
                if (isOver(upper)) {
                    betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.CORNERS, false, points);
                } else if (isUnder(upper)) {
                    betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.CORNERS, false, points);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("corners_total", scope), oName, odds, betType);
                }
            } else if (isHandicap) {
                Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
                if (hcp == null) continue;
                BetType betType = null;
                if (isTeam1(upper, event)) {
                    betType = mapHandicapRecord("1", scope, StatType.CORNERS, false, hcp);
                } else if (isTeam2(upper, event)) {
                    betType = mapHandicapRecord("2", scope, StatType.CORNERS, false, hcp);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("corners_handicap", scope), oName, odds, betType);
                }
            } else if (isDnb) {
                BetType betType = null;
                if (isTeam1(upper, event)) {
                    betType = mapHandicapRecord("1", scope, StatType.CORNERS, false, 0.0);
                } else if (isTeam2(upper, event)) {
                    betType = mapHandicapRecord("2", scope, StatType.CORNERS, false, 0.0);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("corners_dnb", scope), oName, odds, betType);
                }
            } else if (is1x2 || isFirstCorner || isLastCorner) {
                BetType betType = null;
                if (isTeam1(upper, event) || "1".equals(upper)) {
                    betType = map1X2Record("1", scope, StatType.CORNERS);
                } else if (isTeam2(upper, event) || "2".equals(upper)) {
                    betType = map1X2Record("2", scope, StatType.CORNERS);
                } else if (isDraw(upper)) {
                    betType = map1X2Record("X", scope, StatType.CORNERS);
                }
                if (betType != null) {
                    String grp = isFirstCorner ? "corners_first" : (isLastCorner ? "corners_last" : "corners_1x2");
                    addOddItem(items, formatGroupName(grp, scope), oName, odds, betType);
                }
            } else if (isDoubleChance) {
                BetType betType = null;
                if (upper.contains("1X") || upper.contains("HOME OR DRAW")) {
                    betType = map1X2DCRecord("1X", scope, StatType.CORNERS);
                } else if (upper.contains("12") || upper.contains("HOME OR AWAY")) {
                    betType = map1X2DCRecord("12", scope, StatType.CORNERS);
                } else if (upper.contains("X2") || upper.contains("DRAW OR AWAY")) {
                    betType = map1X2DCRecord("X2", scope, StatType.CORNERS);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("corners_dc", scope), oName, odds, betType);
                }
            } else {
                // Generic 1X2 fallback for corner winner markets
                BetType betType = null;
                if (isTeam1(upper, event) || "1".equals(upper)) {
                    betType = map1X2Record("1", scope, StatType.CORNERS);
                } else if (isTeam2(upper, event) || "2".equals(upper)) {
                    betType = map1X2Record("2", scope, StatType.CORNERS);
                } else if (isDraw(upper)) {
                    betType = map1X2Record("X", scope, StatType.CORNERS);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("corners_1x2", scope), oName, odds, betType);
                }
            }
        }
    }
}

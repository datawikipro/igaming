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
 * Handler for period / half / quarter / set 1X2 result markets:
 * - 1st Half / 2nd Half / Quarter 1..4 / Period 1..3 / Set 1..5
 * - Primer tiempo / Segundo tiempo / Cuarto 1..4 / Periodo / Set (Spanish)
 * Delegates scope detection to resolveScope() and maps outcomes as 1X2.
 */
@Component
@Order(50)
public class PeriodMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();

        // Exclude stats markets
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CÓRNER") ||
            mName.contains("CARD") || mName.contains("TARJETA") || mName.contains("AMARILLA")) {
            return false;
        }
        // Exclude HT/FT composite
        if (mName.contains("HALF TIME / FULL TIME") || mName.contains("HT/FT") || mName.contains("HT / FT") ||
            mName.contains("DESCANSO / FINAL") || mName.contains("DESCANSO/FINAL") ||
            mName.contains("MEDIO TIEMPO / TIEMPO COMPLETO") || mName.contains("MEDIO TIEMPO/TIEMPO COMPLETO")) {
            return false;
        }
        // Exclude total/handicap — those go to TotalMarketHandler / HandicapMarketHandler
        if (mName.contains("TOTAL") || mName.contains("HANDICAP") || mName.contains("HÁNDICAP") ||
            mName.contains("OVER") || mName.contains("UNDER") || mName.contains("MÁS DE") || mName.contains("MAS DE") ||
            mName.contains("SPREAD")) {
            return false;
        }
        // Exclude BTTS, DoubleChance, DrawNoBet, CorrectScore
        if (mName.contains("BOTH TEAMS") || mName.contains("AMBOS EQUIPOS") || mName.contains("BTTS") ||
            mName.contains("DOUBLE CHANCE") || mName.contains("DOBLE OPORTUNIDAD") || mName.contains("DOBLE CHANCE") ||
            mName.contains("DRAW NO BET") || mName.contains("APUESTA SIN EMPATE") || mName.contains("DNB") ||
            mName.contains("CORRECT SCORE") || mName.contains("MARCADOR EXACTO")) {
            return false;
        }

        BetScope scope = resolveScope(mName);
        return scope == BetScope.HALF_1 || scope == BetScope.HALF_2 ||
               scope == BetScope.QUARTER_1 || scope == BetScope.QUARTER_2 ||
               scope == BetScope.QUARTER_3 || scope == BetScope.QUARTER_4 ||
               scope == BetScope.PERIOD_1 || scope == BetScope.PERIOD_2 || scope == BetScope.PERIOD_3 ||
               scope == BetScope.SET_4 || scope == BetScope.SET_5;
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("1x2", scope);

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("1 ") ||
                upper.startsWith("HOME") || upper.startsWith("LOCAL") || upper.startsWith("CASA")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("2 ") ||
                       upper.startsWith("AWAY") || upper.startsWith("VISITANTE")) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if (isDraw(upper) || "X".equals(upper)) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

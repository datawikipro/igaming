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
 * Handler for 1X2, Match Winner, and Moneyline markets:
 * - 1X2 / Resultado del partido / Ganador (Spanish)
 * - Match Winner / Moneyline / Full Time Result (English)
 */
@Component
@Order(120)
public class MatchResultMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CÓRNER") ||
            mName.contains("CARD") || mName.contains("TARJETA") || mName.contains("AMARILLA")) {
            return false;
        }
        if (mName.contains("DOUBLE CHANCE") || mName.contains("DOBLE OPORTUNIDAD") || mName.contains("DOBLE CHANCE")) {
            return false;
        }
        if (mName.contains("DRAW NO BET") || mName.contains("APUESTA SIN EMPATE") ||
            mName.contains("EMPATE NO ACCIÓN") || mName.contains("EMPATE NO ACCION") ||
            mName.contains("EMPATE ANULA") || mName.contains("DNB")) {
            return false;
        }
        if (mName.contains("CORRECT SCORE") || mName.contains("MARCADOR EXACTO") || mName.contains("RESULTADO EXACTO")) {
            return false;
        }
        if (mName.contains("HALF TIME / FULL TIME") || mName.contains("HT/FT") || mName.contains("HT / FT") ||
            mName.contains("DESCANSO / FINAL") || mName.contains("DESCANSO/FINAL") ||
            mName.contains("MEDIO TIEMPO / TIEMPO COMPLETO") || mName.contains("MEDIO TIEMPO/TIEMPO COMPLETO")) {
            return false;
        }
        if (mName.contains("BOTH TEAMS") || mName.contains("AMBOS EQUIPOS") ||
            mName.contains("AMBOS MARCAN") || mName.contains("BTTS")) {
            return false;
        }

        return mName.contains("MATCH WINNER") ||
               mName.contains("WIN / DRAW / WIN") ||
               mName.contains("WIN-DRAW-WIN") ||
               mName.contains("1X2") ||
               mName.contains("MONEYLINE") ||
               mName.contains("MONEY LINE") ||
               mName.contains("MATCH RESULT") ||
               mName.contains("RESULTADO DEL PARTIDO") ||
               mName.contains("RESULTADO FINAL") ||
               mName.contains("RESULTADO TIEMPO REGLAMENTARIO") ||
               mName.contains("GANADOR DEL PARTIDO") ||
               mName.contains("GANADOR DE PARTIDO") ||
               mName.contains("GANADOR");
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
            } else if (isDraw(upper) || "X".equals(upper) || upper.contains("DRAW") ||
                       upper.contains("EMPATE") || upper.contains("TIE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

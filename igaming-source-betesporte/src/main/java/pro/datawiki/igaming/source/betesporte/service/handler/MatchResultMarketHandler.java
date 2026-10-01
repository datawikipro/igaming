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
 * Handler for 1X2, Match Winner, and Moneyline markets.
 */
@Component
@Order(120)
public class MatchResultMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO") || mName.contains("CARD") || mName.contains("CART")) return false;
        if (mName.contains("DOUBLE CHANCE") || mName.contains("DUPLA CHANCE") || mName.contains("CHANCE DUPLA")) return false;
        if (mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("EMPATE ANULA")) return false;
        if (mName.contains("CORRECT SCORE") || mName.contains("RESULTADO EXATO") || mName.contains("PLACAR EXATO")) return false;
        if (mName.contains("HALF TIME / FULL TIME") || mName.contains("HT/FT") || mName.contains("HT / FT") ||
            mName.contains("INTERVALO / FINAL") || mName.contains("INTERVALO/FINAL")) return false;
        if (mName.contains("BOTH TEAMS") || mName.contains("BTTS") || mName.contains("AMBAS MARCAM") || mName.contains("AMBOS MARCAM")) return false;

        return mName.contains("MATCH WINNER") ||
               mName.contains("WIN / DRAW / WIN") ||
               mName.contains("WIN-DRAW-WIN") ||
               mName.contains("1X2") ||
               mName.contains("MONEYLINE") ||
               mName.contains("MATCH RESULT") ||
               mName.contains("RESULTADO FINAL") ||
               mName.contains("RESULTADO") ||
               mName.contains("VENCEDOR DO ENCONTRO") ||
               mName.contains("VENCEDOR DA PARTIDA") ||
               mName.contains("VENCEDOR");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upperOutcome = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upperOutcome, event) || "1".equals(upperOutcome) || upperOutcome.startsWith("HOME") || upperOutcome.startsWith("CASA")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upperOutcome, event) || "2".equals(upperOutcome) || upperOutcome.startsWith("AWAY") || upperOutcome.startsWith("FORA")) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upperOutcome) || upperOutcome.contains("DRAW") || upperOutcome.contains("TIE") || upperOutcome.contains("EMPATE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "1x2", oName, odds, betType);
            }
        }
    }
}

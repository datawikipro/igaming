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
 * Handler for 1X2, Match Winner, and Moneyline markets in Caliente.
 */
@Component
public class MatchResultMarketHandler extends AbstractCalienteMarketHandler {

    @Override
    public boolean supports(CalienteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("TIRO DE ESQUINA") || mName.contains("ESQUINA") ||
            mName.contains("CARD") || mName.contains("TARJETA")) return false;
        if (mName.contains("DOUBLE CHANCE") || mName.contains("DOBLE OPORTUNIDAD")) return false;
        if (mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("EMPATE APUESTA NO VÁLIDA") ||
            mName.contains("EMPATE NO CUENTA") || mName.contains("EMPATE ANULA")) return false;

        return mName.contains("MATCH WINNER") ||
               mName.contains("WIN / DRAW / WIN") ||
               mName.contains("WIN-DRAW-WIN") ||
               mName.contains("1X2") ||
               mName.contains("MONEYLINE") ||
               mName.contains("MONEY LINE") ||
               mName.contains("LÍNEA DE DINERO") ||
               mName.contains("LINEA DE DINERO") ||
               mName.contains("MATCH RESULT") ||
               mName.contains("RESULTADO DEL PARTIDO") ||
               mName.contains("RESULTADO FINAL") ||
               mName.contains("GANADOR DEL PARTIDO") ||
               mName.contains("GANADOR DEL ENCUENTRO") ||
               mName.contains("GANADOR");
    }

    @Override
    public void handle(CalienteMarketDto market, CalienteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        for (CalienteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upperOutcome = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upperOutcome, event) || "1".equals(upperOutcome) || upperOutcome.startsWith("LOCAL") || upperOutcome.startsWith("HOME")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upperOutcome, event) || "2".equals(upperOutcome) || upperOutcome.startsWith("VISITANTE") || upperOutcome.startsWith("AWAY")) {
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

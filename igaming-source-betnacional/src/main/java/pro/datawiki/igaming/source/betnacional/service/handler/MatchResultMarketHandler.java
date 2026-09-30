package pro.datawiki.igaming.source.betnacional.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalMarketDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalOutcomeDto;

import java.util.List;

/**
 * Handler for 1X2, Match Winner, and Moneyline markets.
 */
@Component
public class MatchResultMarketHandler extends AbstractBetnacionalMarketHandler {

    @Override
    public boolean supports(BetnacionalMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CARD") || mName.contains("CART")) return false;
        if (mName.contains("DOUBLE CHANCE") || mName.contains("DUPLA CHANCE") || mName.contains("CHANCE DUPLA")) return false;
        if (mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("EMPATE ANULA")) return false;

        return mName.contains("MATCH WINNER") ||
               mName.contains("WIN / DRAW / WIN") ||
               mName.contains("WIN-DRAW-WIN") ||
               mName.contains("1X2") ||
               mName.contains("MONEYLINE") ||
               mName.contains("MATCH RESULT") ||
               mName.contains("RESULTADO FINAL") ||
               mName.contains("VENCEDOR DO ENCONTRO") ||
               mName.contains("VENCEDOR DA PARTIDA") ||
               mName.contains("VENCEDOR");
    }

    @Override
    public void handle(BetnacionalMarketDto market, BetnacionalEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        for (BetnacionalOutcomeDto outcome : market.getOutcomes()) {
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

    private boolean isTeam1(String outcomeName, BetnacionalEventDto event) {
        if (event.getHomeTeam() != null && outcomeName.contains(event.getHomeTeam().toUpperCase())) return true;
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME") || outcomeName.startsWith("CASA");
    }

    private boolean isTeam2(String outcomeName, BetnacionalEventDto event) {
        if (event.getAwayTeam() != null && outcomeName.contains(event.getAwayTeam().toUpperCase())) return true;
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY") || outcomeName.startsWith("FORA");
    }
}

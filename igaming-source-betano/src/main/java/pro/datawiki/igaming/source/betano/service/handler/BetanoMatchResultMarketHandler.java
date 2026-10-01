package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Handler for 1X2, Match Winner, Moneyline, Head-to-Head markets for Betano.
 */
@Component
public class BetanoMatchResultMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName();
        if (isStatsMarket(mName)) return false;
        if (mName.contains("DOUBLE CHANCE")) return false;
        if (mName.contains("DRAW NO BET") || mName.contains("DNB")) return false;

        return mName.contains("MATCH WINNER") ||
               mName.contains("WIN / DRAW / WIN") ||
               mName.contains("WIN-DRAW-WIN") ||
               mName.contains("1X2") ||
               mName.contains("MONEYLINE") ||
               mName.contains("MATCH RESULT") ||
               mName.contains("FULL TIME RESULT") ||
               mName.contains("HEAD TO HEAD") ||
               mName.contains("HEAD-TO-HEAD") ||
               mName.contains("RESULTADO FINAL") ||
               mName.contains("VENCEDOR") ||
               mName.equals("RESULT") ||
               mName.equals("WINNER");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upperOutcome = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upperOutcome, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upperOutcome, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upperOutcome) || upperOutcome.contains("DRAW") || upperOutcome.contains("TIE") ||
                       upperOutcome.contains("EMPATE") || "OT_DRAW".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                String group = formatGroupName("1x2", scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

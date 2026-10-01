package pro.datawiki.igaming.source.wplay.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Both Teams To Score (BTTS) markets:
 * - Ambos equipos marcarán / Ambos marcan (Spanish)
 * - Both Teams To Score / BTTS (English)
 * - Both Halves BTTS (Ambos equipos marcarán en ambos tiempos)
 */
@Component
@Order(40)
public class BothTeamsToScoreMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CÓRNER") ||
            mName.contains("CARD") || mName.contains("TARJETA") || mName.contains("AMARILLA")) {
            return false;
        }

        return mName.contains("BOTH TEAMS TO SCORE") ||
               mName.contains("BOTH TEAMS SCORE") ||
               mName.contains("BTTS") ||
               mName.contains("AMBOS EQUIPOS MARCARÁN") ||
               mName.contains("AMBOS EQUIPOS MARCARAN") ||
               mName.contains("AMBOS MARCAN") ||
               mName.contains("AMBOS EQUIPOS ANOTARÁN") ||
               mName.contains("AMBOS EQUIPOS ANOTARAN") ||
               mName.contains("AMBOS ANOTAN") ||
               mName.contains("MARCAN AMBOS EQUIPOS") ||
               mName.contains("ANOTAN AMBOS EQUIPOS") ||
               mName.contains("GOAL / GOAL") ||
               mName.contains("GOAL/GOAL") ||
               mName.contains("GG/NG");
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        boolean isBothHalves = mName.contains("BOTH HALVES") || mName.contains("IN BOTH HALVES") ||
                               mName.contains("AMBOS TIEMPOS") || mName.contains("EN AMBOS TIEMPOS") ||
                               mName.contains("AMBAS MITADES") || mName.contains("EN AMBAS MITADES");

        BetScope scope = isBothHalves ? BetScope.FULL_MATCH : resolveScope(mName + " " + period);
        String group = isBothHalves ? "btts_both_halves" : formatGroupName("btts", scope);
        BinaryMarketBet.MarketType marketType = isBothHalves ? BinaryMarketBet.MarketType.BOTH_HALVES_BTTS : BinaryMarketBet.MarketType.BTTS;

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bttsOutcome = null;
            if (upper.equals("SÍ") || upper.equals("SI") || upper.startsWith("SÍ") || upper.startsWith("SI") ||
                upper.equals("YES") || upper.startsWith("YES") || upper.equals("S") || upper.equals("GG") ||
                upper.equals("GOAL/GOAL") || upper.equals("GOAL / GOAL")) {
                bttsOutcome = BinaryMarketBet.Outcome.YES;
            } else if (upper.equals("NO") || upper.startsWith("NO") || upper.equals("N") || upper.equals("NG") ||
                       upper.equals("NO GOAL") || upper.startsWith("NO GOAL")) {
                bttsOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bttsOutcome != null) {
                BinaryMarketBet betType = new BinaryMarketBet(scope, BetSubject.MATCH,
                        marketType, bttsOutcome, StatType.MATCH);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

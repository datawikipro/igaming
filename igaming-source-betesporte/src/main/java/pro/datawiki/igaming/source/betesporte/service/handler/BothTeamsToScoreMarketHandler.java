package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Both Teams To Score (BTTS) markets:
 * - Match BTTS (Ambas Marcam: Sim / Não, Both Teams To Score: Yes / No)
 * - Half BTTS (1º Tempo / 2º Tempo Ambas Marcam)
 * - Both Halves BTTS (Ambas Marcam em Ambos os Tempos / Both Halves BTTS)
 */
@Component
@Order(50)
public class BothTeamsToScoreMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO") ||
            mName.contains("CARD") || mName.contains("CART") || mName.contains("AMAREL")) {
            return false;
        }

        return mName.contains("BOTH TEAMS TO SCORE") ||
               mName.contains("BOTH TEAMS SCORE") ||
               mName.contains("BTTS") ||
               mName.contains("AMBAS MARCAM") ||
               mName.contains("AMBOS MARCAM") ||
               mName.contains("AMBAS AS EQUIPES MARCAM") ||
               mName.contains("AMBAS EQUIPES MARCAM") ||
               mName.contains("AMBAS AS EQUIPES MARCARÃO") ||
               mName.contains("AMBAS AS EQUIPES MARCARAO") ||
               mName.contains("AMBAS EQUIPES MARCARÃO") ||
               mName.contains("AMBAS EQUIPES MARCARAO") ||
               mName.contains("GOAL / GOAL") ||
               mName.contains("GOAL/GOAL") ||
               mName.contains("GG/NG");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        boolean isBothHalves = mName.contains("BOTH HALVES") || mName.contains("IN BOTH HALVES") ||
                               mName.contains("AMBOS OS TEMPOS") || mName.contains("EM AMBOS OS TEMPOS") ||
                               mName.contains("AMBOS TEMPOS");

        BetScope scope = isBothHalves ? BetScope.FULL_MATCH : resolveScope(mName + " " + period);
        String group = isBothHalves ? "btts_both_halves" : formatGroupName("btts", scope);
        BinaryMarketBet.MarketType marketType = isBothHalves ? BinaryMarketBet.MarketType.BOTH_HALVES_BTTS : BinaryMarketBet.MarketType.BTTS;

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bttsOutcome = null;
            if (upper.equals("SIM") || upper.startsWith("SIM") ||
                upper.equals("YES") || upper.startsWith("YES") ||
                upper.equals("S") || upper.equals("GG") ||
                upper.equals("GOAL/GOAL") || upper.equals("GOAL / GOAL")) {
                bttsOutcome = BinaryMarketBet.Outcome.YES;
            } else if (upper.equals("NÃO") || upper.startsWith("NÃO") ||
                       upper.equals("NAO") || upper.startsWith("NAO") ||
                       upper.equals("NO") || upper.startsWith("NO") ||
                       upper.equals("N") || upper.equals("NG") ||
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

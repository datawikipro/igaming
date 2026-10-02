package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
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
 * Handler for Both Teams To Score (Ambas Marcam) markets.
 */
@Component
@Order(40)
public class BothTeamsToScoreMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CARD") || mName.contains("CART")) return false;

        return mName.contains("BOTH TEAMS TO SCORE") ||
               mName.contains("BTTS") ||
               mName.contains("AMBAS MARCAM") ||
               mName.contains("AMBOS MARCAM") ||
               mName.contains("AMBAS AS EQUIPES MARCAM") ||
               mName.contains("AMBAS EQUIPES MARCAM") ||
               mName.contains("AMBOS OS TIMES MARCAM");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("btts", scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.equals("YES") || upper.equals("SIM") || upper.startsWith("SIM") || upper.equals("S")) {
                betType = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH);
            } else if (upper.equals("NO") || upper.equals("NÃO") || upper.equals("NAO") || upper.startsWith("NÃO") || upper.startsWith("NAO") || upper.equals("N")) {
                betType = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

package pro.datawiki.igaming.source.bet7k.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kMarketDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kOutcomeDto;

import java.util.List;

/**
 * Handler for Over/Under totals (Match Totals, Team Totals).
 */
@Component
@Order(100)
public class TotalMarketHandler extends AbstractBet7kMarketHandler {

    @Override
    public boolean supports(Bet7kMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO") || mName.contains("CARD") || mName.contains("CART")) return false;

        return mName.contains("TOTAL") ||
               mName.contains("OVER/UNDER") ||
               mName.contains("OVER / UNDER") ||
               mName.contains("MAIS/MENOS") ||
               mName.contains("MAIS / MENOS") ||
               mName.contains("ACIMA/ABAIXO") ||
               mName.contains("ACIMA / ABAIXO") ||
               mName.contains("GOALS O/U") ||
               mName.contains("POINTS O/U") ||
               mName.contains("GOLS MAIS/MENOS");
    }

    @Override
    public void handle(Bet7kMarketDto market, Bet7kEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        BetSubject subject = resolveSubject(mName, event);

        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") ||
                upper.startsWith("MAIS") || upper.startsWith("ACIMA") || upper.contains(" MAIS ") || upper.contains(" ACIMA ")) {
                betType = mapTotalRecord("OVER", scope, subject, StatType.MATCH, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") ||
                       upper.startsWith("MENOS") || upper.startsWith("ABAIXO") || upper.contains(" MENOS ") || upper.contains(" ABAIXO ")) {
                betType = mapTotalRecord("UNDER", scope, subject, StatType.MATCH, false, points);
            }

            if (betType != null) {
                String baseGroup = (subject == BetSubject.MATCH) ? "total" : ("total_" + subject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}

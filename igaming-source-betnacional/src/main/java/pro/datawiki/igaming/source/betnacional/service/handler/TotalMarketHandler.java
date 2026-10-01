package pro.datawiki.igaming.source.betnacional.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalMarketDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalOutcomeDto;

import java.util.List;

/**
 * Handler for Over/Under totals (Match Totals, Team Totals).
 */
@Component
public class TotalMarketHandler extends AbstractBetnacionalMarketHandler {

    @Override
    public boolean supports(BetnacionalMarketDto market, SportType sportType) {
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
    public void handle(BetnacionalMarketDto market, BetnacionalEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        BetSubject subject = resolveSubject(mName, event);

        for (BetnacionalOutcomeDto outcome : market.getOutcomes()) {
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

    private BetSubject resolveSubject(String marketName, BetnacionalEventDto event) {
        if (event.getHomeTeam() != null && marketName.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event.getAwayTeam() != null && marketName.contains(event.getAwayTeam().toUpperCase())) {
            return BetSubject.TEAM2;
        }
        if (marketName.contains("HOME TOTAL") || marketName.contains("TEAM 1 TOTAL") ||
            marketName.contains("TOTAL CASA") || marketName.contains("CASA TOTAL")) {
            return BetSubject.TEAM1;
        }
        if (marketName.contains("AWAY TOTAL") || marketName.contains("TEAM 2 TOTAL") ||
            marketName.contains("TOTAL FORA") || marketName.contains("FORA TOTAL")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }
}

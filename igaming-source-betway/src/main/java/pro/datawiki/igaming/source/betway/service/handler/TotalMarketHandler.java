package pro.datawiki.igaming.source.betway.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;

/**
 * Handler for Over/Under totals (Match Totals, Team Totals).
 */
@Component
public class TotalMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false; // Handled by EsportsMarketHandler
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;

        return mName.contains("TOTAL") ||
               mName.contains("OVER/UNDER") ||
               mName.contains("OVER / UNDER") ||
               mName.contains("GOALS O/U") ||
               mName.contains("POINTS O/U");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        BetSubject subject = resolveSubject(mName, event);

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ")) {
                betType = mapTotalRecord("OVER", scope, subject, StatType.MATCH, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ")) {
                betType = mapTotalRecord("UNDER", scope, subject, StatType.MATCH, false, points);
            }

            if (betType != null) {
                String group = (subject == BetSubject.MATCH) ? "total" : ("total_" + subject.name().toLowerCase());
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private BetSubject resolveSubject(String marketName, BetwayEventDto event) {
        if (event.getHomeTeam() != null && marketName.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event.getAwayTeam() != null && marketName.contains(event.getAwayTeam().toUpperCase())) {
            return BetSubject.TEAM2;
        }
        if (marketName.contains("HOME TOTAL") || marketName.contains("TEAM 1 TOTAL")) {
            return BetSubject.TEAM1;
        }
        if (marketName.contains("AWAY TOTAL") || marketName.contains("TEAM 2 TOTAL")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }
}

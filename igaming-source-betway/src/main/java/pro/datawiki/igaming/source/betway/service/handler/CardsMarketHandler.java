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
 * Dedicated handler for Yellow Cards, Cards, and Bookings statistics:
 * - Match Total Cards / Bookings (Over / Under)
 * - Team Total Cards / Bookings (Home / Away)
 * - Cards 1X2 (Most Cards / Most Bookings)
 * - Cards Handicap
 */
@Component
public class CardsMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("CARD") || mName.contains("BOOKING");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        if (mName.contains("HANDICAP") || mName.contains("SPREAD")) {
            handleCardsHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER")) {
            handleCardsTotal(market, event, scope, items);
        } else if (mName.contains("1X2") || mName.contains("WINNER") || mName.contains("MATCH") || mName.contains("MOST")) {
            handleCards1X2(market, event, scope, items);
        }
    }

    private void handleCardsTotal(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject subject = resolveSubject(mName, event);
        String group = (subject == BetSubject.MATCH) ? "cards_total" : ("cards_total_" + subject.name().toLowerCase());

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ")) {
                betType = mapTotalRecord("OVER", scope, subject, StatType.YELLOW_CARDS, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ")) {
                betType = mapTotalRecord("UNDER", scope, subject, StatType.YELLOW_CARDS, false, points);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCards1X2(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME")) {
                betType = map1X2Record("1", scope, StatType.YELLOW_CARDS);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY")) {
                betType = map1X2Record("2", scope, StatType.YELLOW_CARDS);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE")) {
                betType = map1X2Record("X", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, "cards_1x2", oName, odds, betType);
            }
        }
    }

    private void handleCardsHandicap(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = mapHandicapRecord("1", scope, StatType.YELLOW_CARDS, false, hdp);
            } else if (isTeam2(upper, event)) {
                betType = mapHandicapRecord("2", scope, StatType.YELLOW_CARDS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, "cards_handicap", oName, odds, betType);
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
        if (marketName.contains("HOME CARD") || marketName.contains("HOME BOOKING") || marketName.contains("TEAM 1 CARD")) {
            return BetSubject.TEAM1;
        }
        if (marketName.contains("AWAY CARD") || marketName.contains("AWAY BOOKING") || marketName.contains("TEAM 2 CARD")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }

    private boolean isTeam1(String outcomeName, BetwayEventDto event) {
        if (event.getHomeTeam() != null && outcomeName.contains(event.getHomeTeam().toUpperCase())) return true;
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME");
    }

    private boolean isTeam2(String outcomeName, BetwayEventDto event) {
        if (event.getAwayTeam() != null && outcomeName.contains(event.getAwayTeam().toUpperCase())) return true;
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY");
    }
}

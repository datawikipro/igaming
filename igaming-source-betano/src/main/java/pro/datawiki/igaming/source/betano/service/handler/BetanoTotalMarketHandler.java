package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Handler for Over/Under Totals markets (Match & Team totals) for Betano.
 */
@Component
public class BetanoTotalMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING") || mName.contains("FOUL") || mName.contains("OFFSIDE")) return false;

        return mName.contains("TOTAL") ||
               mName.contains("OVER/UNDER") ||
               mName.contains("OVER / UNDER") ||
               mName.contains("MAIS/MENOS") ||
               mName.contains("GOALS O/U") ||
               mName.contains("POINTS O/U") ||
               mName.matches(".*\\bO/U\\b.*");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        BetSubject marketSubject = resolveSubject(mName, event);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), mName);
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetSubject outcomeSubject = marketSubject;
            if (outcomeSubject == BetSubject.MATCH) {
                if (isTeam1(upper, event)) {
                    outcomeSubject = BetSubject.TEAM1;
                } else if (isTeam2(upper, event)) {
                    outcomeSubject = BetSubject.TEAM2;
                }
            }

            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.endsWith(" OVER") ||
                upper.startsWith("MAIS") || upper.startsWith("MÁS") || upper.startsWith(">") || "OT_OVER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("OVER", scope, outcomeSubject, StatType.MATCH, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.endsWith(" UNDER") ||
                       upper.startsWith("MENOS") || upper.startsWith("<") || "OT_UNDER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("UNDER", scope, outcomeSubject, StatType.MATCH, false, points);
            }

            if (betType != null) {
                String baseGroup = (outcomeSubject == BetSubject.MATCH) ? "total" : ("total_" + outcomeSubject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private BetSubject resolveSubject(String marketName, BetanoEventDto event) {
        if (event != null && event.getHomeTeam() != null && marketName.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event != null && event.getAwayTeam() != null && marketName.contains(event.getAwayTeam().toUpperCase())) {
            return BetSubject.TEAM2;
        }
        if (marketName.contains("HOME TOTAL") || marketName.contains("TEAM 1 TOTAL") ||
            marketName.contains("HOME TEAM TOTAL") || marketName.contains("TOTAL - HOME") ||
            marketName.contains("TOTAL - TEAM 1") || marketName.contains("TOTAL CASA")) {
            return BetSubject.TEAM1;
        }
        if (marketName.contains("AWAY TOTAL") || marketName.contains("TEAM 2 TOTAL") ||
            marketName.contains("AWAY TEAM TOTAL") || marketName.contains("TOTAL - AWAY") ||
            marketName.contains("TOTAL - TEAM 2") || marketName.contains("TOTAL FORA")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }

    private boolean isTeam1(String outcomeName, BetanoEventDto event) {
        if (event != null && event.getHomeTeam() != null) {
            String home = event.getHomeTeam().toUpperCase();
            if (outcomeName.contains(home) || (home.length() >= 3 && home.contains(outcomeName))) return true;
        }
        return outcomeName.contains("TEAM 1") || outcomeName.contains("TEAM1") || outcomeName.startsWith("HOME") || outcomeName.contains("CASA");
    }

    private boolean isTeam2(String outcomeName, BetanoEventDto event) {
        if (event != null && event.getAwayTeam() != null) {
            String away = event.getAwayTeam().toUpperCase();
            if (outcomeName.contains(away) || (away.length() >= 3 && away.contains(outcomeName))) return true;
        }
        return outcomeName.contains("TEAM 2") || outcomeName.contains("TEAM2") || outcomeName.startsWith("AWAY") || outcomeName.contains("FORA");
    }
}

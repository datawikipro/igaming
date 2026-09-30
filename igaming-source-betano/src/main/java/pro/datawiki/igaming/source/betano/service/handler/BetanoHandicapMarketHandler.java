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
 * Handler for Handicap and Spread markets for Betano.
 */
@Component
public class BetanoHandicapMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING") || mName.contains("FOUL") || mName.contains("OFFSIDE")) return false;

        return mName.contains("HANDICAP") ||
               mName.contains("SPREAD") ||
               mName.contains("POINT SPREAD") ||
               mName.contains("PUCK LINE") ||
               mName.contains("RUN LINE") ||
               mName.contains("ASIAN HANDICAP") ||
               mName.contains("HANDICAP ASIATICO");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("handicap", scope);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), mName);
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, false, hdp);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isTeam1(String outcomeName, BetanoEventDto event) {
        if (event != null && event.getHomeTeam() != null) {
            String home = event.getHomeTeam().toUpperCase();
            if (outcomeName.contains(home) || (home.length() >= 3 && home.contains(outcomeName))) return true;
        }
        return "1".equals(outcomeName) || outcomeName.startsWith("1 ") || outcomeName.startsWith("1(") || outcomeName.startsWith("1 (") ||
               outcomeName.startsWith("HOME") || outcomeName.contains("TEAM 1") || outcomeName.contains("TEAM1") || outcomeName.startsWith("CASA");
    }

    private boolean isTeam2(String outcomeName, BetanoEventDto event) {
        if (event != null && event.getAwayTeam() != null) {
            String away = event.getAwayTeam().toUpperCase();
            if (outcomeName.contains(away) || (away.length() >= 3 && away.contains(outcomeName))) return true;
        }
        return "2".equals(outcomeName) || outcomeName.startsWith("2 ") || outcomeName.startsWith("2(") || outcomeName.startsWith("2 (") ||
               outcomeName.startsWith("AWAY") || outcomeName.contains("TEAM 2") || outcomeName.contains("TEAM2") || outcomeName.startsWith("FORA");
    }
}

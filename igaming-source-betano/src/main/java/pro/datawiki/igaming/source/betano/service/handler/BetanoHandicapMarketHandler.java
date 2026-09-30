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
        String mName = market.getEffectiveName();
        if (isStatsMarket(mName)) return false;

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
}

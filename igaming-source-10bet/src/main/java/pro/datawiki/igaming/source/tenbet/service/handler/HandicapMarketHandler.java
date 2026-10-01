package pro.datawiki.igaming.source.tenbet.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.tenbet.dto.TenBetEventDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetMarketDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetOutcomeDto;

import java.util.List;

/**
 * Handler for Point Spread and Asian Handicap markets.
 */
@Component
public class HandicapMarketHandler extends AbstractTenBetMarketHandler {

    @Override
    public boolean supports(TenBetMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false; // Handled by EsportsMarketHandler
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;

        return mName.contains("HANDICAP") ||
               mName.contains("SPREAD") ||
               mName.contains("POINT SPREAD") ||
               mName.contains("PUCK LINE") ||
               mName.contains("RUN LINE");
    }

    @Override
    public void handle(TenBetMarketDto market, TenBetEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, false, hdp);
            } else if (isTeam2(upper, event)) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, false, hdp);
            }

            if (betType != null) {
                String group = formatGroupName("handicap", scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isTeam1(String outcomeName, TenBetEventDto event) {
        if (event.getHomeTeam() != null && outcomeName.contains(event.getHomeTeam().toUpperCase())) return true;
        return outcomeName.startsWith("1") || outcomeName.contains("HOME");
    }

    private boolean isTeam2(String outcomeName, TenBetEventDto event) {
        if (event.getAwayTeam() != null && outcomeName.contains(event.getAwayTeam().toUpperCase())) return true;
        return outcomeName.startsWith("2") || outcomeName.contains("AWAY");
    }
}

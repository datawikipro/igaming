package pro.datawiki.igaming.source.bovada.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.source.bovada.dto.BovadaMarketDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaOutcomeDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class BovadaSpreadStrategy extends AbstractBovadaStrategy {

    @Override
    public boolean supports(BovadaMarketDto market) {
        String key = market.getKey() != null ? market.getKey().toUpperCase() : "";
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";

        if (desc.contains("total")) return false;

        return "2W-HCAP".equals(key) || desc.contains("point spread")
                || desc.contains("spread") || desc.contains("puck line")
                || desc.contains("run line") || desc.contains("handicap");
    }

    @Override
    public List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2) {
        List<OddItem> items = new ArrayList<>();
        if (market.getOutcomes() == null) return items;

        BetScope scope = BovadaScopeResolver.resolve(market.getPeriod(), sportType);

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            Double hdp = parseHandicap(outcome);
            if (hdp == null) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";

            HandicapBet.Outcome outcomeType = null;
            if ("H".equals(type) || (team1 != null && desc.toLowerCase().contains(team1.toLowerCase()))) {
                outcomeType = HandicapBet.Outcome.TEAM1;
            } else if ("A".equals(type) || (team2 != null && desc.toLowerCase().contains(team2.toLowerCase()))) {
                outcomeType = HandicapBet.Outcome.TEAM2;
            }

            if (outcomeType != null) {
                OddItem item = createOddItem(
                        "spread",
                        desc.isEmpty() ? type + " " + hdp : desc,
                        decimal,
                        new HandicapBet(scope, outcomeType, hdp, false, null)
                );
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}

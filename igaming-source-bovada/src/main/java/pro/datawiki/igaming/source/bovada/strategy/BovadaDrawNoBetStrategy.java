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
public class BovadaDrawNoBetStrategy extends AbstractBovadaStrategy {

    @Override
    public boolean supports(BovadaMarketDto market) {
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";
        return desc.contains("draw no bet") || desc.contains("tie no bet") || desc.equals("dnb");
    }

    @Override
    public List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2) {
        List<OddItem> items = new ArrayList<>();
        if (market.getOutcomes() == null) return items;

        BetScope scope = BovadaScopeResolver.resolve(market.getPeriod(), sportType);

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

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
                        "draw_no_bet",
                        desc.isEmpty() ? type : desc,
                        decimal,
                        new HandicapBet(scope, outcomeType, 0.0, false, null)
                );
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}

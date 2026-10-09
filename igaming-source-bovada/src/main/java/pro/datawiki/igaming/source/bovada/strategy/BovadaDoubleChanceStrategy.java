package pro.datawiki.igaming.source.bovada.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.bovada.dto.BovadaMarketDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaOutcomeDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class BovadaDoubleChanceStrategy extends AbstractBovadaStrategy {

    @Override
    public boolean supports(BovadaMarketDto market) {
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";
        return desc.contains("double chance");
    }

    @Override
    public List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2) {
        List<OddItem> items = new ArrayList<>();
        if (market.getOutcomes() == null) return items;

        BetScope scope = BovadaScopeResolver.resolve(market.getPeriod(), sportType);

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";
            String type = outcome.getType() != null ? outcome.getType().trim().toUpperCase() : "";

            MatchResultBet.Outcome dcOutcome = null;
            if ("1X".equals(type) || desc.equalsIgnoreCase("1X") || (team1 != null && desc.contains(team1) && desc.contains("Draw"))) {
                dcOutcome = MatchResultBet.Outcome.DC_1X;
            } else if ("12".equals(type) || desc.equalsIgnoreCase("12") || (team1 != null && team2 != null && desc.contains(team1) && desc.contains(team2))) {
                dcOutcome = MatchResultBet.Outcome.DC_12;
            } else if ("X2".equals(type) || "2X".equals(type) || desc.equalsIgnoreCase("X2") || (team2 != null && desc.contains(team2) && desc.contains("Draw"))) {
                dcOutcome = MatchResultBet.Outcome.DC_X2;
            }

            if (dcOutcome != null) {
                OddItem item = createOddItem(
                        "double_chance",
                        desc.isEmpty() ? type : desc,
                        decimal,
                        new MatchResultBet(scope, dcOutcome, null)
                );
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}

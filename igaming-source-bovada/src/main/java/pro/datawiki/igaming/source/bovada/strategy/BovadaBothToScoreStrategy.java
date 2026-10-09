package pro.datawiki.igaming.source.bovada.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.source.bovada.dto.BovadaMarketDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaOutcomeDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class BovadaBothToScoreStrategy extends AbstractBovadaStrategy {

    @Override
    public boolean supports(BovadaMarketDto market) {
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";
        return desc.contains("both teams to score") || desc.contains("both to score") || desc.equals("btts");
    }

    @Override
    public List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2) {
        List<OddItem> items = new ArrayList<>();
        if (market.getOutcomes() == null) return items;

        BetScope scope = BovadaScopeResolver.resolve(market.getPeriod(), sportType);

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            String desc = outcome.getDescription() != null ? outcome.getDescription().trim().toLowerCase() : "";
            String type = outcome.getType() != null ? outcome.getType().trim().toLowerCase() : "";

            BinaryMarketBet.Outcome binaryOutcome = null;
            if (desc.contains("yes") || type.contains("yes") || "y".equals(type)) {
                binaryOutcome = BinaryMarketBet.Outcome.YES;
            } else if (desc.contains("no") || type.contains("no") || "n".equals(type)) {
                binaryOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (binaryOutcome != null) {
                OddItem item = createOddItem(
                        "both_teams_to_score",
                        outcome.getDescription() != null ? outcome.getDescription() : type,
                        decimal,
                        new BinaryMarketBet(scope, pro.datawiki.igaming.dto.market.BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, binaryOutcome, null)
                );
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}

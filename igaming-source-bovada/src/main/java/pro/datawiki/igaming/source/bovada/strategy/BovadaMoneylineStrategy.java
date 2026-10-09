package pro.datawiki.igaming.source.bovada.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bovada.dto.BovadaMarketDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaOutcomeDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class BovadaMoneylineStrategy extends AbstractBovadaStrategy {

    @Override
    public boolean supports(BovadaMarketDto market) {
        String key = market.getKey() != null ? market.getKey().toUpperCase() : "";
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";

        if (desc.contains("half time / full time") || desc.contains("double chance") || desc.contains("draw no bet")
                || desc.contains("both teams to score") || desc.contains("spread") || desc.contains("total")) {
            return false;
        }

        return "2W-12".equals(key) || "3W-1X2".equals(key)
                || desc.contains("moneyline") || desc.contains("money line")
                || desc.contains("match winner") || desc.contains("head to head")
                || desc.equals("winner") || desc.equals("game winner");
    }

    @Override
    public List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2) {
        List<OddItem> items = new ArrayList<>();
        if (market.getOutcomes() == null) return items;

        BetScope scope = BovadaScopeResolver.resolve(market.getPeriod(), sportType);

        boolean hasDraw = market.getOutcomes().stream().anyMatch(o -> {
            String t = o.getType() != null ? o.getType().toUpperCase() : "";
            String d = o.getDescription() != null ? o.getDescription().toLowerCase() : "";
            return "D".equals(t) || d.contains("draw") || d.contains("tie");
        });

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";

            MatchResultBet.Outcome outcomeType = null;
            if ("H".equals(type) || (team1 != null && desc.equalsIgnoreCase(team1))) {
                outcomeType = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            } else if ("A".equals(type) || (team2 != null && desc.equalsIgnoreCase(team2))) {
                outcomeType = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            } else if ("D".equals(type) || desc.equalsIgnoreCase("Draw") || desc.equalsIgnoreCase("Tie")) {
                outcomeType = MatchResultBet.Outcome.DRAW;
            }

            if (outcomeType != null) {
                OddItem item = createOddItem(
                        "moneyline",
                        desc.isEmpty() ? type : desc,
                        decimal,
                        new MatchResultBet(scope, outcomeType, null)
                );
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}

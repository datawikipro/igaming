package pro.datawiki.igaming.source.bovada.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bovada.dto.BovadaMarketDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaOutcomeDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class BovadaTotalStrategy extends AbstractBovadaStrategy {

    @Override
    public boolean supports(BovadaMarketDto market, String team1, String team2) {
        if (!supports(market)) return false;
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";
        if (team1 != null && !team1.isBlank() && desc.contains(team1.toLowerCase())) {
            return false;
        }
        if (team2 != null && !team2.isBlank() && desc.contains(team2.toLowerCase())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(BovadaMarketDto market) {
        String key = market.getKey() != null ? market.getKey().toUpperCase() : "";
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";

        if (desc.contains("team total") || desc.contains("home total") || desc.contains("away total")
                || desc.contains("team 1") || desc.contains("team 2")) {
            return false;
        }

        return "2W-OU".equals(key) || desc.contains("total") || desc.contains("over/under");
    }

    @Override
    public List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2) {
        List<OddItem> items = new ArrayList<>();
        if (market.getOutcomes() == null) return items;

        BetScope scope = BovadaScopeResolver.resolve(market.getPeriod(), sportType);

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            Double total = parseTotal(outcome);
            if (total == null) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";

            TotalBet.Direction direction = null;
            if ("O".equals(type) || desc.toLowerCase().startsWith("over") || desc.toLowerCase().startsWith("o ")) {
                direction = TotalBet.Direction.OVER;
            } else if ("U".equals(type) || desc.toLowerCase().startsWith("under") || desc.toLowerCase().startsWith("u ")) {
                direction = TotalBet.Direction.UNDER;
            }

            if (direction != null) {
                OddItem item = createOddItem(
                        "total",
                        desc.isEmpty() ? type + " " + total : desc,
                        decimal,
                        new TotalBet(scope, BetSubject.MATCH, direction, total, false, null)
                );
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}

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
public class BovadaTeamTotalStrategy extends AbstractBovadaStrategy {

    @Override
    public boolean supports(BovadaMarketDto market, String team1, String team2) {
        if (supports(market)) return true;
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";
        if (desc.contains("total") || desc.contains("over/under")) {
            if (team1 != null && !team1.isBlank() && desc.contains(team1.toLowerCase())) return true;
            if (team2 != null && !team2.isBlank() && desc.contains(team2.toLowerCase())) return true;
        }
        return false;
    }

    @Override
    public boolean supports(BovadaMarketDto market) {
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";
        return desc.contains("team total") || desc.contains("home total") || desc.contains("away total")
                || (desc.contains("total") && (desc.contains("team 1") || desc.contains("team 2")));
    }

    @Override
    public List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2) {
        List<OddItem> items = new ArrayList<>();
        if (market.getOutcomes() == null) return items;

        BetScope scope = BovadaScopeResolver.resolve(market.getPeriod(), sportType);
        String marketDesc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";

        BetSubject subject = BetSubject.TEAM1;
        if (marketDesc.contains("away") || marketDesc.contains("team 2") || (team2 != null && marketDesc.contains(team2.toLowerCase()))) {
            subject = BetSubject.TEAM2;
        }

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
                String groupName = (subject == BetSubject.TEAM1) ? "total_team1" : "total_team2";
                OddItem item = createOddItem(
                        groupName,
                        desc.isEmpty() ? type + " " + total : desc,
                        decimal,
                        new TotalBet(scope, subject, direction, total, false, null)
                );
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}

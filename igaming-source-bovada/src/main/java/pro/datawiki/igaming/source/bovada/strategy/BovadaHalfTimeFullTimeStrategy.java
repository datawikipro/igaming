package pro.datawiki.igaming.source.bovada.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.bovada.dto.BovadaMarketDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaOutcomeDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class BovadaHalfTimeFullTimeStrategy extends AbstractBovadaStrategy {

    @Override
    public boolean supports(BovadaMarketDto market) {
        String desc = market.getDescription() != null ? market.getDescription().toLowerCase() : "";
        return desc.contains("half time / full time") || desc.contains("double result") || desc.contains("ht/ft");
    }

    @Override
    public List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2) {
        List<OddItem> items = new ArrayList<>();
        if (market.getOutcomes() == null) return items;

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";
            HalfTimeFullTimeBet.Outcome htftOutcome = parseOutcome(desc, team1, team2);

            if (htftOutcome != null) {
                OddItem item = createOddItem(
                        "ht_ft",
                        desc,
                        decimal,
                        new HalfTimeFullTimeBet(htftOutcome)
                );
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    private HalfTimeFullTimeBet.Outcome parseOutcome(String desc, String team1, String team2) {
        if (desc == null || desc.isBlank()) return null;
        String normalized = desc.toUpperCase()
                .replace("/", " - ")
                .replace(" / ", " - ");

        if (team1 != null && !team1.isBlank()) {
            normalized = normalized.replace(team1.toUpperCase(), "1");
        }
        if (team2 != null && !team2.isBlank()) {
            normalized = normalized.replace(team2.toUpperCase(), "2");
        }
        normalized = normalized.replace("HOME", "1")
                .replace("AWAY", "2")
                .replace("DRAW", "X")
                .replace("TIE", "X")
                .replaceAll("\\s+", "");

        return switch (normalized) {
            case "1-1", "1/1", "11" -> HalfTimeFullTimeBet.Outcome.W1_W1;
            case "1-X", "1/X", "1X" -> HalfTimeFullTimeBet.Outcome.W1_X;
            case "1-2", "1/2", "12" -> HalfTimeFullTimeBet.Outcome.W1_W2;
            case "X-1", "X/1", "X1" -> HalfTimeFullTimeBet.Outcome.X_W1;
            case "X-X", "X/X", "XX" -> HalfTimeFullTimeBet.Outcome.X_X;
            case "X-2", "X/2", "X2" -> HalfTimeFullTimeBet.Outcome.X_W2;
            case "2-1", "2/1", "21" -> HalfTimeFullTimeBet.Outcome.W2_W1;
            case "2-X", "2/X", "2X" -> HalfTimeFullTimeBet.Outcome.W2_X;
            case "2-2", "2/2", "22" -> HalfTimeFullTimeBet.Outcome.W2_W2;
            default -> null;
        };
    }
}

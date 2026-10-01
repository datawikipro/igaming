package pro.datawiki.igaming.source.smarkets.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;
import java.util.regex.Pattern;

@Component
@Order(50)
public class SmarketsHalfTimeFullTimeHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        if (mName.contains("corner") || mName.contains("card") || mName.contains("booking")) {
            return false;
        }

        return mName.contains("half time / full time") ||
               mName.contains("half-time / full-time") ||
               mName.contains("halftime/fulltime") ||
               mName.contains("half time/full time") ||
               mName.contains("ht/ft") ||
               mName.contains("ht / ft") ||
               mName.contains("double result") ||
               mtName.contains("half_time_full_time") ||
               mtName.contains("ht_ft");
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        String team1 = context.getTeam1();
        String team2 = context.getTeam2();

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            HalfTimeFullTimeBet.Outcome outcome = parseHtFtOutcome(cName, team1, team2);

            if (outcome != null) {
                HalfTimeFullTimeBet betType = new HalfTimeFullTimeBet(outcome);
                addOddItem(items, "half_time_full_time", contract.getId(), cName, backOdds, betType);
            }
        }
    }

    private HalfTimeFullTimeBet.Outcome parseHtFtOutcome(String text, String team1, String team2) {
        if (text == null || text.isBlank()) return null;
        String upper = text.toUpperCase().trim();

        String delimiter = null;
        if (upper.contains("/")) {
            delimiter = "/";
        } else if (upper.contains(" - ")) {
            delimiter = " - ";
        } else if (upper.contains("-")) {
            delimiter = "-";
        }

        if (delimiter != null) {
            String[] parts = upper.split(Pattern.quote(delimiter), 2);
            int r1 = parseHalfOutcome(parts[0].trim(), team1, team2);
            int r2 = parseHalfOutcome(parts[1].trim(), team1, team2);
            if (r1 >= 0 && r2 >= 0) {
                if (r1 == 1 && r2 == 1) return HalfTimeFullTimeBet.Outcome.W1_W1;
                if (r1 == 1 && r2 == 0) return HalfTimeFullTimeBet.Outcome.W1_X;
                if (r1 == 1 && r2 == 2) return HalfTimeFullTimeBet.Outcome.W1_W2;
                if (r1 == 0 && r2 == 1) return HalfTimeFullTimeBet.Outcome.X_W1;
                if (r1 == 0 && r2 == 0) return HalfTimeFullTimeBet.Outcome.X_X;
                if (r1 == 0 && r2 == 2) return HalfTimeFullTimeBet.Outcome.X_W2;
                if (r1 == 2 && r2 == 1) return HalfTimeFullTimeBet.Outcome.W2_W1;
                if (r1 == 2 && r2 == 0) return HalfTimeFullTimeBet.Outcome.W2_X;
                if (r1 == 2 && r2 == 2) return HalfTimeFullTimeBet.Outcome.W2_W2;
            }
        }

        return null;
    }

    private int parseHalfOutcome(String part, String team1, String team2) {
        if (part == null || part.isBlank()) return -1;
        String p = part.trim().toUpperCase();

        if ("1".equals(p) || "HOME".equals(p) || p.startsWith("HOME") || "TEAM 1".equals(p) || "TEAM1".equals(p)) {
            return 1;
        }
        if ("X".equals(p) || "DRAW".equals(p) || "TIE".equals(p) || "D".equals(p)) {
            return 0;
        }
        if ("2".equals(p) || "AWAY".equals(p) || p.startsWith("AWAY") || "TEAM 2".equals(p) || "TEAM2".equals(p)) {
            return 2;
        }

        if (isMatch(p, team1)) {
            return 1;
        }
        if (isMatch(p, team2)) {
            return 2;
        }

        return -1;
    }
}

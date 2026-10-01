package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@Order(80)
public class WplayPeriodHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        if (isStats(marketName)) return false;
        if (context != null && isEsports(context.getSportType())) return false;

        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("descanso / final")
                || m.contains("descanso/final")
                || m.contains("medio tiempo / final")
                || m.contains("medio tiempo/final")
                || m.contains("half time / full time")
                || m.contains("halftime/fulltime")
                || m.contains("ht/ft");
    }

    @Override
    public void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null) return;

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getEffectiveOdds() <= 1.0) continue;

            String outName = outcome.getName() != null ? outcome.getName().trim() : "";
            HalfTimeFullTimeBet.Outcome htft = parseOutcome(outName, context);
            if (htft != null) {
                addOddItem(items, outcome, market.getName(), new HalfTimeFullTimeBet(htft));
            }
        }
    }

    private HalfTimeFullTimeBet.Outcome parseOutcome(String name, WplayMarketContext context) {
        String upper = name.toUpperCase(Locale.ROOT).trim();
        String delimiter = null;
        if (upper.contains("/")) delimiter = "/";
        else if (upper.contains(" - ")) delimiter = " - ";
        else if (upper.contains("-")) delimiter = "-";

        if (delimiter != null) {
            String[] parts = upper.split(java.util.regex.Pattern.quote(delimiter), 2);
            int r1 = parseHalfOutcome(parts[0].trim(), context);
            int r2 = parseHalfOutcome(parts[1].trim(), context);
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

    private int parseHalfOutcome(String part, WplayMarketContext context) {
        if ("1".equals(part) || "LOCAL".equals(part) || part.contains("LOCAL") || "HOME".equals(part)) return 1;
        if ("X".equals(part) || "EMPATE".equals(part) || "DRAW".equals(part)) return 0;
        if ("2".equals(part) || "VISITANTE".equals(part) || part.contains("VISITANTE") || "AWAY".equals(part)) return 2;

        if (context != null) {
            if (context.getHomeTeam() != null && part.contains(context.getHomeTeam().toUpperCase(Locale.ROOT))) return 1;
            if (context.getAwayTeam() != null && part.contains(context.getAwayTeam().toUpperCase(Locale.ROOT))) return 2;
        }
        return -1;
    }
}

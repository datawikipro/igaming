package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@Order(70)
public class WplayDrawNoBetHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        if (isStats(marketName)) return false;
        if (context != null && isEsports(context.getSportType())) return false;

        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("empate no acción")
                || m.contains("empate no accion")
                || m.contains("apuesta sin empate")
                || m.contains("empate, apuesta no válida")
                || m.contains("empate apuesta no valida")
                || m.contains("draw no bet")
                || m.contains("dnb");
    }

    @Override
    public void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null) return;

        String marketName = market.getName();
        BetScope scope = resolveScope(marketName, context != null ? context.getSportType() : null);

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getEffectiveOdds() <= 1.0) continue;

            String outName = outcome.getName() != null ? outcome.getName().trim() : "";
            String lower = outName.toLowerCase(Locale.ROOT);

            if (isTeam1(lower, context)) {
                addOddItem(items, outcome, marketName, new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.NONE));
            } else if (isTeam2(lower, context)) {
                addOddItem(items, outcome, marketName, new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.NONE));
            }
        }
    }

    private boolean isTeam1(String lower, WplayMarketContext context) {
        if (lower.equals("1") || lower.startsWith("1 ") || lower.contains("local") || lower.contains("equipo 1")) {
            return true;
        }
        if (context != null && context.getHomeTeam() != null) {
            String home = context.getHomeTeam().toLowerCase(Locale.ROOT);
            return lower.contains(home) || home.contains(lower);
        }
        return false;
    }

    private boolean isTeam2(String lower, WplayMarketContext context) {
        if (lower.equals("2") || lower.startsWith("2 ") || lower.contains("visitante") || lower.contains("equipo 2")) {
            return true;
        }
        if (context != null && context.getAwayTeam() != null) {
            String away = context.getAwayTeam().toLowerCase(Locale.ROOT);
            return lower.contains(away) || away.contains(lower);
        }
        return false;
    }
}

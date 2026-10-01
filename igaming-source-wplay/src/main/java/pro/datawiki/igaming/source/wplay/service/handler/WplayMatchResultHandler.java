package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@Order(100)
public class WplayMatchResultHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        if (isStats(marketName)) return false;
        if (context != null && isEsports(context.getSportType())) return false;

        String m = marketName.toLowerCase(Locale.ROOT);
        if (m.contains("total") || m.contains("más/menos") || m.contains("mas/menos")
                || m.contains("handicap") || m.contains("hándicap")
                || m.contains("doble") || m.contains("ambos") || m.contains("empate no")
                || m.contains("apuesta sin empate") || m.contains("marcador") || m.contains("exacto")) {
            return false;
        }

        return m.contains("1x2") || m.contains("resultado") || m.contains("ganador")
                || m.contains("match result") || m.contains("moneyline") || m.contains("línea de dinero")
                || m.contains("encuentro") || m.equals("1 2") || m.equals("1 x 2");
    }

    @Override
    public void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null) return;

        BetScope scope = resolveScope(market.getName(), context != null ? context.getSportType() : null);

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getEffectiveOdds() <= 1.0) continue;

            String outName = outcome.getName() != null ? outcome.getName().trim() : "";
            String lower = outName.toLowerCase(Locale.ROOT);

            if (lower.contains("empate") || lower.equals("x") || lower.contains("draw")) {
                addOddItem(items, outcome, market.getName(), new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.NONE));
            } else if (isTeam1(lower, context)) {
                addOddItem(items, outcome, market.getName(), new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.NONE));
            } else if (isTeam2(lower, context)) {
                addOddItem(items, outcome, market.getName(), new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.NONE));
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

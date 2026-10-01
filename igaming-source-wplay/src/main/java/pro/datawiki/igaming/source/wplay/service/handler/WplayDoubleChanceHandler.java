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
@Order(50)
public class WplayDoubleChanceHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        if (isStats(marketName)) return false;
        if (context != null && isEsports(context.getSportType())) return false;

        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("doble oportunidad") || m.contains("doble chance") || m.contains("double chance");
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

            if (lower.equals("1x") || lower.contains("1x") || lower.contains("1 o x") || lower.contains("1/x")
                    || (lower.contains("empate") && isTeam1(lower, context))) {
                addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.NONE));
            } else if (lower.equals("12") || lower.contains("12") || lower.contains("1 o 2") || lower.contains("1/2")
                    || (isTeam1(lower, context) && isTeam2(lower, context))) {
                addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.NONE));
            } else if (lower.equals("x2") || lower.contains("x2") || lower.contains("x o 2") || lower.contains("x/2")
                    || (lower.contains("empate") && isTeam2(lower, context))) {
                addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.NONE));
            }
        }
    }

    private boolean isTeam1(String lower, WplayMarketContext context) {
        if (lower.contains("local") || lower.contains("equipo 1")) return true;
        if (context != null && context.getHomeTeam() != null) {
            String home = context.getHomeTeam().toLowerCase(Locale.ROOT);
            return lower.contains(home);
        }
        return false;
    }

    private boolean isTeam2(String lower, WplayMarketContext context) {
        if (lower.contains("visitante") || lower.contains("equipo 2")) return true;
        if (context != null && context.getAwayTeam() != null) {
            String away = context.getAwayTeam().toLowerCase(Locale.ROOT);
            return lower.contains(away);
        }
        return false;
    }
}

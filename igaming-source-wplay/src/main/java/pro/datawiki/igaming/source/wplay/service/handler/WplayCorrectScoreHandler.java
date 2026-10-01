package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(75)
public class WplayCorrectScoreHandler extends AbstractWplayMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*[-:]\\s*(\\d+)");

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        if (isStats(marketName)) return false;
        if (context != null && isEsports(context.getSportType())) return false;

        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("marcador correcto")
                || m.contains("resultado exacto")
                || m.contains("marcador exacto")
                || m.contains("correct score")
                || m.contains("exact score");
    }

    @Override
    public void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null) return;

        String marketName = market.getName();
        BetScope scope = resolveScope(marketName, context != null ? context.getSportType() : null);

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getEffectiveOdds() <= 1.0) continue;

            String outName = outcome.getName() != null ? outcome.getName().trim() : "";
            CorrectScoreBet betType = resolveScore(outName, scope);
            if (betType != null) {
                addOddItem(items, outcome, marketName, betType);
            }
        }
    }

    private CorrectScoreBet resolveScore(String outcomeName, BetScope scope) {
        if (outcomeName == null || outcomeName.isBlank()) return null;
        String u = outcomeName.toUpperCase(Locale.ROOT).trim();

        if (u.contains("OTRO") || u.contains("OTHER") || u.contains("AOS")) {
            return new CorrectScoreBet(scope, -1, -1, true);
        }

        Matcher matcher = SCORE_PATTERN.matcher(outcomeName);
        if (matcher.find()) {
            try {
                int s1 = Integer.parseInt(matcher.group(1));
                int s2 = Integer.parseInt(matcher.group(2));
                return new CorrectScoreBet(scope, s1, s2, false);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}

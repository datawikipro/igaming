package pro.datawiki.igaming.source.sport888.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiBetOffer;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEvent;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiOutcome;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(60)
public class Sport888CorrectScoreHandler extends AbstractSport888MarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*[-:]\\s*(\\d+)");

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String mUpper = marketName.toUpperCase();
        return (mUpper.contains("CORRECT SCORE") || mUpper.contains("EXACT SCORE")) && !isStats(marketName);
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = "correct_score" + scopeSuffix;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String label = outcome.getLabel() != null ? outcome.getLabel() : "";
            Matcher matcher = SCORE_PATTERN.matcher(label);
            if (matcher.find()) {
                try {
                    int s1 = Integer.parseInt(matcher.group(1));
                    int s2 = Integer.parseInt(matcher.group(2));
                    addOddItem(items, outcome, groupName, s1 + "-" + s2, decimal,
                            new CorrectScoreBet(scope, s1, s2, false));
                } catch (NumberFormatException ignored) {}
            } else if (label.toUpperCase().contains("OTHER") || label.toUpperCase().contains("ANY OTHER")) {
                addOddItem(items, outcome, groupName, "ANY_OTHER", decimal,
                        new CorrectScoreBet(scope, -1, -1, true));
            }
        }
    }
}

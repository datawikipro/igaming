package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(30)
public class BetanoCorrectScoreHandler extends AbstractBetanoMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)[\\s:-]+(\\d+)");

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String mUpper = marketName.toUpperCase();
        return mUpper.contains("CORRECT SCORE") || mUpper.contains("RESULTADO CORRETO") || mUpper.contains("EXACT SCORE");
    }

    @Override
    public void handle(MatchCache match, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = "correct_score" + scopeSuffix;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String label = outcome.getLabel();
            if (label == null) continue;

            Matcher matcher = SCORE_PATTERN.matcher(label);
            if (matcher.find()) {
                int score1 = Integer.parseInt(matcher.group(1));
                int score2 = Integer.parseInt(matcher.group(2));
                addOddItem(items, outcome, groupName, score1 + ":" + score2, decimal,
                        new CorrectScoreBet(scope, score1, score2, StatType.MATCH));
            }
        }
    }
}

package pro.datawiki.igaming.source.bcgame.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for Correct Score markets:
 * - Full match correct score (e.g. 1-0, 2-1, 0-0, Any Other Score)
 * - Period/Half correct score (1st Half Correct Score, 2nd Half Correct Score)
 */
@Slf4j
@Component
@Order(70)
public class BcgameCorrectScoreHandler extends AbstractBcgameMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(?i)(?:score\\s*)?(\\d+)\\s*[-:]\\s*(\\d+)");

    @Override
    public boolean supports(String marketName, BcgameMarketContext context) {
        if (marketName == null || marketName.isBlank()) {
            return false;
        }
        String mLower = marketName.trim().toLowerCase();

        // 1. Stats markets are handled by @Order(10) BcgameStatsMarketHandler
        if (isStats(marketName)) {
            return false;
        }

        // 2. Esports sub-markets handled by @Order(20) BcgameEsportsMarketHandler
        if (mLower.contains("map ") || mLower.contains("карта ") || mLower.contains("map1") || mLower.contains("map2")
                || mLower.contains("round ") || mLower.contains("раунд ")
                || mLower.contains("kill") || mLower.contains("first blood")) {
            return false;
        }

        // 3. Exclude non-correct-score markets (totals, handicaps, btts, etc.)
        if (mLower.contains("total") || mLower.contains("тотал")
                || mLower.contains("handicap") || mLower.contains("фора") || mLower.contains("spread")
                || mLower.contains("both teams") || mLower.contains("btts") || mLower.contains("обе забьют")
                || mLower.contains("1x2") || mLower.contains("double chance") || mLower.contains("двойной шанс")
                || mLower.contains("draw no bet") || mLower.contains("moneyline")
                || mLower.contains("margin") || mLower.contains("разница")) {
            return false;
        }

        // 4. Match Correct Score patterns
        return mLower.contains("correct score")
                || mLower.contains("exact score")
                || mLower.contains("correct_score")
                || mLower.contains("exact_score")
                || mLower.contains("точный счет")
                || mLower.contains("точный счёт")
                || mLower.equals("cs") || mLower.startsWith("cs ") || mLower.endsWith(" cs");
    }

    @Override
    public void handle(BcgameMarketDto market, BcgameMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
            return;
        }

        String marketName = market.getName() != null ? market.getName().trim() : "";
        SportType sportType = context != null ? context.getSportType() : null;
        BetScope scope = resolveScope(marketName, sportType);
        String groupName = !marketName.isBlank() ? marketName : "Correct Score";

        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getName() == null) continue;

            String name = outcome.getName().trim();
            CorrectScoreBet betType = resolveCorrectScore(name, scope);
            if (betType != null) {
                addOddItem(items, outcome, groupName, betType);
            }
        }
    }

    private CorrectScoreBet resolveCorrectScore(String outcomeName, BetScope scope) {
        if (outcomeName == null || outcomeName.isBlank()) {
            return null;
        }

        if (isAnyOtherScore(outcomeName)) {
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

    private boolean isAnyOtherScore(String outcomeName) {
        if (outcomeName == null || outcomeName.isBlank()) {
            return false;
        }
        String u = outcomeName.trim().toUpperCase();
        return u.equals("AOS")
                || u.equals("OTHER")
                || u.contains("OTHER")
                || u.contains("ANY OTHER")
                || u.contains("ДРУГОЙ")
                || u.contains("ДРУГИЕ")
                || u.contains("ЛЮБОЙ ДРУГОЙ");
    }
}

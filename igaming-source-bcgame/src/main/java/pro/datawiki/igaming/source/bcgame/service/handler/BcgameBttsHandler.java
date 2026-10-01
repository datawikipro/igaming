package pro.datawiki.igaming.source.bcgame.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Handler for Both Teams to Score (BTTS) markets:
 * - Match BTTS (Yes / No)
 * - Period/Half BTTS (1st Half, 2nd Half)
 */
@Slf4j
@Component
@Order(60)
public class BcgameBttsHandler extends AbstractBcgameMarketHandler {

    private static final Pattern YES_PATTERN = Pattern.compile("(?i)^(?:yes|да|y|д)$");
    private static final Pattern NO_PATTERN = Pattern.compile("(?i)^(?:no|нет|n|н)$");

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

        // 3. Exclude combo markets (e.g. "1X2 & BTTS", "Total & BTTS", "Match Result and Both Teams To Score")
        if (mLower.contains(" & ") || mLower.contains(" and ") || mLower.contains(" + ")
                || mLower.contains(" и обе забьют") || mLower.contains(" & обе забьют")
                || mLower.contains("total") || mLower.contains("тотал")
                || mLower.contains("handicap") || mLower.contains("фора")
                || mLower.contains("1x2") || mLower.contains("result")
                || mLower.contains("double chance") || mLower.contains("двойной шанс")
                || mLower.contains("correct score") || mLower.contains("точный счет")) {
            return false;
        }

        // 4. Match BTTS patterns
        return mLower.contains("both teams to score")
                || mLower.contains("both teams score")
                || mLower.contains("both to score")
                || mLower.contains("btts")
                || mLower.contains("обе забьют")
                || mLower.contains("обе команды забьют")
                || mLower.contains("забьют обе команды");
    }

    @Override
    public void handle(BcgameMarketDto market, BcgameMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
            return;
        }

        String marketName = market.getName() != null ? market.getName().trim() : "";
        SportType sportType = context != null ? context.getSportType() : null;
        BetScope scope = resolveScope(marketName, sportType);
        String groupName = !marketName.isBlank() ? marketName : "Both Teams to Score";

        List<BcgameOutcomeDto> outcomes = market.getOutcomes();
        for (int i = 0; i < outcomes.size(); i++) {
            BcgameOutcomeDto outcome = outcomes.get(i);
            if (outcome == null) continue;

            BinaryMarketBet.Outcome outcomeEnum = resolveOutcome(outcome.getName());
            if (outcomeEnum == null && outcomes.size() == 2) {
                // Positional fallback for 2-way binary outcomes (Yes / No)
                outcomeEnum = (i == 0) ? BinaryMarketBet.Outcome.YES : BinaryMarketBet.Outcome.NO;
            }

            if (outcomeEnum != null) {
                BinaryMarketBet betType = new BinaryMarketBet(
                        scope,
                        BetSubject.MATCH,
                        BinaryMarketBet.MarketType.BTTS,
                        outcomeEnum,
                        StatType.MATCH
                );
                addOddItem(items, outcome, groupName, betType);
            }
        }
    }

    private BinaryMarketBet.Outcome resolveOutcome(String outcomeName) {
        if (outcomeName == null || outcomeName.isBlank()) {
            return null;
        }
        String trimmed = outcomeName.trim();
        String u = trimmed.toUpperCase();

        if (YES_PATTERN.matcher(u).matches()) {
            return BinaryMarketBet.Outcome.YES;
        }
        if (NO_PATTERN.matcher(u).matches()) {
            return BinaryMarketBet.Outcome.NO;
        }

        if (u.endsWith(" YES") || u.endsWith("-YES") || u.endsWith(": YES") || u.endsWith(":YES")
                || u.startsWith("YES ") || u.startsWith("YES-")
                || u.endsWith(" ДА") || u.endsWith("-ДА") || u.endsWith(": ДА") || u.endsWith(":ДА")
                || u.startsWith("ДА ") || u.startsWith("ДА-")) {
            return BinaryMarketBet.Outcome.YES;
        }

        if (u.endsWith(" NO") || u.endsWith("-NO") || u.endsWith(": NO") || u.endsWith(":NO")
                || u.startsWith("NO ") || u.startsWith("NO-")
                || u.endsWith(" НЕТ") || u.endsWith("-НЕТ") || u.endsWith(": НЕТ") || u.endsWith(":НЕТ")
                || u.startsWith("НЕТ ") || u.startsWith("НЕТ-")) {
            return BinaryMarketBet.Outcome.NO;
        }

        if (u.matches(".*\\bYES\\b.*") || u.matches(".*\\bДА\\b.*")) {
            return BinaryMarketBet.Outcome.YES;
        }
        if (u.matches(".*\\bNO\\b.*") || u.matches(".*\\bНЕТ\\b.*")) {
            return BinaryMarketBet.Outcome.NO;
        }

        return null;
    }
}

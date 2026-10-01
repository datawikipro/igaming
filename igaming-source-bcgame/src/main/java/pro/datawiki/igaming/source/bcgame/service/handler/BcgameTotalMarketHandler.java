package pro.datawiki.igaming.source.bcgame.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for Over/Under and Total markets (Full Match, Halves, Quarters, Periods,
 * and Individual Team Totals).
 */
@Slf4j
@Component
@Order(40)
public class BcgameTotalMarketHandler extends AbstractBcgameMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern DIR_PARAM_PATTERN = Pattern.compile("(?i)(?:over|under|больше|меньше|тотал|total|tb|tm|тб|тм|exact|ровно|[ouбм])\\s*[:(]?\\s*([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("(?:^|[^0-9])(\\d+\\.\\d+)(?:$|[^0-9])");
    private static final Pattern END_NUMBER_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)\\s*$");
    private static final Pattern TOTAL_KEYWORD_PARAM = Pattern.compile("(?i)(?:total|тотал|over/under|o/u)\\s*(?:goals|points|games|голов|очков)?\\s*([+-]?\\d+(?:\\.\\d+)?)");

    private static final Pattern TEAM1_KEYWORDS = Pattern.compile("(?i)(team\\s*1|team1|home\\s*team|home|1-я\\s*команда|1-й\\s*команд|команда\\s*1|первая\\s*команда|первой\\s*команд|ит\\s*1|ит1|индивидуальный\\s*тотал\\s*(?:команды\\s*)?1)");
    private static final Pattern TEAM2_KEYWORDS = Pattern.compile("(?i)(team\\s*2|team2|away\\s*team|away|2-я\\s*команда|2-й\\s*команд|команда\\s*2|вторая\\s*команда|второй\\s*команд|ит\\s*2|ит2|индивидуальный\\s*тотал\\s*(?:команды\\s*)?2)");
    private static final Pattern PERIOD_PREFIX_STRIP = Pattern.compile("(?i)(1st half|2nd half|1st period|2nd period|3rd period|4th period|5th period|1st quarter|2nd quarter|3rd quarter|4th quarter|1st set|2nd set|3rd set|4th set|5th set|half 1|half 2|ht1|ht2|1 half|2 half|период 1|период 2|тайм 1|тайм 2)");

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
                || mLower.contains("kill") || mLower.contains("first blood")
                || mLower.contains("tower") || mLower.contains("roshan") || mLower.contains("baron")
                || mLower.contains("total maps") || mLower.contains("тотал карт")
                || mLower.contains("total rounds") || mLower.contains("тотал раундов")
                || mLower.contains("total kills") || mLower.contains("тотал убийств")) {
            return false;
        }

        // 3. Exclude non-total markets
        if (mLower.contains("handicap") || mLower.contains("фора") || mLower.contains("spread")
                || mLower.contains("both teams") || mLower.contains("btts") || mLower.contains("обе забьют")
                || mLower.contains("correct score") || mLower.contains("точный счет")
                || mLower.contains("1x2") || mLower.contains("double chance") || mLower.contains("двойной шанс")
                || mLower.contains("draw no bet") || mLower.contains("moneyline")
                || mLower.contains("winner") || mLower.contains("победитель")) {
            return false;
        }

        // 4. Accept general and individual total markets
        return mLower.contains("total") || mLower.contains("тотал")
                || mLower.contains("over/under") || mLower.contains("o/u") || mLower.contains("over / under")
                || mLower.contains("больше/меньше") || mLower.contains("больше / меньше")
                || mLower.contains("индивидуальный тотал") || mLower.contains("инд тотал")
                || mLower.contains("ит1") || mLower.contains("ит2")
                || mLower.startsWith("o/u ") || mLower.startsWith("o/u:");
    }

    @Override
    public void handle(BcgameMarketDto market, BcgameMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
            return;
        }

        String marketName = market.getName() != null ? market.getName().trim() : "";
        SportType sportType = context != null ? context.getSportType() : null;
        BetScope scope = resolveScope(marketName, sportType);
        String groupName = !marketName.isBlank() ? marketName : "Total";

        BetSubject defaultSubject = resolveSubject(marketName, context);

        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null) continue;

            String outcomeName = outcome.getName();
            TotalBet.Direction direction = resolveDirection(outcomeName);
            if (direction == null) {
                continue;
            }

            Double param = resolveTotalParam(outcome, marketName);
            if (param == null || param < 0) {
                continue;
            }

            BetSubject outcomeSubject = resolveSubject(outcomeName, context);
            BetSubject subject = (outcomeSubject != BetSubject.MATCH) ? outcomeSubject : defaultSubject;

            boolean isAsian = isAsianLine(marketName, param);

            TotalBet betType = new TotalBet(scope, subject, direction, param, isAsian, StatType.MATCH);
            addOddItem(items, outcome, groupName, betType);
        }
    }

    private TotalBet.Direction resolveDirection(String outcomeName) {
        if (outcomeName == null || outcomeName.isBlank()) return null;
        String u = outcomeName.trim().toUpperCase();

        // Exact
        if (u.equals("EXACT") || u.equals("РОВНО") || u.startsWith("EXACT ") || u.startsWith("РОВНО ")
                || u.contains("EXACT") || u.contains("РОВНО")) {
            return TotalBet.Direction.EXACT;
        }

        // Over
        if (u.equals("OVER") || u.equals("O") || u.equals("БОЛЬШЕ") || u.equals("Б")
                || u.startsWith("OVER") || u.startsWith("БОЛЬШЕ")
                || u.startsWith("O ") || u.startsWith("O(")
                || u.startsWith("Б ") || u.startsWith("Б(")
                || u.startsWith("ТБ") || u.contains("OVER") || u.contains("БОЛЬШЕ")
                || u.contains(" ТБ") || u.contains("ТБ ") || u.contains("(ТБ)")
                || u.contains("OVER ") || u.contains(" OVER")) {
            return TotalBet.Direction.OVER;
        }

        // Under
        if (u.equals("UNDER") || u.equals("U") || u.equals("МЕНЬШЕ") || u.equals("М")
                || u.startsWith("UNDER") || u.startsWith("МЕНЬШЕ")
                || u.startsWith("U ") || u.startsWith("U(")
                || u.startsWith("М ") || u.startsWith("М(")
                || u.startsWith("ТМ") || u.contains("UNDER") || u.contains("МЕНЬШЕ")
                || u.contains(" ТМ") || u.contains("ТМ ") || u.contains("(ТМ)")
                || u.contains("UNDER ") || u.contains(" UNDER")) {
            return TotalBet.Direction.UNDER;
        }

        return null;
    }

    private Double resolveTotalParam(BcgameOutcomeDto outcome, String marketName) {
        if (outcome != null && outcome.getEffectiveParam() != null && outcome.getEffectiveParam() >= 0) {
            return outcome.getEffectiveParam();
        }

        if (outcome != null && outcome.getName() != null && !outcome.getName().isBlank()) {
            String name = outcome.getName();

            // 1. Parenthesized number: Over (2.5) -> 2.5
            Matcher mParen = PAREN_PARAM_PATTERN.matcher(name);
            if (mParen.find()) {
                try {
                    return Double.parseDouble(mParen.group(1));
                } catch (NumberFormatException ignored) {}
            }

            // 2. Number following keyword/direction: Over 2.5, Team 1 Over 1.5 -> 1.5
            Matcher mDir = DIR_PARAM_PATTERN.matcher(name);
            if (mDir.find()) {
                try {
                    return Double.parseDouble(mDir.group(1));
                } catch (NumberFormatException ignored) {}
            }

            // 3. Decimal number: 2.5
            Matcher mDec = DECIMAL_PATTERN.matcher(name);
            if (mDec.find()) {
                try {
                    return Double.parseDouble(mDec.group(1));
                } catch (NumberFormatException ignored) {}
            }

            // 4. Number at end of outcome string
            Matcher mEnd = END_NUMBER_PATTERN.matcher(name);
            if (mEnd.find()) {
                try {
                    return Double.parseDouble(mEnd.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }

        // Fallback to market name
        if (marketName != null && !marketName.isBlank()) {
            Matcher mParen = PAREN_PARAM_PATTERN.matcher(marketName);
            if (mParen.find()) {
                try {
                    return Double.parseDouble(mParen.group(1));
                } catch (NumberFormatException ignored) {}
            }

            Matcher mDec = DECIMAL_PATTERN.matcher(marketName);
            if (mDec.find()) {
                try {
                    return Double.parseDouble(mDec.group(1));
                } catch (NumberFormatException ignored) {}
            }

            Matcher mTotal = TOTAL_KEYWORD_PARAM.matcher(marketName);
            if (mTotal.find()) {
                try {
                    return Double.parseDouble(mTotal.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }

        return null;
    }

    private BetSubject resolveSubject(String text, BcgameMarketContext context) {
        if (text == null || text.isBlank()) {
            return BetSubject.MATCH;
        }

        // Strip period/half mentions to prevent confusing "1st Half" with "Team 1"
        String cleaned = PERIOD_PREFIX_STRIP.matcher(text).replaceAll(" ");

        boolean isT1 = TEAM1_KEYWORDS.matcher(cleaned).find();
        boolean isT2 = TEAM2_KEYWORDS.matcher(cleaned).find();

        String team1 = context != null ? context.getHomeTeam() : null;
        String team2 = context != null ? context.getAwayTeam() : null;

        if (containsTeamName(cleaned, team1)) {
            isT1 = true;
        }
        if (containsTeamName(cleaned, team2)) {
            isT2 = true;
        }

        if (isT1 && !isT2) return BetSubject.TEAM1;
        if (isT2 && !isT1) return BetSubject.TEAM2;

        return BetSubject.MATCH;
    }

    private boolean containsTeamName(String text, String teamName) {
        if (text == null || teamName == null || teamName.trim().length() < 3) {
            return false;
        }
        String p = Pattern.quote(teamName.trim().toLowerCase());
        Pattern pattern = Pattern.compile("(?i)(?:^|[^a-zA-Z0-9а-яА-ЯёЁ])" + p + "(?:$|[^a-zA-Z0-9а-яА-ЯёЁ])");
        return pattern.matcher(text).find();
    }

    private boolean isAsianLine(String marketName, Double param) {
        if (isQuarterAsian(param)) {
            return true;
        }
        if (marketName != null) {
            String mLower = marketName.toLowerCase();
            return mLower.contains("asian") || mLower.contains("азиатск");
        }
        return false;
    }

    private boolean isQuarterAsian(Double line) {
        if (line == null) return false;
        double rem = Math.abs(line) % 1.0;
        return Math.abs(rem - 0.25) < 0.001 || Math.abs(rem - 0.75) < 0.001;
    }
}

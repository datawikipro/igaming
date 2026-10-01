package pro.datawiki.igaming.source.bcgame.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for sports statistics markets:
 * - Corners (Total, Handicap, 1X2, Double Chance)
 * - Yellow Cards & Cards (Total, Handicap, 1X2, Double Chance)
 * - Fouls (Total, Handicap, 1X2)
 * - Offsides (Total, Handicap, 1X2)
 * - Shots on Target (Total, Handicap, 1X2)
 * Full match and period scopes (halves, quarters, periods).
 */
@Slf4j
@Component
@Order(10)
public class BcgameStatsMarketHandler extends AbstractBcgameMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern SCORE_HDP_PATTERN = Pattern.compile("\\((\\d+):(\\d+)\\)");
    private static final Pattern SIGNED_NUM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?i)^(?:handicap\\s*[12]|фора\\s*[12]|spread\\s*[12]|h[12]|ф[12]|team\\s*[12]|[12])\\b\\s*");
    private static final Pattern PURE_SELECTION_PATTERN = Pattern.compile("(?i)^(?:handicap\\s*[12]|фора\\s*[12]|spread\\s*[12]|h[12]|ф[12]|team\\s*[12]|[12]|home|away|draw|tie|ничья|x|х|п1|п2|w1|w2|победа\\s*[12])$");

    private static final Pattern DIR_PARAM_PATTERN = Pattern.compile("(?i)(?:over|under|больше|меньше|тотал|total|tb|tm|тб|тм|exact|ровно|[ouбм])\\s*[:(]?\\s*([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("(?:^|[^0-9])(\\d+\\.\\d+)(?:$|[^0-9])");
    private static final Pattern END_NUMBER_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)\\s*$");
    private static final Pattern TOTAL_KEYWORD_PARAM = Pattern.compile("(?i)(?:total|тотал|over/under|o/u)\\s*(?:corners|cards|yellow\\s*cards|fouls|offsides|shots|голов|очков|угловых|карточек|желтых\\s*карточек|фолов|офсайдов|ударов)?\\s*([+-]?\\d+(?:\\.\\d+)?)");

    private static final Pattern TEAM1_KEYWORDS = Pattern.compile("(?i)(team\\s*1|team1|home\\s*team|home|1-я\\s*команда|1-й\\s*команд|команда\\s*1|первая\\s*команда|первой\\s*команд|ит\\s*1|ит1|индивидуальный\\s*тотал\\s*(?:команды\\s*)?1)");
    private static final Pattern TEAM2_KEYWORDS = Pattern.compile("(?i)(team\\s*2|team2|away\\s*team|away|2-я\\s*команда|2-й\\s*команд|команда\\s*2|вторая\\s*команда|второй\\s*команд|ит\\s*2|ит2|индивидуальный\\s*тотал\\s*(?:команды\\s*)?2)");
    private static final Pattern PERIOD_PREFIX_STRIP = Pattern.compile("(?i)(1st half|2nd half|1st period|2nd period|3rd period|4th period|half 1|half 2|ht1|ht2|1 half|2 half|период 1|период 2|тайм 1|тайм 2)");

    @Override
    public boolean supports(String marketName, BcgameMarketContext context) {
        if (marketName == null || marketName.isBlank()) {
            return false;
        }
        String mLower = marketName.trim().toLowerCase();
        // Exclude esports-specific identifiers
        if (mLower.contains("map ") || mLower.contains("map1") || mLower.contains("map2")
                || mLower.contains("roshan") || mLower.contains("tower") || mLower.contains("baron")) {
            return false;
        }
        return isStats(marketName);
    }

    @Override
    public void handle(BcgameMarketDto market, BcgameMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
            return;
        }

        String marketName = market.getName() != null ? market.getName().trim() : "";
        String mLower = marketName.toLowerCase();
        SportType sportType = context != null ? context.getSportType() : null;
        BetScope scope = resolveScope(marketName, sportType);
        StatType statType = resolveStatType(marketName);
        String groupName = !marketName.isBlank() ? marketName : statType.name();

        String team1 = context != null ? context.getHomeTeam() : null;
        String team2 = context != null ? context.getAwayTeam() : null;

        // 1. Double Chance
        if (isDoubleChanceMarket(mLower, market)) {
            handleDoubleChance(market, scope, groupName, team1, team2, statType, items);
            return;
        }

        // 2. Handicap / Spread
        if (isHandicapMarket(mLower, market)) {
            handleHandicap(market, scope, groupName, team1, team2, statType, items);
            return;
        }

        // 3. Total / Over-Under
        if (isTotalMarket(mLower, market)) {
            handleTotal(market, scope, groupName, team1, team2, statType, context, items);
            return;
        }

        // 4. 1X2 / Match Result / Most / Winner / Draw No Bet
        handleResult(market, scope, groupName, team1, team2, statType, mLower, items);
    }

    public StatType resolveStatType(String marketName) {
        if (marketName == null) return StatType.CORNERS;
        String m = marketName.toLowerCase();

        // 1. Yellow cards vs Cards
        if (m.contains("yellow") || m.contains("желт") || m.contains(" жк") || m.startsWith("жк ") || m.equals("жк")) {
            return StatType.YELLOW_CARDS;
        }
        if (m.contains("card") || m.contains("booking") || m.contains("карточ")) {
            return StatType.CARDS;
        }

        // 2. Corners
        if (m.contains("corner") || m.contains("углов")) {
            return StatType.CORNERS;
        }

        // 3. Fouls
        if (m.contains("foul") || m.contains("фол")) {
            return StatType.FOULS;
        }

        // 4. Offsides
        if (m.contains("offside") || m.contains("офсайд")) {
            return StatType.OFFSIDES;
        }

        // 5. Shots on Target
        if (m.contains("shot on goal") || m.contains("shots on goal")) {
            return StatType.SHOTS_ON_GOAL;
        }
        if (m.contains("shot") || m.contains("sot") || m.contains("в створ") || m.contains("удар")) {
            return StatType.SHOTS_ON_TARGET;
        }

        return StatType.CORNERS;
    }

    private boolean isDoubleChanceMarket(String mLower, BcgameMarketDto market) {
        if (mLower.contains("double chance") || mLower.contains("двойной шанс")) {
            return true;
        }
        return market.getOutcomes().stream().anyMatch(this::isDoubleChanceOutcome);
    }

    private void handleDoubleChance(BcgameMarketDto market, BetScope scope, String groupName,
                                    String team1, String team2, StatType statType, List<OddItem> items) {
        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getName() == null) continue;
            String name = outcome.getName();

            BetType betType = null;
            if (is1X(name, team1)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, statType);
            } else if (is12(name, team1, team2)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, statType);
            } else if (isX2(name, team2)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, statType);
            }

            if (betType != null) {
                addOddItem(items, outcome, groupName, betType);
            }
        }
    }

    private boolean isHandicapMarket(String mLower, BcgameMarketDto market) {
        if (mLower.contains("handicap") || mLower.contains("фора")
                || mLower.contains("spread") || mLower.contains("hcap")) {
            return true;
        }
        if (!mLower.contains("total") && !mLower.contains("тотал")
                && !mLower.contains("over") && !mLower.contains("under")
                && !mLower.contains("1x2") && !mLower.contains("most")) {
            return market.getOutcomes().stream().anyMatch(o -> o != null && o.getName() != null
                    && o.getName().contains("(") && (o.getName().contains("+") || o.getName().contains("-")));
        }
        return false;
    }

    private void handleHandicap(BcgameMarketDto market, BetScope scope, String groupName,
                                String team1, String team2, StatType statType, List<OddItem> items) {
        List<BcgameOutcomeDto> outcomes = market.getOutcomes();
        boolean hasDraw = outcomes.stream().anyMatch(this::isDrawOutcome);
        String mLower = market.getName() != null ? market.getName().toLowerCase() : "";
        boolean is3Way = hasDraw || mLower.contains("3-way") || mLower.contains("3 way")
                || mLower.contains("european") || mLower.contains("европейск");

        Double marketParam = extractSignedHandicap(market.getName(), team1, team2);

        for (int i = 0; i < outcomes.size(); i++) {
            BcgameOutcomeDto outcome = outcomes.get(i);
            if (outcome == null) continue;

            HandicapBet.Outcome outcomeType = resolveHandicapOutcome(outcome, i, outcomes.size(), team1, team2);
            if (outcomeType == null) continue;

            Double param = resolveHandicapParam(outcome, marketParam, outcomeType, team1, team2);
            if (param == null) continue;

            boolean isAsian = isAsianLine(market.getName(), param, is3Way);
            HandicapBet betType = new HandicapBet(scope, outcomeType, param, isAsian, statType);
            addOddItem(items, outcome, groupName, betType);
        }
    }

    private boolean isTotalMarket(String mLower, BcgameMarketDto market) {
        if (mLower.contains("total") || mLower.contains("тотал")
                || mLower.contains("over/under") || mLower.contains("o/u")
                || mLower.contains("over / under") || mLower.contains("over")
                || mLower.contains("under") || mLower.contains("больше") || mLower.contains("меньше")) {
            return true;
        }
        return market.getOutcomes().stream().anyMatch(o -> o != null && resolveDirection(o.getName()) != null);
    }

    private void handleTotal(BcgameMarketDto market, BetScope scope, String groupName,
                             String team1, String team2, StatType statType, BcgameMarketContext context, List<OddItem> items) {
        BetSubject defaultSubject = resolveSubject(market.getName(), context);

        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null) continue;

            String outcomeName = outcome.getName();
            TotalBet.Direction direction = resolveDirection(outcomeName);
            if (direction == null) continue;

            Double param = resolveTotalParam(outcome, market.getName());
            if (param == null || param < 0) continue;

            BetSubject outcomeSubject = resolveSubject(outcomeName, context);
            BetSubject subject = (outcomeSubject != BetSubject.MATCH) ? outcomeSubject : defaultSubject;

            boolean isAsian = isAsianLine(market.getName(), param);
            TotalBet betType = new TotalBet(scope, subject, direction, param, isAsian, statType);
            addOddItem(items, outcome, groupName, betType);
        }
    }

    private void handleResult(BcgameMarketDto market, BetScope scope, String groupName,
                              String team1, String team2, StatType statType, String mLower, List<OddItem> items) {
        List<BcgameOutcomeDto> outcomes = market.getOutcomes();
        boolean isDnb = mLower.contains("draw no bet") || mLower.contains("tie no bet")
                || mLower.contains("dnb") || mLower.contains("ничья исключена");

        if (isDnb) {
            for (int i = 0; i < outcomes.size(); i++) {
                BcgameOutcomeDto outcome = outcomes.get(i);
                if (outcome == null || outcome.getName() == null) continue;
                String name = outcome.getName();

                BetType betType = null;
                if (isTeam1(name, team1, team2)) {
                    betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType);
                } else if (isTeam2(name, team1, team2)) {
                    betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType);
                } else if (outcomes.size() == 2) {
                    betType = (i == 0)
                            ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType)
                            : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType);
                }

                if (betType != null) {
                    addOddItem(items, outcome, groupName, betType);
                }
            }
            return;
        }

        boolean hasDraw = outcomes.stream().anyMatch(this::isDrawOutcome);
        boolean is3Way = hasDraw || mLower.contains("3-way") || mLower.contains("3 way") || mLower.contains("1x2");

        for (int i = 0; i < outcomes.size(); i++) {
            BcgameOutcomeDto outcome = outcomes.get(i);
            if (outcome == null) continue;
            String name = outcome.getName();

            BetType betType = null;
            if (isDraw(name)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType);
            } else if (isTeam1(name, team1, team2)) {
                MatchResultBet.Outcome resOutcome = is3Way ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                betType = new MatchResultBet(scope, resOutcome, statType);
            } else if (isTeam2(name, team1, team2)) {
                MatchResultBet.Outcome resOutcome = is3Way ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                betType = new MatchResultBet(scope, resOutcome, statType);
            } else {
                if (outcomes.size() == 2 && !hasDraw) {
                    MatchResultBet.Outcome resOutcome = (i == 0) ? MatchResultBet.Outcome.WIN1_2WAY : MatchResultBet.Outcome.WIN2_2WAY;
                    betType = new MatchResultBet(scope, resOutcome, statType);
                } else if (outcomes.size() == 3 && hasDraw) {
                    if (i == 0) betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, statType);
                    else if (i == 2) betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, statType);
                }
            }

            if (betType != null) {
                addOddItem(items, outcome, groupName, betType);
            }
        }
    }

    private TotalBet.Direction resolveDirection(String outcomeName) {
        if (outcomeName == null || outcomeName.isBlank()) return null;
        String u = outcomeName.trim().toUpperCase();

        if (u.equals("EXACT") || u.equals("РОВНО") || u.startsWith("EXACT ") || u.startsWith("РОВНО ")
                || u.contains("EXACT") || u.contains("РОВНО")) {
            return TotalBet.Direction.EXACT;
        }

        if (u.equals("OVER") || u.equals("O") || u.equals("БОЛЬШЕ") || u.equals("Б")
                || u.startsWith("OVER") || u.startsWith("БОЛЬШЕ")
                || u.startsWith("O ") || u.startsWith("O(")
                || u.startsWith("Б ") || u.startsWith("Б(")
                || u.startsWith("ТБ") || u.contains("OVER") || u.contains("БОЛЬШЕ")
                || u.contains(" ТБ") || u.contains("ТБ ") || u.contains("(ТБ)")
                || u.contains("OVER ") || u.contains(" OVER")) {
            return TotalBet.Direction.OVER;
        }

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

            Matcher mParen = PAREN_PARAM_PATTERN.matcher(name);
            if (mParen.find()) {
                try {
                    return Double.parseDouble(mParen.group(1));
                } catch (NumberFormatException ignored) {}
            }

            Matcher mDir = DIR_PARAM_PATTERN.matcher(name);
            if (mDir.find()) {
                try {
                    return Double.parseDouble(mDir.group(1));
                } catch (NumberFormatException ignored) {}
            }

            Matcher mDec = DECIMAL_PATTERN.matcher(name);
            if (mDec.find()) {
                try {
                    return Double.parseDouble(mDec.group(1));
                } catch (NumberFormatException ignored) {}
            }

            Matcher mEnd = END_NUMBER_PATTERN.matcher(name);
            if (mEnd.find()) {
                try {
                    return Double.parseDouble(mEnd.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }

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

    private HandicapBet.Outcome resolveHandicapOutcome(BcgameOutcomeDto outcome, int index, int totalCount, String team1, String team2) {
        if (outcome == null) return null;
        String name = outcome.getName();
        if (name != null) {
            String u = name.trim().toUpperCase();
            if (isDraw(u)) return HandicapBet.Outcome.DRAW;
            if (isTeam1Outcome(u, team1, team2)) return HandicapBet.Outcome.TEAM1;
            if (isTeam2Outcome(u, team1, team2)) return HandicapBet.Outcome.TEAM2;
        }

        if (totalCount == 2) {
            return (index == 0) ? HandicapBet.Outcome.TEAM1 : HandicapBet.Outcome.TEAM2;
        } else if (totalCount == 3) {
            if (index == 0) return HandicapBet.Outcome.TEAM1;
            if (index == 1) return HandicapBet.Outcome.DRAW;
            if (index == 2) return HandicapBet.Outcome.TEAM2;
        }
        return null;
    }

    private Double resolveHandicapParam(BcgameOutcomeDto outcome, Double marketParam,
                                        HandicapBet.Outcome outcomeType, String team1, String team2) {
        Double dtoParam = outcome.getEffectiveParam();
        String name = outcome.getName();

        Double parsedFromName = extractSignedHandicap(name, team1, team2);
        if (parsedFromName != null) {
            if (dtoParam != null && dtoParam > 0 && parsedFromName < 0
                    && Math.abs(dtoParam - Math.abs(parsedFromName)) < 0.001) {
                return parsedFromName;
            }
            if (dtoParam == null) {
                return parsedFromName;
            }
        }

        if (dtoParam != null) {
            return dtoParam;
        }

        if (marketParam != null) {
            if (outcomeType == HandicapBet.Outcome.TEAM2) {
                return -marketParam;
            }
            return marketParam;
        }

        return null;
    }

    private Double extractSignedHandicap(String text, String team1, String team2) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher mParen = PAREN_PARAM_PATTERN.matcher(text);
        if (mParen.find()) {
            try {
                return Double.parseDouble(mParen.group(1));
            } catch (NumberFormatException ignored) {}
        }

        Matcher mScore = SCORE_HDP_PATTERN.matcher(text);
        if (mScore.find()) {
            try {
                double h = Double.parseDouble(mScore.group(1));
                double a = Double.parseDouble(mScore.group(2));
                return h - a;
            } catch (NumberFormatException ignored) {}
        }

        String trimmed = text.trim();
        if (PURE_SELECTION_PATTERN.matcher(trimmed).matches()) {
            return null;
        }
        if (team1 != null && !team1.isBlank() && trimmed.equalsIgnoreCase(team1.trim())) {
            return null;
        }
        if (team2 != null && !team2.isBlank() && trimmed.equalsIgnoreCase(team2.trim())) {
            return null;
        }

        Matcher mEnd = END_NUMBER_PATTERN.matcher(trimmed);
        if (mEnd.find()) {
            try {
                return Double.parseDouble(mEnd.group(1));
            } catch (NumberFormatException ignored) {}
        }

        String stripped = PREFIX_PATTERN.matcher(text).replaceFirst("");
        if (team1 != null && !team1.isBlank()) {
            stripped = stripped.replace(team1, " ");
        }
        if (team2 != null && !team2.isBlank()) {
            stripped = stripped.replace(team2, " ");
        }
        Matcher numMatcher = SIGNED_NUM_PATTERN.matcher(stripped);
        if (numMatcher.find()) {
            try {
                return Double.parseDouble(numMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        return null;
    }

    private boolean isDrawOutcome(BcgameOutcomeDto outcome) {
        if (outcome == null || outcome.getName() == null) return false;
        return isDraw(outcome.getName().trim().toUpperCase());
    }

    private boolean isDraw(String text) {
        if (text == null) return false;
        String u = text.trim().toUpperCase();
        return u.equals("X") || u.equals("Х")
                || u.equals("DRAW") || u.equals("TIE") || u.equals("НИЧЬЯ")
                || u.startsWith("X ") || u.startsWith("Х ")
                || u.startsWith("DRAW ") || u.startsWith("TIE ") || u.startsWith("НИЧЬЯ ")
                || u.startsWith("X(") || u.startsWith("Х(")
                || u.startsWith("DRAW(") || u.startsWith("TIE(") || u.startsWith("НИЧЬЯ(")
                || u.contains("DRAW") || u.contains("НИЧЬЯ") || u.contains("TIE");
    }

    private boolean isTeam1(String text, String team1, String team2) {
        if (text == null) return false;
        String u = text.trim().toUpperCase();
        if (u.equals("1") || u.equals("W1") || u.equals("WIN1") || u.equals("WIN 1")
                || u.equals("HOME") || u.equals("TEAM 1") || u.equals("TEAM1")
                || u.equals("П1") || u.equals("ПОБЕДА 1") || u.equals("ПОБЕДА1") || u.equals("1-Я КОМАНДА")) {
            return true;
        }
        if (team2 != null && !team2.isBlank() && u.equalsIgnoreCase(team2.trim())) {
            return false;
        }
        if (team1 != null && !team1.isBlank()) {
            String t1 = team1.toUpperCase().trim();
            if (u.equals(t1)) return true;
            if (team2 != null && !team2.isBlank() && u.contains(team2.toUpperCase().trim())) {
                return false;
            }
            if (u.startsWith(t1 + " ") || u.endsWith(" " + t1) || u.contains(t1)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTeam2(String text, String team1, String team2) {
        if (text == null) return false;
        String u = text.trim().toUpperCase();
        if (u.equals("2") || u.equals("W2") || u.equals("WIN2") || u.equals("WIN 2")
                || u.equals("AWAY") || u.equals("TEAM 2") || u.equals("TEAM2")
                || u.equals("П2") || u.equals("ПОБЕДА 2") || u.equals("ПОБЕДА2") || u.equals("2-Я КОМАНДА")) {
            return true;
        }
        if (team1 != null && !team1.isBlank() && u.equalsIgnoreCase(team1.trim())) {
            return false;
        }
        if (team2 != null && !team2.isBlank()) {
            String t2 = team2.toUpperCase().trim();
            if (u.equals(t2)) return true;
            if (team1 != null && !team1.isBlank() && u.contains(team1.toUpperCase().trim())) {
                return false;
            }
            if (u.startsWith(t2 + " ") || u.endsWith(" " + t2) || u.contains(t2)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTeam1Outcome(String u, String team1, String team2) {
        if (u.equals("1") || u.equals("H1") || u.equals("HOME") || u.equals("TEAM 1") || u.equals("TEAM1")
                || u.equals("Ф1") || u.equals("ФОРА 1") || u.equals("ФОРА1") || u.equals("П1") || u.equals("ПОБЕДА 1")) {
            return true;
        }
        if (u.startsWith("1 ") || u.startsWith("1(") || u.startsWith("1 -") || u.startsWith("1 +") || u.startsWith("1-") || u.startsWith("1+")
                || u.startsWith("H1 ") || u.startsWith("H1(") || u.startsWith("HOME ") || u.startsWith("HOME(")
                || u.startsWith("TEAM 1 ") || u.startsWith("TEAM1 ") || u.startsWith("TEAM 1(") || u.startsWith("TEAM1(")
                || u.startsWith("Ф1 ") || u.startsWith("Ф1(") || u.startsWith("ФОРА 1") || u.startsWith("ФОРА1")
                || u.startsWith("HANDICAP 1") || u.startsWith("SPREAD 1")) {
            return true;
        }
        if (team2 != null && !team2.isBlank() && containsTeamName(u, team2)) {
            return false;
        }
        if (team1 != null && !team1.isBlank() && containsTeamName(u, team1)) {
            return true;
        }
        return false;
    }

    private boolean isTeam2Outcome(String u, String team1, String team2) {
        if (u.equals("2") || u.equals("H2") || u.equals("AWAY") || u.equals("TEAM 2") || u.equals("TEAM2")
                || u.equals("Ф2") || u.equals("ФОРА 2") || u.equals("ФОРА2") || u.equals("П2") || u.equals("ПОБЕДА 2")) {
            return true;
        }
        if (u.startsWith("2 ") || u.startsWith("2(") || u.startsWith("2 -") || u.startsWith("2 +") || u.startsWith("2-") || u.startsWith("2+")
                || u.startsWith("H2 ") || u.startsWith("H2(") || u.startsWith("AWAY ") || u.startsWith("AWAY(")
                || u.startsWith("TEAM 2 ") || u.startsWith("TEAM2 ") || u.startsWith("TEAM 2(") || u.startsWith("TEAM2(")
                || u.startsWith("Ф2 ") || u.startsWith("Ф2(") || u.startsWith("ФОРА 2") || u.startsWith("ФОРА2")
                || u.startsWith("HANDICAP 2") || u.startsWith("SPREAD 2")) {
            return true;
        }
        if (team1 != null && !team1.isBlank() && containsTeamName(u, team1)) {
            return false;
        }
        if (team2 != null && !team2.isBlank() && containsTeamName(u, team2)) {
            return true;
        }
        return false;
    }

    private boolean isDoubleChanceOutcome(BcgameOutcomeDto outcome) {
        if (outcome == null || outcome.getName() == null) return false;
        String name = outcome.getName().trim().toUpperCase();
        return name.equals("1X") || name.equals("1/X") || name.equals("1-X")
                || name.equals("12") || name.equals("1/2") || name.equals("1-2")
                || name.equals("X2") || name.equals("2X") || name.equals("X/2") || name.equals("X-2");
    }

    private boolean is1X(String outcomeName, String team1) {
        if (outcomeName == null) return false;
        String u = outcomeName.trim().toUpperCase();
        if (u.equals("1X") || u.equals("1/X") || u.equals("1-X") || u.equals("1 X") || u.equals("1 OR X") || u.equals("1 ИЛИ X") || u.equals("1 ИЛИ Х")) {
            return true;
        }
        if (u.contains("HOME OR DRAW") || u.contains("DRAW OR HOME") || u.contains("HOME/DRAW") || u.contains("DRAW/HOME")) {
            return true;
        }
        if (u.contains("1X") || u.contains("1 OR X") || u.contains("1 ИЛИ X") || u.contains("1 ИЛИ Х")) {
            return true;
        }
        if (team1 != null && !team1.isBlank()) {
            String t1 = team1.toUpperCase().trim();
            if (u.contains(t1) && (u.contains("DRAW") || u.contains("TIE") || u.contains("НИЧЬЯ") || u.contains(" X") || u.endsWith("X") || u.contains(" Х") || u.endsWith("Х"))) {
                return true;
            }
        }
        return false;
    }

    private boolean is12(String outcomeName, String team1, String team2) {
        if (outcomeName == null) return false;
        String u = outcomeName.trim().toUpperCase();
        if (u.equals("12") || u.equals("1/2") || u.equals("1-2") || u.equals("1 2") || u.equals("1 OR 2") || u.equals("1 ИЛИ 2")) {
            return true;
        }
        if (u.contains("HOME OR AWAY") || u.contains("AWAY OR HOME") || u.contains("HOME/AWAY") || u.contains("AWAY/HOME")) {
            return true;
        }
        if (u.contains("12") || u.contains("1 OR 2") || u.contains("1 ИЛИ 2")) {
            return true;
        }
        if (team1 != null && team2 != null && !team1.isBlank() && !team2.isBlank()) {
            String t1 = team1.toUpperCase().trim();
            String t2 = team2.toUpperCase().trim();
            if (u.contains(t1) && u.contains(t2)) {
                return true;
            }
        }
        return false;
    }

    private boolean isX2(String outcomeName, String team2) {
        if (outcomeName == null) return false;
        String u = outcomeName.trim().toUpperCase();
        if (u.equals("X2") || u.equals("2X") || u.equals("Х2") || u.equals("2Х")
                || u.equals("X/2") || u.equals("2/X") || u.equals("Х/2") || u.equals("2/Х")
                || u.equals("X-2") || u.equals("2-X") || u.equals("Х-2") || u.equals("2-Х")
                || u.equals("X 2") || u.equals("X OR 2") || u.equals("2 OR X") || u.equals("X ИЛИ 2") || u.equals("2 ИЛИ X")
                || u.equals("Х ИЛИ 2") || u.equals("2 ИЛИ Х")) {
            return true;
        }
        if (u.contains("DRAW OR AWAY") || u.contains("AWAY OR DRAW") || u.contains("DRAW/AWAY") || u.contains("AWAY/DRAW")) {
            return true;
        }
        if (u.contains("X2") || u.contains("2X") || u.contains("Х2") || u.contains("2Х") || u.contains("X OR 2") || u.contains("2 OR X")) {
            return true;
        }
        if (team2 != null && !team2.isBlank()) {
            String t2 = team2.toUpperCase().trim();
            if (u.contains(t2) && (u.contains("DRAW") || u.contains("TIE") || u.contains("НИЧЬЯ") || u.contains("X ") || u.startsWith("X") || u.contains("Х ") || u.startsWith("Х"))) {
                return true;
            }
        }
        return false;
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

    private boolean isAsianLine(String marketName, Double param, boolean is3Way) {
        if (is3Way) {
            return false;
        }
        return isAsianLine(marketName, param);
    }

    private boolean isQuarterAsian(Double line) {
        if (line == null) return false;
        double rem = Math.abs(line) % 1.0;
        return Math.abs(rem - 0.25) < 0.001 || Math.abs(rem - 0.75) < 0.001;
    }
}

package pro.datawiki.igaming.source.bcgame.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for Handicap markets:
 * - Asian Handicap (2-way, quarter and half-ball lines)
 * - European Handicap (3-way with Draw option)
 * - Point Spread, Puck Line, Run Line
 * - Full match and periods (halves, quarters, periods, sets).
 */
@Slf4j
@Component
@Order(50)
public class BcgameHandicapMarketHandler extends AbstractBcgameMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern SCORE_HDP_PATTERN = Pattern.compile("\\((\\d+):(\\d+)\\)");
    private static final Pattern END_NUMBER_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)\\s*$");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?i)^(?:handicap\\s*[12]|фора\\s*[12]|spread\\s*[12]|h[12]|ф[12]|team\\s*[12]|[12])\\b\\s*");
    private static final Pattern PURE_SELECTION_PATTERN = Pattern.compile("(?i)^(?:handicap\\s*[12]|фора\\s*[12]|spread\\s*[12]|h[12]|ф[12]|team\\s*[12]|[12]|home|away|draw|tie|ничья|x|х|п1|п2|w1|w2|победа\\s*[12])$");
    private static final Pattern SIGNED_NUM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

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
                || mLower.contains("handicap maps") || mLower.contains("фора карт")
                || mLower.contains("handicap rounds") || mLower.contains("фора раундов")
                || mLower.contains("handicap kills") || mLower.contains("фора убийств")) {
            return false;
        }

        // 3. Exclude non-handicap markets
        if (mLower.contains("total") || mLower.contains("тотал")
                || mLower.contains("over") || mLower.contains("under")
                || mLower.contains("both teams") || mLower.contains("btts") || mLower.contains("обе забьют")
                || mLower.contains("correct score") || mLower.contains("точный счет")
                || mLower.contains("1x2") || mLower.contains("double chance") || mLower.contains("двойной шанс")
                || mLower.contains("draw no bet") || mLower.contains("moneyline")
                || mLower.contains("match result") || mLower.contains("исход")) {
            return false;
        }

        // 4. Accept handicap / spread / puck line / run line
        return mLower.contains("handicap") || mLower.contains("фора")
                || mLower.contains("spread") || mLower.contains("puck line")
                || mLower.contains("run line") || mLower.contains("hcap");
    }

    @Override
    public void handle(BcgameMarketDto market, BcgameMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
            return;
        }

        String marketName = market.getName() != null ? market.getName().trim() : "";
        SportType sportType = context != null ? context.getSportType() : null;
        BetScope scope = resolveScope(marketName, sportType);
        String groupName = !marketName.isBlank() ? marketName : "Handicap";

        String team1 = context != null ? context.getHomeTeam() : null;
        String team2 = context != null ? context.getAwayTeam() : null;

        List<BcgameOutcomeDto> outcomes = market.getOutcomes();
        boolean hasDraw = outcomes.stream().anyMatch(this::isDrawOutcome);
        boolean isExplicitEuropean = isEuropeanHandicapMarket(marketName);
        boolean is3Way = hasDraw || isExplicitEuropean;

        Double marketParam = extractSignedHandicap(marketName, team1, team2);

        for (int i = 0; i < outcomes.size(); i++) {
            BcgameOutcomeDto outcome = outcomes.get(i);
            if (outcome == null) continue;

            HandicapBet.Outcome outcomeType = resolveOutcome(outcome, i, outcomes.size(), team1, team2);
            if (outcomeType == null) {
                continue;
            }

            Double param = resolveHandicapParam(outcome, marketParam, outcomeType, team1, team2);
            if (param == null) {
                continue;
            }

            boolean isAsian = isAsianLine(marketName, param, is3Way);
            HandicapBet betType = new HandicapBet(scope, outcomeType, param, isAsian, StatType.MATCH);
            addOddItem(items, outcome, groupName, betType);
        }
    }

    private HandicapBet.Outcome resolveOutcome(BcgameOutcomeDto outcome, int index, int totalCount, String team1, String team2) {
        if (outcome == null) return null;
        String name = outcome.getName();
        if (name != null) {
            String u = name.trim().toUpperCase();

            // Check for Draw (in 3-way handicap)
            if (isDraw(u)) {
                return HandicapBet.Outcome.DRAW;
            }

            // Check Team 1
            if (isTeam1Outcome(u, team1, team2)) {
                return HandicapBet.Outcome.TEAM1;
            }

            // Check Team 2
            if (isTeam2Outcome(u, team1, team2)) {
                return HandicapBet.Outcome.TEAM2;
            }
        }

        // Positional fallbacks
        if (totalCount == 2) {
            return (index == 0) ? HandicapBet.Outcome.TEAM1 : HandicapBet.Outcome.TEAM2;
        } else if (totalCount == 3) {
            if (index == 0) return HandicapBet.Outcome.TEAM1;
            if (index == 1) return HandicapBet.Outcome.DRAW;
            if (index == 2) return HandicapBet.Outcome.TEAM2;
        }

        return null;
    }

    private boolean isDrawOutcome(BcgameOutcomeDto outcome) {
        if (outcome == null || outcome.getName() == null) return false;
        return isDraw(outcome.getName().trim().toUpperCase());
    }

    private boolean isDraw(String u) {
        return u.equals("X") || u.equals("Х")
                || u.equals("DRAW") || u.equals("TIE") || u.equals("НИЧЬЯ")
                || u.startsWith("X ") || u.startsWith("Х ")
                || u.startsWith("DRAW ") || u.startsWith("TIE ") || u.startsWith("НИЧЬЯ ")
                || u.startsWith("X(") || u.startsWith("Х(")
                || u.startsWith("DRAW(") || u.startsWith("TIE(") || u.startsWith("НИЧЬЯ(")
                || u.startsWith("ФОРА Х") || u.startsWith("ФОРА X") || u.startsWith("HANDICAP X")
                || u.contains("DRAW") || u.contains("НИЧЬЯ") || u.contains("TIE");
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
        if (team2 != null && !team2.isBlank() && containsTeam(u, team2)) {
            return false;
        }
        if (team1 != null && !team1.isBlank() && containsTeam(u, team1)) {
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
        if (team1 != null && !team1.isBlank() && containsTeam(u, team1)) {
            return false;
        }
        if (team2 != null && !team2.isBlank() && containsTeam(u, team2)) {
            return true;
        }
        return false;
    }

    private boolean containsTeam(String text, String teamName) {
        if (text == null || teamName == null || teamName.trim().length() < 3) {
            return false;
        }
        String p = Pattern.quote(teamName.trim().toUpperCase());
        Pattern pattern = Pattern.compile("(?i)(?:^|[^a-zA-Z0-9а-яА-ЯёЁ])" + p + "(?:$|[^a-zA-Z0-9а-яА-ЯёЁ])");
        return pattern.matcher(text).find();
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

        // 1. Parenthesized number: e.g. "Arsenal (-1.5)" -> -1.5, "(+1.5)" -> 1.5, "(0)" -> 0.0
        Matcher mParen = PAREN_PARAM_PATTERN.matcher(text);
        if (mParen.find()) {
            try {
                return Double.parseDouble(mParen.group(1));
            } catch (NumberFormatException ignored) {}
        }

        // 2. Score format: e.g. "(0:1)"
        Matcher mScore = SCORE_HDP_PATTERN.matcher(text);
        if (mScore.find()) {
            try {
                double h = Double.parseDouble(mScore.group(1));
                double a = Double.parseDouble(mScore.group(2));
                return h - a;
            } catch (NumberFormatException ignored) {}
        }

        // If the text is purely a team selection / outcome label (e.g. "1", "2", "Team 1", "Home", or matches team1/team2 name),
        // then the string contains NO handicap parameter!
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

        // 3. Number at end of text: e.g. "Arsenal -1.5" or "Lakers +6.5" or "1 -1.5"
        Matcher mEnd = END_NUMBER_PATTERN.matcher(trimmed);
        if (mEnd.find()) {
            try {
                return Double.parseDouble(mEnd.group(1));
            } catch (NumberFormatException ignored) {}
        }

        // 4. Strip prefixes and find signed numeric
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

    private boolean isEuropeanHandicapMarket(String marketName) {
        if (marketName == null) return false;
        String mLower = marketName.toLowerCase();
        return mLower.contains("european") || mLower.contains("европейск")
                || mLower.contains("3-way") || mLower.contains("3 way") || mLower.contains("threeway");
    }

    private boolean isAsianLine(String marketName, Double param, boolean is3Way) {
        if (is3Way) {
            return false;
        }
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

package pro.datawiki.igaming.source.bcgame.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.List;

/**
 * Handler for Match Result (1X2), Moneyline (2-Way), Double Chance (1X, 12, X2),
 * and Draw No Bet (DNB) markets for full match and periods.
 */
@Slf4j
@Component
@Order(30)
public class BcgameResultMarketHandler extends AbstractBcgameMarketHandler {

    @Override
    public boolean supports(String marketName, BcgameMarketContext context) {
        if (marketName == null || marketName.isBlank()) {
            return false;
        }
        String mLower = marketName.trim().toLowerCase();

        // Stats markets are handled by @Order(10) BcgameStatsMarketHandler
        if (isStats(marketName)) {
            return false;
        }

        // Totals, handicaps, BTTS, correct score are handled by their dedicated handlers
        if (mLower.contains("total") || mLower.contains("тотал")
                || mLower.contains("over") || mLower.contains("under")
                || mLower.contains("handicap") || mLower.contains("фора") || mLower.contains("spread")
                || mLower.contains("both teams") || mLower.contains("btts") || mLower.contains("обе забьют")
                || mLower.contains("correct score") || mLower.contains("точный счет")) {
            return false;
        }

        // Esports sub-markets (maps, rounds, kills, towers, first blood) handled by BcgameEsportsMarketHandler
        if (mLower.contains("map ") || mLower.contains("карта ") || mLower.contains("map1") || mLower.contains("map2")
                || mLower.contains("round ") || mLower.contains("раунд ")
                || mLower.contains("kill") || mLower.contains("first blood")
                || mLower.contains("tower") || mLower.contains("roshan") || mLower.contains("baron")) {
            return false;
        }

        // 1. Double Chance
        if (mLower.contains("double chance") || mLower.contains("двойной шанс")) {
            return true;
        }

        // 2. Draw No Bet
        if (mLower.contains("draw no bet") || mLower.contains("tie no bet") || mLower.contains("dnb") || mLower.contains("ничья исключена")) {
            return true;
        }

        // 3. 1X2 / Match Result / 3-Way
        if (mLower.equals("1x2") || mLower.contains("1x2") || mLower.contains("1 x 2") || mLower.contains("1-x-2")
                || mLower.contains("3-way") || mLower.contains("3 way") || mLower.contains("threeway")
                || mLower.contains("match result") || mLower.contains("full time result") || mLower.contains("fulltime result")
                || mLower.contains("half time result") || mLower.contains("halftime result")
                || mLower.contains("win-draw-win") || mLower.contains("win / draw / win")
                || mLower.contains("исход")) {
            return true;
        }

        // 4. Moneyline / 2-Way / Head to Head / Winner
        if (mLower.contains("moneyline") || mLower.contains("money line") || mLower.equals("ml")
                || mLower.contains("2-way") || mLower.contains("2 way") || mLower.contains("twoway")
                || mLower.contains("head to head") || mLower.contains("head-to-head") || mLower.contains("h2h")
                || mLower.contains("match winner") || mLower.contains("winner") || mLower.contains("победитель")
                || mLower.contains("to win") || mLower.contains("who will win") || mLower.contains("победа в матче")) {
            return true;
        }

        return false;
    }

    @Override
    public void handle(BcgameMarketDto market, BcgameMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
            return;
        }

        String marketName = market.getName() != null ? market.getName() : "";
        String mLower = marketName.toLowerCase().trim();
        SportType sportType = context != null ? context.getSportType() : null;
        BetScope scope = resolveScope(marketName, sportType);
        String groupName = !marketName.isBlank() ? marketName : "1X2";

        String team1 = context != null ? context.getHomeTeam() : null;
        String team2 = context != null ? context.getAwayTeam() : null;

        // 1. Double Chance
        boolean isDoubleChance = mLower.contains("double chance") || mLower.contains("двойной шанс")
                || market.getOutcomes().stream().anyMatch(this::isDoubleChanceOutcome);

        if (isDoubleChance) {
            handleDoubleChance(market, scope, groupName, team1, team2, items);
            return;
        }

        // 2. Draw No Bet (2-Way)
        boolean isDnb = mLower.contains("draw no bet") || mLower.contains("tie no bet")
                || mLower.contains("dnb") || mLower.contains("ничья исключена");

        if (isDnb) {
            handleDrawNoBet(market, scope, groupName, team1, team2, items);
            return;
        }

        // 3. 1X2 / 3-Way Match Result vs Moneyline (2-Way)
        handleResultOrMoneyline(market, scope, groupName, team1, team2, mLower, items);
    }

    private void handleDoubleChance(BcgameMarketDto market, BetScope scope, String groupName,
                                    String team1, String team2, List<OddItem> items) {
        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getName() == null) continue;
            String name = outcome.getName();

            BetType betType = null;
            if (is1X(name, team1)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH);
            } else if (is12(name, team1, team2)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.MATCH);
            } else if (isX2(name, team2)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, outcome, groupName, betType);
            }
        }
    }

    private void handleDrawNoBet(BcgameMarketDto market, BetScope scope, String groupName,
                                 String team1, String team2, List<OddItem> items) {
        List<BcgameOutcomeDto> outcomes = market.getOutcomes();
        for (int i = 0; i < outcomes.size(); i++) {
            BcgameOutcomeDto outcome = outcomes.get(i);
            if (outcome == null || outcome.getName() == null) continue;
            String name = outcome.getName();

            BetType betType = null;
            if (isTeam1(name, team1, team2)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH);
            } else if (isTeam2(name, team1, team2)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH);
            } else if (outcomes.size() == 2) {
                // Positional fallback for 2-way DNB
                if (i == 0) {
                    betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH);
                } else {
                    betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH);
                }
            }

            if (betType != null) {
                addOddItem(items, outcome, groupName, betType);
            }
        }
    }

    private void handleResultOrMoneyline(BcgameMarketDto market, BetScope scope, String groupName,
                                         String team1, String team2, String mLower, List<OddItem> items) {
        List<BcgameOutcomeDto> outcomes = market.getOutcomes();
        boolean hasDraw = outcomes.stream().anyMatch(this::isDraw);
        boolean isExplicit3Way = mLower.contains("3-way") || mLower.contains("3 way") || mLower.contains("threeway")
                || mLower.contains("1x2") || mLower.contains("1 x 2") || mLower.contains("1-x-2")
                || mLower.contains("win-draw-win") || mLower.contains("win / draw / win");
        boolean is3Way = hasDraw || isExplicit3Way;

        for (int i = 0; i < outcomes.size(); i++) {
            BcgameOutcomeDto outcome = outcomes.get(i);
            if (outcome == null) continue;
            String name = outcome.getName();

            BetType betType = null;
            if (isDraw(name)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH);
            } else if (isTeam1(name, team1, team2)) {
                MatchResultBet.Outcome resOutcome = is3Way ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                betType = new MatchResultBet(scope, resOutcome, StatType.MATCH);
            } else if (isTeam2(name, team1, team2)) {
                MatchResultBet.Outcome resOutcome = is3Way ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                betType = new MatchResultBet(scope, resOutcome, StatType.MATCH);
            } else {
                // Positional fallbacks
                if (outcomes.size() == 2 && !hasDraw) {
                    MatchResultBet.Outcome resOutcome = (i == 0) ? MatchResultBet.Outcome.WIN1_2WAY : MatchResultBet.Outcome.WIN2_2WAY;
                    betType = new MatchResultBet(scope, resOutcome, StatType.MATCH);
                } else if (outcomes.size() == 3 && hasDraw) {
                    if (i == 0) {
                        betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.MATCH);
                    } else if (i == 2) {
                        betType = new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.MATCH);
                    }
                }
            }

            if (betType != null) {
                addOddItem(items, outcome, groupName, betType);
            }
        }
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

    private boolean isTeam1(String outcomeName, String team1, String team2) {
        if (outcomeName == null) return false;
        String u = outcomeName.trim().toUpperCase();
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

    private boolean isTeam2(String outcomeName, String team1, String team2) {
        if (outcomeName == null) return false;
        String u = outcomeName.trim().toUpperCase();
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

    private boolean isDraw(BcgameOutcomeDto outcome) {
        if (outcome == null || outcome.getName() == null) return false;
        return isDraw(outcome.getName());
    }

    private boolean isDraw(String outcomeName) {
        if (outcomeName == null) return false;
        String u = outcomeName.trim().toUpperCase();
        return u.equals("X") || u.equals("Х")
                || u.equals("DRAW") || u.equals("TIE") || u.equals("НИЧЬЯ")
                || u.contains("DRAW") || u.contains("TIE") || u.contains("НИЧЬЯ");
    }
}

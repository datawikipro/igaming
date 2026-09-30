package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Corner statistics markets for Betano:
 * - Match Total Corners (Over / Under, Alternate Lines)
 * - Team Total Corners (Home / Away)
 * - Corners 1X2 (Most Corners / 3-Way / Match Bet)
 * - Corners Handicap (Corner Spread / Asian Handicap)
 * - Corners Double Chance (1X, 12, X2)
 * - Corners Draw No Bet (2-Way / Tie No Bet)
 * - Corners Odd / Even
 * - First Corner / Last Corner
 * - Support for Halves (1st Half, 2nd Half) scopes
 */
@Component
public class BetanoCornersMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("CORNER") || mName.contains("ESCANTIO") || mName.contains("ESCANTEIO");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        if (mName.contains("FIRST CORNER") || mName.contains("1ST CORNER") || mName.contains("PRIMEIRO ESCANTEIO")) {
            handleFirstLastCorner(market, event, scope, BinaryMarketBet.MarketType.FIRST_CORNER, "corners_first", items);
        } else if (mName.contains("LAST CORNER") || mName.contains("ULTIMO ESCANTEIO")) {
            handleFirstLastCorner(market, event, scope, BinaryMarketBet.MarketType.LAST_CORNER, "corners_last", items);
        } else if (mName.contains("DOUBLE CHANCE") || mName.contains("1X2 OR") || mName.contains("CHANCE DUPLA")) {
            handleCornersDoubleChance(market, event, scope, items);
        } else if (mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("EMPATE ANULA")) {
            handleCornersDrawNoBet(market, event, scope, items);
        } else if (mName.contains("ODD/EVEN") || mName.contains("ODD / EVEN") || mName.contains("PAR/IMPAR")) {
            handleCornersOddEven(market, event, scope, items);
        } else if (mName.contains("HANDICAP") || mName.contains("SPREAD")) {
            handleCornersHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER") || mName.contains("MAIS/MENOS")) {
            handleCornersTotal(market, event, scope, items);
        } else if (mName.contains("1X2") || mName.contains("WINNER") || mName.contains("MATCH") || mName.contains("MOST") || mName.contains("VENCEDOR")) {
            handleCorners1X2(market, event, scope, items);
        }
    }

    private void handleCornersTotal(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject marketSubject = resolveSubject(mName, event);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), mName);
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetSubject outcomeSubject = marketSubject;
            if (outcomeSubject == BetSubject.MATCH) {
                if (isTeam1(upper, event)) {
                    outcomeSubject = BetSubject.TEAM1;
                } else if (isTeam2(upper, event)) {
                    outcomeSubject = BetSubject.TEAM2;
                }
            }

            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.endsWith(" OVER") ||
                upper.startsWith("MAIS") || "OT_OVER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("OVER", scope, outcomeSubject, StatType.CORNERS, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.endsWith(" UNDER") ||
                       upper.startsWith("MENOS") || "OT_UNDER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("UNDER", scope, outcomeSubject, StatType.CORNERS, false, points);
            }

            if (betType != null) {
                String baseGroup = (outcomeSubject == BetSubject.MATCH) ? "corners_total" : ("corners_total_" + outcomeSubject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCorners1X2(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_1x2", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;

            if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE") || "OT_DRAW".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("X", scope, StatType.CORNERS);
            } else if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME") || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("1", scope, StatType.CORNERS);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY") || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("2", scope, StatType.CORNERS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCornersHandicap(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_handicap", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, StatType.CORNERS, false, hdp);
            } else if (isTeam2(upper, event) || "2".equals(upper) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, StatType.CORNERS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCornersDoubleChance(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_double_chance", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            boolean isHome = (event.getHomeTeam() != null && upper.contains(event.getHomeTeam().toUpperCase())) || upper.contains("HOME") || upper.startsWith("1 ") || upper.endsWith(" 1");
            boolean isAway = (event.getAwayTeam() != null && upper.contains(event.getAwayTeam().toUpperCase())) || upper.contains("AWAY") || upper.startsWith("2 ") || upper.endsWith(" 2");
            boolean isDraw = upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE") || upper.contains(" X") || upper.startsWith("X ") || upper.equals("X");

            BetType betType = null;
            if (upper.contains("1X") || (isHome && isDraw && !isAway)) {
                betType = map1X2DCRecord("1X", scope, StatType.CORNERS);
            } else if (upper.contains("12") || (isHome && isAway && !isDraw)) {
                betType = map1X2DCRecord("12", scope, StatType.CORNERS);
            } else if (upper.contains("X2") || upper.contains("2X") || (isAway && isDraw && !isHome)) {
                betType = map1X2DCRecord("X2", scope, StatType.CORNERS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCornersDrawNoBet(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_draw_no_bet", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, StatType.CORNERS, false, 0.0);
            } else if (isTeam2(upper, event) || "2".equals(upper) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, StatType.CORNERS, false, 0.0);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCornersOddEven(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_odd_even", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.contains("ODD") || upper.contains("ÍMPAR") || upper.contains("IMPAR")) {
                betType = mapOddEvenRecord("ODD", scope, StatType.CORNERS);
            } else if (upper.contains("EVEN") || upper.contains("PAR")) {
                betType = mapOddEvenRecord("EVEN", scope, StatType.CORNERS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleFirstLastCorner(BetanoMarketDto market, BetanoEventDto event, BetScope scope,
                                       BinaryMarketBet.MarketType marketType, String baseGroup, List<OddItem> items) {
        String group = formatGroupName(baseGroup, scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if (isTeam1(upper, event)) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event)) {
                bOutcome = BinaryMarketBet.Outcome.TEAM2;
            } else if (upper.contains("NONE") || upper.contains("NEITHER") || upper.contains("NENHUM")) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, marketType, bOutcome, StatType.CORNERS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private BetSubject resolveSubject(String marketName, BetanoEventDto event) {
        if (event.getHomeTeam() != null && marketName.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event.getAwayTeam() != null && marketName.contains(event.getAwayTeam().toUpperCase())) {
            return BetSubject.TEAM2;
        }
        if (marketName.contains("HOME CORNER") || marketName.contains("TEAM 1 CORNER") ||
            marketName.contains("ESCANTEIOS CASA") || marketName.contains("ESCANTEIOS TIME 1")) {
            return BetSubject.TEAM1;
        }
        if (marketName.contains("AWAY CORNER") || marketName.contains("TEAM 2 CORNER") ||
            marketName.contains("ESCANTEIOS FORA") || marketName.contains("ESCANTEIOS TIME 2")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }

    private boolean isTeam1(String outcomeName, BetanoEventDto event) {
        if (event.getHomeTeam() != null && outcomeName.contains(event.getHomeTeam().toUpperCase())) return true;
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME") || outcomeName.contains("TEAM 1") || outcomeName.contains("CASA");
    }

    private boolean isTeam2(String outcomeName, BetanoEventDto event) {
        if (event.getAwayTeam() != null && outcomeName.contains(event.getAwayTeam().toUpperCase())) return true;
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY") || outcomeName.contains("TEAM 2") || outcomeName.contains("FORA");
    }
}

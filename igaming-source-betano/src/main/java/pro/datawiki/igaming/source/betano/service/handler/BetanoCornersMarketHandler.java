package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Corner statistics markets for Betano:
 * - Match Total Corners (Over / Under, Alternate Lines, Asian lines)
 * - Team Total Corners (Home / Away)
 * - Corners 1X2 (Most Corners / 3-Way / Match Bet / 2-Way)
 * - Corners Handicap (Corner Spread / Asian Handicap)
 * - Corners Double Chance (1X, 12, X2)
 * - Corners Draw No Bet (2-Way / Tie No Bet / Empate Anula)
 * - Corners Odd / Even
 * - First Corner / Last Corner
 * - Support for Full Match and Halves (1st Half, 2nd Half) scopes
 */
@Component
@Order(10)
public class BetanoCornersMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String norm = normalizeText(market.getEffectiveName());
        return norm.contains("CORNER") || norm.contains("ESCANTEI") || norm.contains("ESCANTIO") ||
               norm.contains("ECKE") || norm.contains("ESQUINA") || norm.contains("CALCIO D'ANGOLO") ||
               norm.contains("ANGOLO") || norm.contains("ΚΟΡΝΕΡ");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mNorm = normalizeText(market.getEffectiveName());
        BetScope scope = resolveScope(market.getEffectiveName());

        boolean isFirst = (mNorm.contains("FIRST") || mNorm.contains("1ST") || mNorm.contains("PRIMEIR") || mNorm.contains("ERST")) &&
                          (mNorm.contains("CORNER") || mNorm.contains("ESCANTEI") || mNorm.contains("ECKE") || mNorm.contains("ESQUINA"));
        boolean isLast = (mNorm.contains("LAST") || mNorm.contains("ULTIM") || mNorm.contains("LETZT")) &&
                         (mNorm.contains("CORNER") || mNorm.contains("ESCANTEI") || mNorm.contains("ECKE") || mNorm.contains("ESQUINA"));

        if (isFirst) {
            handleFirstLastCorner(market, event, scope, BinaryMarketBet.MarketType.FIRST_CORNER, "corners_first", items);
        } else if (isLast) {
            handleFirstLastCorner(market, event, scope, BinaryMarketBet.MarketType.LAST_CORNER, "corners_last", items);
        } else if (mNorm.contains("DOUBLE CHANCE") || mNorm.contains("CHANCE DUPLA") || mNorm.contains("1X2 OR") ||
                   mNorm.contains("DOBLE OPORTUNIDAD") || hasDoubleChanceOutcomes(market)) {
            handleCornersDoubleChance(market, event, scope, items);
        } else if (mNorm.contains("DRAW NO BET") || mNorm.contains("DNB") || mNorm.contains("EMPATE ANULA") ||
                   mNorm.contains("EMPATE NAO TEM APOSTA") || mNorm.contains("TIE NO BET")) {
            handleCornersDrawNoBet(market, event, scope, items);
        } else if (mNorm.contains("ODD/EVEN") || mNorm.contains("ODD / EVEN") || mNorm.contains("PAR/IMPAR") ||
                   mNorm.contains("PAR O IMPAR") || hasOddEvenOutcomes(market)) {
            handleCornersOddEven(market, event, scope, items);
        } else if (mNorm.contains("TOTAL") || mNorm.contains("OVER/UNDER") || mNorm.contains("OVER / UNDER") ||
                   mNorm.contains("MAIS/MENOS") || mNorm.contains("MAS/MENOS") || mNorm.contains("O/U") || hasTotalOutcomes(market)) {
            handleCornersTotal(market, event, scope, items);
        } else if (mNorm.contains("HANDICAP") || mNorm.contains("SPREAD") || mNorm.contains("VANTAGEM") || hasHandicapOutcomes(market)) {
            handleCornersHandicap(market, event, scope, items);
        } else if (mNorm.contains("1X2") || mNorm.contains("WINNER") || mNorm.contains("MATCH") ||
                   mNorm.contains("MOST") || mNorm.contains("VENCEDOR") || mNorm.contains("MAIS ESCANTEIOS") || has1X2Outcomes(market)) {
            handleCorners1X2(market, event, scope, items);
        }
    }

    private void handleCornersTotal(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String mNorm = normalizeText(market.getEffectiveName());
        BetSubject marketSubject = resolveSubject(market.getEffectiveName(), event);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = normalizeText(oName);
            BetSubject outcomeSubject = marketSubject;
            if (outcomeSubject == BetSubject.MATCH) {
                if (isTeam1(oName, event)) {
                    outcomeSubject = BetSubject.TEAM1;
                } else if (isTeam2(oName, event)) {
                    outcomeSubject = BetSubject.TEAM2;
                }
            }

            boolean isAsian = isAsian(market.getEffectiveName(), points) || isAsian(oName, points);

            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.endsWith(" OVER") ||
                upper.startsWith("MAIS") || upper.startsWith("MAS") || upper.startsWith(">") || "OT_OVER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("OVER", scope, outcomeSubject, StatType.CORNERS, isAsian, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.endsWith(" UNDER") ||
                       upper.startsWith("MENOS") || upper.startsWith("<") || "OT_UNDER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("UNDER", scope, outcomeSubject, StatType.CORNERS, isAsian, points);
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
        boolean hasDraw = market.getOutcomes().stream().anyMatch(o -> isDraw(o.getName()) || "OT_DRAW".equalsIgnoreCase(o.getOutcomeType()));

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            BetType betType = null;

            if (isDraw(oName) || "OT_DRAW".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("X", scope, StatType.CORNERS);
            } else if (isTeam1(oName, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = hasDraw ? map1X2Record("1", scope, StatType.CORNERS)
                                  : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.CORNERS);
            } else if (isTeam2(oName, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = hasDraw ? map1X2Record("2", scope, StatType.CORNERS)
                                  : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.CORNERS);
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

            boolean isAsian = isAsian(market.getEffectiveName(), hdp) || isAsian(oName, hdp);

            BetType betType = null;
            if (isTeam1(oName, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, StatType.CORNERS, isAsian, hdp);
            } else if (isTeam2(oName, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, StatType.CORNERS, isAsian, hdp);
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
            String upper = normalizeText(oName);

            boolean isHome = isTeam1(oName, event);
            boolean isAway = isTeam2(oName, event);
            boolean isDraw = isDraw(oName);

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

            BetType betType = null;
            if (isTeam1(oName, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, StatType.CORNERS, false, 0.0);
            } else if (isTeam2(oName, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
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
            String upper = normalizeText(oName);

            BetType betType = null;
            if (upper.contains("ODD") || upper.contains("IMPAR") || upper.contains("UNGERADE")) {
                betType = mapOddEvenRecord("ODD", scope, StatType.CORNERS);
            } else if (upper.contains("EVEN") || upper.contains("PAR") || upper.contains("GERADE")) {
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
            String upper = normalizeText(oName);

            BinaryMarketBet.Outcome bOutcome = null;
            if (isTeam1(oName, event)) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(oName, event)) {
                bOutcome = BinaryMarketBet.Outcome.TEAM2;
            } else if (upper.contains("NONE") || upper.contains("NEITHER") || upper.contains("NENHUM") || upper.contains("NINGUNO")) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, marketType, bOutcome, StatType.CORNERS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private boolean hasTotalOutcomes(BetanoMarketDto market) {
        if (market.getOutcomes() == null) return false;
        return market.getOutcomes().stream().anyMatch(o -> {
            String type = o.getOutcomeType();
            if ("OT_OVER".equalsIgnoreCase(type) || "OT_UNDER".equalsIgnoreCase(type)) return true;
            String upper = normalizeText(o.getName());
            return upper.startsWith("OVER") || upper.startsWith("UNDER") ||
                   upper.startsWith("MAIS") || upper.startsWith("MENOS") ||
                   upper.startsWith("MAS") || upper.startsWith(">") || upper.startsWith("<");
        });
    }

    private boolean hasHandicapOutcomes(BetanoMarketDto market) {
        if (market.getOutcomes() == null) return false;
        return market.getOutcomes().stream().anyMatch(o -> {
            if (o.getHandicap() != null && !hasTotalOutcomes(market)) return true;
            String name = o.getName();
            return name != null && (name.contains("(+") || name.contains("(-"));
        });
    }

    private boolean hasDoubleChanceOutcomes(BetanoMarketDto market) {
        if (market.getOutcomes() == null) return false;
        return market.getOutcomes().stream().anyMatch(o -> {
            String upper = normalizeText(o.getName());
            return upper.equals("1X") || upper.equals("12") || upper.equals("X2") || upper.equals("2X") ||
                   upper.contains(" 1X ") || upper.contains(" 12 ") || upper.contains(" X2 ");
        });
    }

    private boolean hasOddEvenOutcomes(BetanoMarketDto market) {
        if (market.getOutcomes() == null) return false;
        return market.getOutcomes().stream().anyMatch(o -> {
            String upper = normalizeText(o.getName());
            return upper.equals("ODD") || upper.equals("EVEN") || upper.equals("IMPAR") || upper.equals("PAR");
        });
    }

    private boolean has1X2Outcomes(BetanoMarketDto market) {
        if (market.getOutcomes() == null) return false;
        return market.getOutcomes().stream().anyMatch(o ->
                isDraw(o.getName()) || "OT_DRAW".equalsIgnoreCase(o.getOutcomeType()));
    }
}

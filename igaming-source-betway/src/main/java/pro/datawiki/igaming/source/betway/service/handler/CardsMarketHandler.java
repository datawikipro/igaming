package pro.datawiki.igaming.source.betway.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Yellow Cards, Cards, and Bookings statistics:
 * - Match Total Cards / Bookings (Over / Under, Alternate Lines)
 * - Team Total Cards / Bookings (Home / Away)
 * - Cards 1X2 (Most Cards / Most Bookings / 3-Way)
 * - Cards Handicap (Card Spread / Booking Handicap)
 * - Cards Double Chance (1X, 12, X2)
 * - Cards Draw No Bet (2-Way / Tie No Bet)
 * - Cards Odd / Even
 * - Red Card (Sending Off - Yes / No)
 * - First Card / Last Card
 * - Support for Halves (1st Half, 2nd Half) scopes
 */
@Component
public class CardsMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("CARD") || mName.contains("BOOKING");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        if (mName.contains("RED CARD") || mName.contains("SENDING OFF") || mName.contains("SENT OFF")) {
            handleRedCard(market, event, scope, items);
        } else if (mName.contains("FIRST CARD") || mName.contains("1ST CARD") || mName.contains("FIRST BOOKING") || mName.contains("1ST BOOKING")) {
            handleFirstLastCard(market, event, scope, BinaryMarketBet.MarketType.FIRST_CARD, "cards_first", items);
        } else if (mName.contains("LAST CARD") || mName.contains("LAST BOOKING")) {
            handleFirstLastCard(market, event, scope, BinaryMarketBet.MarketType.LAST_CARD, "cards_last", items);
        } else if (mName.contains("DOUBLE CHANCE") || mName.contains("1X2 OR")) {
            handleCardsDoubleChance(market, event, scope, items);
        } else if (mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("2-WAY") || mName.contains("2 WAY") || mName.contains("TIE NO BET")) {
            handleCardsDrawNoBet(market, event, scope, items);
        } else if (mName.contains("ODD/EVEN") || mName.contains("ODD / EVEN") || mName.contains("ODD OR EVEN")) {
            handleCardsOddEven(market, event, scope, items);
        } else if (mName.contains("HANDICAP") || mName.contains("SPREAD")) {
            handleCardsHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER") || mName.contains("O/U")) {
            handleCardsTotal(market, event, scope, items);
        } else if (mName.contains("1X2") || mName.contains("WINNER") || mName.contains("MATCH") || mName.contains("MOST") || mName.contains("3-WAY") || mName.contains("3 WAY")) {
            handleCards1X2(market, event, scope, items);
        }
    }

    private void handleCardsTotal(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject marketSubject = resolveSubject(mName, event);

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
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
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.endsWith(" OVER")) {
                betType = mapTotalRecord("OVER", scope, outcomeSubject, StatType.YELLOW_CARDS, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.endsWith(" UNDER")) {
                betType = mapTotalRecord("UNDER", scope, outcomeSubject, StatType.YELLOW_CARDS, false, points);
            }

            if (betType != null) {
                String baseGroup = (outcomeSubject == BetSubject.MATCH) ? "cards_total" : ("cards_total_" + outcomeSubject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCards1X2(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_1x2", scope);
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;

            if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.equals("EQUAL")) {
                betType = map1X2Record("X", scope, StatType.YELLOW_CARDS);
            } else if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME")) {
                betType = map1X2Record("1", scope, StatType.YELLOW_CARDS);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY")) {
                betType = map1X2Record("2", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsHandicap(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_handicap", scope);
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = mapHandicapRecord("1", scope, StatType.YELLOW_CARDS, false, hdp);
            } else if (isTeam2(upper, event)) {
                betType = mapHandicapRecord("2", scope, StatType.YELLOW_CARDS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsDoubleChance(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_double_chance", scope);
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            boolean isHome = (event.getHomeTeam() != null && upper.contains(event.getHomeTeam().toUpperCase())) || upper.contains("HOME") || upper.startsWith("1 ") || upper.endsWith(" 1");
            boolean isAway = (event.getAwayTeam() != null && upper.contains(event.getAwayTeam().toUpperCase())) || upper.contains("AWAY") || upper.startsWith("2 ") || upper.endsWith(" 2");
            boolean isDraw = upper.contains("DRAW") || upper.contains("TIE") || upper.contains(" X") || upper.startsWith("X ") || upper.equals("X");

            BetType betType = null;
            if (upper.contains("1X") || (isHome && isDraw && !isAway)) {
                betType = map1X2DCRecord("1X", scope, StatType.YELLOW_CARDS);
            } else if (upper.contains("12") || (isHome && isAway && !isDraw)) {
                betType = map1X2DCRecord("12", scope, StatType.YELLOW_CARDS);
            } else if (upper.contains("X2") || upper.contains("2X") || (isAway && isDraw && !isHome)) {
                betType = map1X2DCRecord("X2", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsDrawNoBet(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_draw_no_bet", scope);
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = mapHandicapRecord("1", scope, StatType.YELLOW_CARDS, false, 0.0);
            } else if (isTeam2(upper, event)) {
                betType = mapHandicapRecord("2", scope, StatType.YELLOW_CARDS, false, 0.0);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsOddEven(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_odd_even", scope);
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.contains("ODD")) {
                betType = mapOddEvenRecord("ODD", scope, StatType.YELLOW_CARDS);
            } else if (upper.contains("EVEN")) {
                betType = mapOddEvenRecord("EVEN", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleRedCard(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_red_card", scope);
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if ("YES".equals(upper) || upper.startsWith("YES")) {
                bOutcome = BinaryMarketBet.Outcome.YES;
            } else if ("NO".equals(upper) || upper.startsWith("NO")) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.RED_CARD, bOutcome, StatType.YELLOW_CARDS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private void handleFirstLastCard(BetwayMarketDto market, BetwayEventDto event, BetScope scope,
                                     BinaryMarketBet.MarketType marketType, String baseGroup, List<OddItem> items) {
        String group = formatGroupName(baseGroup, scope);
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if (isTeam1(upper, event)) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event)) {
                bOutcome = BinaryMarketBet.Outcome.TEAM2;
            } else if (upper.contains("NONE") || upper.contains("NEITHER") || upper.contains("NO CARD") || upper.contains("NO BOOKING")) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, marketType, bOutcome, StatType.YELLOW_CARDS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private BetSubject resolveSubject(String marketName, BetwayEventDto event) {
        if (event.getHomeTeam() != null && marketName.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event.getAwayTeam() != null && marketName.contains(event.getAwayTeam().toUpperCase())) {
            return BetSubject.TEAM2;
        }
        if (marketName.contains("HOME CARD") || marketName.contains("HOME BOOKING") ||
            marketName.contains("TEAM 1 CARD") || marketName.contains("TEAM 1 BOOKING") ||
            marketName.contains("CARDS - HOME") || marketName.contains("BOOKINGS - HOME") ||
            marketName.contains("CARDS - TEAM 1") || marketName.contains("BOOKINGS - TEAM 1")) {
            return BetSubject.TEAM1;
        }
        if (marketName.contains("AWAY CARD") || marketName.contains("AWAY BOOKING") ||
            marketName.contains("TEAM 2 CARD") || marketName.contains("TEAM 2 BOOKING") ||
            marketName.contains("CARDS - AWAY") || marketName.contains("BOOKINGS - AWAY") ||
            marketName.contains("CARDS - TEAM 2") || marketName.contains("BOOKINGS - TEAM 2")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }

    private boolean isTeam1(String outcomeName, BetwayEventDto event) {
        if (event.getHomeTeam() != null && outcomeName.contains(event.getHomeTeam().toUpperCase())) return true;
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME") || outcomeName.contains("TEAM 1") || outcomeName.startsWith("1 ");
    }

    private boolean isTeam2(String outcomeName, BetwayEventDto event) {
        if (event.getAwayTeam() != null && outcomeName.contains(event.getAwayTeam().toUpperCase())) return true;
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY") || outcomeName.contains("TEAM 2") || outcomeName.startsWith("2 ");
    }
}

package pro.datawiki.igaming.source.betano.service.handler;

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
 * Dedicated handler for Yellow Cards, Cards, and Bookings statistics for Betano:
 * - Match Total Cards / Bookings (Over / Under, Alternate Lines, Asian lines)
 * - Team Total Cards / Bookings (Home / Away)
 * - Cards 1X2 (Most Cards / Most Bookings / 3-Way / 2-Way)
 * - Cards Handicap (Card Spread / Asian Handicap)
 * - Cards Double Chance (1X, 12, X2)
 * - Cards Draw No Bet (2-Way / Tie No Bet / Empate Anula)
 * - Cards Odd / Even
 * - Red Card (Sending Off - Yes / No)
 * - First Card / Last Card
 * - Support for StatType.YELLOW_CARDS vs StatType.CARDS differentiation
 * - Support for Full Match and Halves (1st Half, 2nd Half) scopes
 */
@Component
public class BetanoCardsMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String norm = normalizeText(market.getEffectiveName());
        return norm.contains("CARD") || norm.contains("BOOKING") || norm.contains("CARTAO") ||
               norm.contains("CARTOES") || norm.contains("TARJETA") || norm.contains("KARTEN") ||
               norm.contains("KARTE") || norm.contains("CARTONAS") || norm.contains("YELLOW") ||
               norm.contains("AMAREL") || norm.contains("AMARILL") || norm.contains("GELB") ||
               norm.contains("GALBEN") || norm.contains("VERMELH") || norm.contains("ROJA") ||
               norm.contains("EXPULSAO") || norm.contains("EXPULSION") ||
               norm.contains("SENDING OFF") || norm.contains("SENT OFF");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mNorm = normalizeText(market.getEffectiveName());
        BetScope scope = resolveScope(market.getEffectiveName());

        if (mNorm.contains("RED CARD") || mNorm.contains("CARTAO VERMELHO") || mNorm.contains("TARJETA ROJA") ||
            mNorm.contains("ROTE KARTE") || mNorm.contains("CARTONAS ROSU") || mNorm.contains("SENDING OFF") ||
            mNorm.contains("SENT OFF") || mNorm.contains("EXPULSAO") || mNorm.contains("EXPULSION")) {
            handleRedCard(market, event, scope, items);
            return;
        }

        StatType statType = resolveStatType(mNorm);
        String prefix = (statType == StatType.YELLOW_CARDS) ? "yellow_cards" : "cards";

        boolean isFirst = (mNorm.contains("FIRST") || mNorm.contains("1ST") || mNorm.contains("PRIMEIR") || mNorm.contains("ERST")) &&
                          (mNorm.contains("CARD") || mNorm.contains("BOOKING") || mNorm.contains("CARTAO") || mNorm.contains("TARJETA") || mNorm.contains("KARTE"));
        boolean isLast = (mNorm.contains("LAST") || mNorm.contains("ULTIM") || mNorm.contains("LETZT")) &&
                         (mNorm.contains("CARD") || mNorm.contains("BOOKING") || mNorm.contains("CARTAO") || mNorm.contains("TARJETA") || mNorm.contains("KARTE"));

        if (isFirst) {
            handleFirstLastCard(market, event, scope, statType, BinaryMarketBet.MarketType.FIRST_CARD, prefix + "_first", items);
        } else if (isLast) {
            handleFirstLastCard(market, event, scope, statType, BinaryMarketBet.MarketType.LAST_CARD, prefix + "_last", items);
        } else if (mNorm.contains("DOUBLE CHANCE") || mNorm.contains("CHANCE DUPLA") || mNorm.contains("1X2 OR") ||
                   mNorm.contains("DOBLE OPORTUNIDAD") || hasDoubleChanceOutcomes(market)) {
            handleCardsDoubleChance(market, event, scope, statType, prefix, items);
        } else if (mNorm.contains("DRAW NO BET") || mNorm.contains("DNB") || mNorm.contains("EMPATE ANULA") ||
                   mNorm.contains("EMPATE NAO TEM APOSTA") || mNorm.contains("TIE NO BET")) {
            handleCardsDrawNoBet(market, event, scope, statType, prefix, items);
        } else if (mNorm.contains("ODD/EVEN") || mNorm.contains("ODD / EVEN") || mNorm.contains("PAR/IMPAR") ||
                   mNorm.contains("PAR O IMPAR") || hasOddEvenOutcomes(market)) {
            handleCardsOddEven(market, event, scope, statType, prefix, items);
        } else if (mNorm.contains("TOTAL") || mNorm.contains("OVER/UNDER") || mNorm.contains("OVER / UNDER") ||
                   mNorm.contains("MAIS/MENOS") || mNorm.contains("MAS/MENOS") || mNorm.contains("O/U") || hasTotalOutcomes(market)) {
            handleCardsTotal(market, event, scope, statType, prefix, items);
        } else if (mNorm.contains("HANDICAP") || mNorm.contains("SPREAD") || mNorm.contains("VANTAGEM") || hasHandicapOutcomes(market)) {
            handleCardsHandicap(market, event, scope, statType, prefix, items);
        } else if (mNorm.contains("1X2") || mNorm.contains("WINNER") || mNorm.contains("MATCH") ||
                   mNorm.contains("MOST") || mNorm.contains("VENCEDOR") || mNorm.contains("MAIS CARTOES") || has1X2Outcomes(market)) {
            handleCards1X2(market, event, scope, statType, prefix, items);
        }
    }

    private StatType resolveStatType(String text) {
        if (text == null) return StatType.CARDS;
        if (text.contains("YELLOW") || text.contains("AMAREL") || text.contains("AMARILL") ||
            text.contains("GELB") || text.contains("GALBEN")) {
            return StatType.YELLOW_CARDS;
        }
        return StatType.CARDS;
    }

    private void handleCardsTotal(BetanoMarketDto market, BetanoEventDto event, BetScope scope,
                                  StatType statType, String prefix, List<OddItem> items) {
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
                betType = mapTotalRecord("OVER", scope, outcomeSubject, statType, isAsian, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.endsWith(" UNDER") ||
                       upper.startsWith("MENOS") || upper.startsWith("<") || "OT_UNDER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("UNDER", scope, outcomeSubject, statType, isAsian, points);
            }

            if (betType != null) {
                String baseGroup = (outcomeSubject == BetSubject.MATCH) ? (prefix + "_total") : (prefix + "_total_" + outcomeSubject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCards1X2(BetanoMarketDto market, BetanoEventDto event, BetScope scope,
                                StatType statType, String prefix, List<OddItem> items) {
        String group = formatGroupName(prefix + "_1x2", scope);
        boolean hasDraw = market.getOutcomes().stream().anyMatch(o -> isDraw(o.getName()) || "OT_DRAW".equalsIgnoreCase(o.getOutcomeType()));

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            BetType betType = null;

            if (isDraw(oName) || "OT_DRAW".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("X", scope, statType);
            } else if (isTeam1(oName, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = hasDraw ? map1X2Record("1", scope, statType)
                                  : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType);
            } else if (isTeam2(oName, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = hasDraw ? map1X2Record("2", scope, statType)
                                  : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsHandicap(BetanoMarketDto market, BetanoEventDto event, BetScope scope,
                                     StatType statType, String prefix, List<OddItem> items) {
        String group = formatGroupName(prefix + "_handicap", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            boolean isAsian = isAsian(market.getEffectiveName(), hdp) || isAsian(oName, hdp);

            BetType betType = null;
            if (isTeam1(oName, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, statType, isAsian, hdp);
            } else if (isTeam2(oName, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, statType, isAsian, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsDoubleChance(BetanoMarketDto market, BetanoEventDto event, BetScope scope,
                                         StatType statType, String prefix, List<OddItem> items) {
        String group = formatGroupName(prefix + "_double_chance", scope);
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
                betType = map1X2DCRecord("1X", scope, statType);
            } else if (upper.contains("12") || (isHome && isAway && !isDraw)) {
                betType = map1X2DCRecord("12", scope, statType);
            } else if (upper.contains("X2") || upper.contains("2X") || (isAway && isDraw && !isHome)) {
                betType = map1X2DCRecord("X2", scope, statType);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsDrawNoBet(BetanoMarketDto market, BetanoEventDto event, BetScope scope,
                                      StatType statType, String prefix, List<OddItem> items) {
        String group = formatGroupName(prefix + "_draw_no_bet", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";

            BetType betType = null;
            if (isTeam1(oName, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, statType, false, 0.0);
            } else if (isTeam2(oName, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, statType, false, 0.0);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsOddEven(BetanoMarketDto market, BetanoEventDto event, BetScope scope,
                                    StatType statType, String prefix, List<OddItem> items) {
        String group = formatGroupName(prefix + "_odd_even", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = normalizeText(oName);

            BetType betType = null;
            if (upper.contains("ODD") || upper.contains("IMPAR") || upper.contains("UNGERADE")) {
                betType = mapOddEvenRecord("ODD", scope, statType);
            } else if (upper.contains("EVEN") || upper.contains("PAR") || upper.contains("GERADE")) {
                betType = mapOddEvenRecord("EVEN", scope, statType);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleRedCard(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        BetSubject marketSubject = resolveSubject(market.getEffectiveName(), event);
        String baseGroup = (marketSubject == BetSubject.MATCH) ? "red_card" : ("red_card_" + marketSubject.name().toLowerCase());
        String group = formatGroupName(baseGroup, scope);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = normalizeText(oName);

            BinaryMarketBet.Outcome bOutcome = null;
            if (upper.equals("YES") || upper.equals("SIM") || upper.equals("SI") ||
                upper.equals("JA") || upper.equals("DA") || "OT_YES".equalsIgnoreCase(outcome.getOutcomeType())) {
                bOutcome = BinaryMarketBet.Outcome.YES;
            } else if (upper.equals("NO") || upper.equals("NAO") || upper.equals("NEIN") ||
                       upper.equals("NU") || "OT_NO".equalsIgnoreCase(outcome.getOutcomeType())) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, marketSubject, BinaryMarketBet.MarketType.RED_CARD, bOutcome, StatType.CARDS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private void handleFirstLastCard(BetanoMarketDto market, BetanoEventDto event, BetScope scope,
                                     StatType statType, BinaryMarketBet.MarketType marketType, String baseGroup, List<OddItem> items) {
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
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, marketType, bOutcome, statType);
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

package pro.datawiki.igaming.source.bet7k.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kMarketDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kOutcomeDto;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Dedicated handler for Card statistics markets (Cartões / Cartões Amarelos / Yellow Cards / Bookings):
 * - Match Total Cards / Yellow Cards (Over / Under, Cartões Mais/Menos, Total de Cartões Amarelos)
 * - Team Total Cards (Home / Away, Cartões Amarelos Casa / Fora)
 * - Cards Handicap (Card Spread / Handicap de Cartões / Handicap Asiático)
 * - Cards 1X2 (Winner / Most Cards / Vencedor dos Cartões / Mais Cartões)
 * - Red Card (Cartão Vermelho na Partida / Expulsão: Sim / Não)
 * - First Card / Last Card (Primeiro / Último Cartão)
 * - Cards Double Chance (Dupla Chance de Cartões)
 * - Cards Draw No Bet (Empate Anula Aposta - Cartões)
 * - Cards Odd / Even (Par / Ímpar de Cartões)
 * - Scopes: Full Match, 1st Half (1º Tempo), 2nd Half (2º Tempo)
 */
@Component
public class CardsMarketHandler extends AbstractBet7kMarketHandler {

    private static final Pattern CARD_PATTERN = Pattern.compile("(?i)\\b(CARD|CARDS|BOOKING|BOOKINGS|CARTÃO|CARTAO|CARTÕES|CARTOES|AMARELO|AMARELOS|AMAREL|VERMELHO|VERMELHOS|VERMELH|EXPULSÃO|EXPULSAO|EXPULSÕES|EXPULSOES)\\b");

    @Override
    public boolean supports(Bet7kMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO")) return false;

        if (CARD_PATTERN.matcher(mName).find()) {
            return true;
        }
        if (market.getCategory() != null && CARD_PATTERN.matcher(market.getCategory()).find()) {
            return true;
        }
        if (market.getGroup() != null && CARD_PATTERN.matcher(market.getGroup()).find()) {
            return true;
        }
        return false;
    }

    @Override
    public void handle(Bet7kMarketDto market, Bet7kEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        BetScope scope = resolveScope(mName + " " + period);

        if (isRedCard(mName)) {
            handleRedCard(market, event, scope, items);
        } else if (isFirstCard(mName)) {
            handleFirstLastCard(market, event, scope, BinaryMarketBet.MarketType.FIRST_CARD, "cards_first", items);
        } else if (isLastCard(mName)) {
            handleFirstLastCard(market, event, scope, BinaryMarketBet.MarketType.LAST_CARD, "cards_last", items);
        } else if (isDoubleChance(mName)) {
            handleCardsDoubleChance(market, event, scope, items);
        } else if (isDrawNoBet(mName)) {
            handleCardsDrawNoBet(market, event, scope, items);
        } else if (isOddEven(mName)) {
            handleCardsOddEven(market, event, scope, items);
        } else if (isHandicap(mName)) {
            handleCardsHandicap(market, event, scope, items);
        } else if (is1X2(mName)) {
            handleCards1X2(market, event, scope, items);
        } else if (isTotal(mName)) {
            handleCardsTotal(market, event, scope, items);
        } else {
            handleMixedOutcomes(market, event, scope, items);
        }
    }

    private boolean isRedCard(String mName) {
        return mName.contains("RED CARD") || mName.contains("SENDING OFF") || mName.contains("SENT OFF") ||
               mName.contains("CARTÃO VERMELHO") || mName.contains("CARTAO VERMELHO") ||
               (mName.contains("VERMELHO") && !mName.contains("AMAREL")) ||
               mName.contains("EXPULSÃO") || mName.contains("EXPULSAO");
    }

    private boolean isFirstCard(String mName) {
        return mName.contains("FIRST CARD") || mName.contains("1ST CARD") || mName.contains("FIRST BOOKING") || mName.contains("1ST BOOKING") ||
               mName.contains("PRIMEIRO CARTÃO") || mName.contains("1º CARTÃO") || mName.contains("1° CARTÃO") ||
               mName.contains("PRIMEIRO CARTAO") || mName.contains("1º CARTAO") || mName.contains("1° CARTAO");
    }

    private boolean isLastCard(String mName) {
        return mName.contains("LAST CARD") || mName.contains("LAST BOOKING") ||
               mName.contains("ÚLTIMO CARTÃO") || mName.contains("ULTIMO CARTAO") ||
               mName.contains("ÚLTIMO CARTAO") || mName.contains("ULTIMO CARTÃO");
    }

    private boolean isDoubleChance(String mName) {
        return mName.contains("DOUBLE CHANCE") || mName.contains("DUPLA CHANCE") ||
               mName.contains("CHANCE DUPLA") || mName.contains("1X2 OU") || mName.contains("1X2 OR");
    }

    private boolean isDrawNoBet(String mName) {
        return mName.contains("DRAW NO BET") || mName.contains("DNB") ||
               mName.contains("EMPATE ANULA") || mName.contains("2-WAY") || mName.contains("2 WAY") ||
               mName.contains("TIE NO BET");
    }

    private boolean isOddEven(String mName) {
        return mName.contains("ODD/EVEN") || mName.contains("ODD / EVEN") || mName.contains("ODD OR EVEN") ||
               mName.contains("PAR/ÍMPAR") || mName.contains("PAR/IMPAR") ||
               mName.contains("PAR OU ÍMPAR") || mName.contains("PAR OU IMPAR");
    }

    private boolean isHandicap(String mName) {
        return mName.contains("HANDICAP") || mName.contains("SPREAD") || mName.contains("DESVANTAGEM");
    }

    private boolean isTotal(String mName) {
        return mName.contains("TOTAL") ||
               mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER") ||
               mName.contains("MAIS/MENOS") || mName.contains("MAIS / MENOS") ||
               mName.contains("ACIMA/ABAIXO") || mName.contains("ACIMA / ABAIXO") ||
               mName.contains("O/U") ||
               mName.contains("OVER") || mName.contains("UNDER") ||
               mName.contains("ACIMA DE") || mName.contains("ABAIXO DE") ||
               mName.contains("MAIS DE") || mName.contains("MENOS DE");
    }

    private boolean is1X2(String mName) {
        return mName.contains("1X2") || mName.contains("WINNER") || mName.contains("VENCEDOR") ||
               mName.contains("RESULTADO") || mName.contains("QUEM TERÁ MAIS") || mName.contains("QUEM TERA MAIS") ||
               mName.contains("MAIOR NÚMERO") || mName.contains("MAIOR NUMERO") ||
               mName.contains("MOST CARD") || mName.contains("MOST CARTÃO") || mName.contains("MOST CARTAO") ||
               mName.contains("MAIS CARTÕES") || mName.contains("MAIS CARTOES") ||
               mName.contains("MAIS CARTÕES AMARELOS") || mName.contains("MAIS CARTOES AMARELOS") ||
               mName.contains("3-WAY") || mName.contains("3 WAY");
    }

    private void handleRedCard(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        boolean hasOverUnder = market.getOutcomes().stream().anyMatch(o -> o.getName() != null && (isOver(o.getName()) || isUnder(o.getName())));
        if (hasOverUnder) {
            handleCardsTotal(market, event, scope, items);
            return;
        }

        String group = formatGroupName("cards_red_card", scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if ("SIM".equals(upper) || upper.startsWith("SIM") || "YES".equals(upper) || upper.startsWith("YES")) {
                bOutcome = BinaryMarketBet.Outcome.YES;
            } else if ("NÃO".equals(upper) || "NAO".equals(upper) || upper.startsWith("NÃO") || upper.startsWith("NAO") || "NO".equals(upper) || upper.startsWith("NO")) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            } else if (isTeam1(upper, event)) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event)) {
                bOutcome = BinaryMarketBet.Outcome.TEAM2;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.RED_CARD, bOutcome, StatType.YELLOW_CARDS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private void handleCardsTotal(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject marketSubject = resolveCardSubject(mName, event);
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetSubject outcomeSubject = marketSubject;
            if (outcomeSubject == BetSubject.MATCH) {
                if (event != null && event.getHomeTeam() != null && upper.contains(event.getHomeTeam().toUpperCase())) {
                    outcomeSubject = BetSubject.TEAM1;
                } else if (event != null && event.getAwayTeam() != null && upper.contains(event.getAwayTeam().toUpperCase())) {
                    outcomeSubject = BetSubject.TEAM2;
                } else if (isTeam1(upper, event) && !isOver(upper) && !isUnder(upper)) {
                    outcomeSubject = BetSubject.TEAM1;
                } else if (isTeam2(upper, event) && !isOver(upper) && !isUnder(upper)) {
                    outcomeSubject = BetSubject.TEAM2;
                }
            }

            boolean outcomeAsian = isAsian || isQuarterAsian(points);
            BetType betType = null;
            if (isOver(upper)) {
                betType = mapTotalRecord("OVER", scope, outcomeSubject, StatType.YELLOW_CARDS, outcomeAsian, points);
            } else if (isUnder(upper)) {
                betType = mapTotalRecord("UNDER", scope, outcomeSubject, StatType.YELLOW_CARDS, outcomeAsian, points);
            }

            if (betType != null) {
                String baseGroup = (outcomeSubject == BetSubject.MATCH) ? "cards_total" : ("cards_total_" + outcomeSubject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsHandicap(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            boolean outcomeAsian = isAsian || isQuarterAsian(hdp);

            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = mapHandicapRecord("1", scope, StatType.YELLOW_CARDS, outcomeAsian, hdp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = mapHandicapRecord("2", scope, StatType.YELLOW_CARDS, outcomeAsian, hdp);
            }

            if (betType != null) {
                String baseGroup = outcomeAsian ? "cards_asian_handicap" : "cards_handicap";
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCards1X2(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_1x2", scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;

            if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE") || upper.equals("IGUALDADE")) {
                betType = map1X2Record("X", scope, StatType.YELLOW_CARDS);
            } else if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = map1X2Record("1", scope, StatType.YELLOW_CARDS);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = map1X2Record("2", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleFirstLastCard(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope,
                                     BinaryMarketBet.MarketType marketType, String baseGroup, List<OddItem> items) {
        String group = formatGroupName(baseGroup, scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                bOutcome = BinaryMarketBet.Outcome.TEAM2;
            } else if ("X".equals(upper) || upper.contains("EMPATE") || upper.contains("DRAW") || upper.contains("TIE")) {
                bOutcome = BinaryMarketBet.Outcome.DRAW;
            } else if (upper.contains("NONE") || upper.contains("NEITHER") || upper.contains("NO CARD") ||
                       upper.contains("NENHUM") || upper.contains("SEM CARTÃO") || upper.contains("SEM CARTAO")) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, marketType, bOutcome, StatType.YELLOW_CARDS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private void handleCardsDoubleChance(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_double_chance", scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.contains("1X") || (isHome(upper, event) && isDraw(upper))) {
                betType = map1X2DCRecord("1X", scope, StatType.YELLOW_CARDS);
            } else if (upper.contains("12") || (isHome(upper, event) && isAway(upper, event))) {
                betType = map1X2DCRecord("12", scope, StatType.YELLOW_CARDS);
            } else if (upper.contains("X2") || upper.contains("2X") || (isDraw(upper) && isAway(upper, event))) {
                betType = map1X2DCRecord("X2", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsDrawNoBet(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_draw_no_bet", scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = mapHandicapRecord("1", scope, StatType.YELLOW_CARDS, false, 0.0);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = mapHandicapRecord("2", scope, StatType.YELLOW_CARDS, false, 0.0);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsOddEven(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_odd_even", scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.contains("ODD") || upper.contains("ÍMPAR") || upper.contains("IMPAR")) {
                betType = mapOddEvenRecord("ODD", scope, StatType.YELLOW_CARDS);
            } else if (upper.contains("EVEN") || upper.contains("PAR")) {
                betType = mapOddEvenRecord("EVEN", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleMixedOutcomes(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        boolean hasOverUnder = market.getOutcomes().stream().anyMatch(o -> o.getName() != null && (isOver(o.getName()) || isUnder(o.getName())));
        if (hasOverUnder) {
            handleCardsTotal(market, event, scope, items);
            return;
        }
        boolean has1X2 = market.getOutcomes().stream().anyMatch(o -> o.getName() != null && (isTeam1(o.getName(), event) || isTeam2(o.getName(), event)));
        if (has1X2) {
            handleCards1X2(market, event, scope, items);
        }
    }

    private boolean isHome(String name, Bet7kEventDto event) {
        if (event != null && event.getHomeTeam() != null && name.contains(event.getHomeTeam().toUpperCase())) return true;
        return name.startsWith("HOME") || name.startsWith("CASA") || name.contains("1");
    }

    private boolean isAway(String name, Bet7kEventDto event) {
        if (event != null && event.getAwayTeam() != null && name.contains(event.getAwayTeam().toUpperCase())) return true;
        return name.startsWith("AWAY") || name.startsWith("FORA") || name.contains("2");
    }

    private boolean isDraw(String name) {
        return name.contains("DRAW") || name.contains("EMPATE") || name.contains(" TIE") || name.contains("X");
    }

    private BetSubject resolveCardSubject(String marketName, Bet7kEventDto event) {
        if (marketName == null) return BetSubject.MATCH;
        String upper = marketName.toUpperCase();
        if (event != null && event.getHomeTeam() != null && !event.getHomeTeam().isBlank() && upper.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event != null && event.getAwayTeam() != null && !event.getAwayTeam().isBlank() && upper.contains(event.getAwayTeam().toUpperCase())) {
            return BetSubject.TEAM2;
        }
        if (upper.contains("HOME CARD") || upper.contains("TEAM 1 CARD") ||
            upper.contains("HOME TEAM CARD") || upper.contains("CARDS - HOME") ||
            upper.contains("BOOKINGS - HOME") || upper.contains("HOME BOOKING") ||
            upper.contains("TOTAL CASA") || upper.contains("CASA TOTAL") ||
            upper.contains("CARTÕES CASA") || upper.contains("CASA CARTÕES") ||
            upper.contains("CARTOES CASA") || upper.contains("CASA CARTOES") ||
            upper.contains("CARTÕES - CASA") || upper.contains("CARTOES - CASA") ||
            upper.contains("TIME 1 CART") || upper.contains("EQUIPE 1 CART") ||
            upper.contains("CARTÕES AMARELOS - CASA") || upper.contains("CARTÕES AMARELOS CASA") ||
            upper.contains("CARTOES AMARELOS CASA")) {
            return BetSubject.TEAM1;
        }
        if (upper.contains("AWAY CARD") || upper.contains("TEAM 2 CARD") ||
            upper.contains("AWAY TEAM CARD") || upper.contains("CARDS - AWAY") ||
            upper.contains("BOOKINGS - AWAY") || upper.contains("AWAY BOOKING") ||
            upper.contains("TOTAL FORA") || upper.contains("FORA TOTAL") ||
            upper.contains("CARTÕES FORA") || upper.contains("FORA CARTÕES") ||
            upper.contains("CARTOES FORA") || upper.contains("FORA CARTOES") ||
            upper.contains("CARTÕES - FORA") || upper.contains("CARTOES - FORA") ||
            upper.contains("TIME 2 CART") || upper.contains("EQUIPE 2 CART") ||
            upper.contains("CARTÕES AMARELOS - FORA") || upper.contains("CARTÕES AMARELOS FORA") ||
            upper.contains("CARTOES AMARELOS FORA")) {
            return BetSubject.TEAM2;
        }
        return resolveSubject(marketName, event);
    }
}

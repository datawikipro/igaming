package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Card statistics markets (Cartões / Yellow Cards / Red Cards):
 * - Match Total Cards / Yellow Cards (Over / Under, Alternate Lines, Cartões Mais/Menos)
 * - Team Total Cards (Home / Away, Cartões Casa / Fora)
 * - Cards 1X2 (Winner / Most Cards / Vencedor dos Cartões / Mais Cartões)
 * - Cards Handicap (Card Spread / Handicap Asiático / Handicap Europeu)
 * - Cards Double Chance (Dupla Chance de Cartões)
 * - Cards Draw No Bet (Empate Anula Aposta - Cartões)
 * - Cards Odd / Even (Par / Ímpar de Cartões)
 * - Red Card (Cartão Vermelho / Expulsão: Sim / Não, Yes / No)
 * - First Card / Last Card (Primeiro / Último Cartão)
 * - Scopes: Full Match, 1st Half (1º Tempo), 2nd Half (2º Tempo)
 */
@Component
@Order(40)
public class CardsMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO")) return false;

        return mName.contains("CARD") ||
               mName.contains("BOOKING") ||
               mName.contains("CARTÃO") ||
               mName.contains("CARTAO") ||
               mName.contains("CARTÕES") ||
               mName.contains("CARTOES") ||
               mName.contains("AMAREL") ||
               mName.contains("VERMELH") ||
               mName.contains("EXPULS");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
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
        } else if (isTotal(mName)) {
            handleCardsTotal(market, event, scope, items);
        } else if (is1X2(mName)) {
            handleCards1X2(market, event, scope, items);
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
        return mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER") ||
               mName.contains("MAIS/MENOS") || mName.contains("MAIS / MENOS") ||
               mName.contains("ACIMA/ABAIXO") || mName.contains("ACIMA / ABAIXO") ||
               mName.contains("O/U") || mName.contains("MAIS") || mName.contains("MENOS") ||
               mName.contains("ACIMA") || mName.contains("ABAIXO");
    }

    private boolean is1X2(String mName) {
        return mName.contains("1X2") || mName.contains("WINNER") || mName.contains("VENCEDOR") ||
               mName.contains("RESULTADO") || mName.contains("MATCH") || mName.contains("PARTIDA") ||
               mName.contains("MOST") || mName.contains("MAIS CARTÕES") || mName.contains("MAIS CARTOES") ||
               mName.contains("MAIOR NÚMERO") || mName.contains("MAIOR NUMERO") ||
               mName.contains("QUEM TERÁ MAIS") || mName.contains("QUEM TERA MAIS") ||
               mName.contains("3-WAY") || mName.contains("3 WAY");
    }

    private void handleRedCard(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        boolean hasOverUnder = market.getOutcomes().stream().anyMatch(o -> o.getName() != null && (isOver(o.getName()) || isUnder(o.getName())));
        if (hasOverUnder) {
            handleCardsTotal(market, event, scope, items);
            return;
        }

        String group = formatGroupName("cards_red_card", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if ("SIM".equals(upper) || upper.startsWith("SIM") || "YES".equals(upper) || upper.startsWith("YES") || "S".equals(upper) || "Y".equals(upper)) {
                bOutcome = BinaryMarketBet.Outcome.YES;
            } else if ("NÃO".equals(upper) || "NAO".equals(upper) || upper.startsWith("NÃO") || upper.startsWith("NAO") || "NO".equals(upper) || upper.startsWith("NO") || "N".equals(upper)) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            } else if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                bOutcome = BinaryMarketBet.Outcome.TEAM2;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.RED_CARD, bOutcome, StatType.YELLOW_CARDS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private void handleCardsTotal(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject marketSubject = resolveSubject(mName, event);
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
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

            boolean outcomeAsian = isAsian || (Math.abs(points * 4) % 2 != 0);
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

    private void handleCardsHandicap(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            boolean outcomeAsian = isAsian || (Math.abs(hdp * 4) % 2 != 0);

            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = mapHandicapRecord("1", scope, StatType.YELLOW_CARDS, outcomeAsian, hdp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = mapHandicapRecord("2", scope, StatType.YELLOW_CARDS, outcomeAsian, hdp);
            }

            if (betType != null) {
                String baseGroup = outcomeAsian ? "cards_asian_handicap" : "cards_handicap";
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCards1X2(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_1x2", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;

            if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE") || upper.equals("IGUALDADE")) {
                betType = map1X2Record("X", scope, StatType.YELLOW_CARDS);
            } else if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = map1X2Record("1", scope, StatType.YELLOW_CARDS);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = map1X2Record("2", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleFirstLastCard(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope,
                                     BinaryMarketBet.MarketType marketType, String baseGroup, List<OddItem> items) {
        String group = formatGroupName(baseGroup, scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
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

    private void handleCardsDoubleChance(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_double_chance", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.contains("1X") || (isTeam1(upper, event) && (upper.contains("EMPATE") || upper.contains("DRAW")))) {
                betType = map1X2DCRecord("1X", scope, StatType.YELLOW_CARDS);
            } else if (upper.contains("12") || (isTeam1(upper, event) && isTeam2(upper, event))) {
                betType = map1X2DCRecord("12", scope, StatType.YELLOW_CARDS);
            } else if (upper.contains("X2") || upper.contains("2X") || (isTeam2(upper, event) && (upper.contains("EMPATE") || upper.contains("DRAW")))) {
                betType = map1X2DCRecord("X2", scope, StatType.YELLOW_CARDS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsDrawNoBet(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_draw_no_bet", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = mapHandicapRecord("1", scope, StatType.YELLOW_CARDS, false, 0.0);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = mapHandicapRecord("2", scope, StatType.YELLOW_CARDS, false, 0.0);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCardsOddEven(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("cards_odd_even", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
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

    private void handleMixedOutcomes(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
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

    @Override
    protected BetSubject resolveSubject(String marketName, BetesporteEventDto event) {
        if (marketName == null) return BetSubject.MATCH;
        String upper = marketName.toUpperCase();
        if (event != null && event.getHomeTeam() != null && upper.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event != null && event.getAwayTeam() != null && upper.contains(event.getAwayTeam().toUpperCase())) {
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
        return BetSubject.MATCH;
    }
}

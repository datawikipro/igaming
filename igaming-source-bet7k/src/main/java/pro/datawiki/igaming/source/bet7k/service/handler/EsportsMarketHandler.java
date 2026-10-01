package pro.datawiki.igaming.source.bet7k.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kMarketDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kOutcomeDto;

import java.util.List;

/**
 * Handler for Esports markets: CS2, Dota 2, League of Legends, Valorant, etc.
 * Supports Match Winner, Map Winner, Map Handicap, Total Maps,
 * Total Rounds, Round Handicap, Total Kills, Kill Handicap, First Blood (BinaryMarketBet),
 * with comprehensive Portuguese and English localization.
 */
@Slf4j
@Component
@Order(10)
public class EsportsMarketHandler extends AbstractBet7kMarketHandler {

    @Override
    public boolean supports(Bet7kMarketDto market, SportType sportType) {
        if (market == null) return false;
        if (sportType == SportType.BOXING || sportType == SportType.MMA) return false;

        String mName = market.getEffectiveName().toUpperCase();
        // Ignore football/basketball stats that might have similar words
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO")
                || mName.contains("CARD") || mName.contains("CARTÃO") || mName.contains("CARTAO")
                || mName.contains("FOUL") || mName.contains("PENALTY") || mName.contains("OFFSIDE")) {
            return false;
        }

        if (isEsports(sportType)) {
            return true;
        }

        return mName.contains("MAPA") || mName.contains("MAP 1") || mName.contains("MAP 2")
                || mName.contains("MAP 3") || mName.contains("MAP 4") || mName.contains("MAP 5")
                || mName.contains("1ST MAP") || mName.contains("2ND MAP") || mName.contains("3RD MAP")
                || mName.contains("FIRST BLOOD") || mName.contains("PRIMEIRO SANGUE")
                || mName.contains("PRIMEIRO ABATE") || mName.contains("1º SANGUE")
                || mName.contains("1° SANGUE") || mName.contains("ROUNDS")
                || mName.contains("RODADAS") || mName.contains("KILLS") || mName.contains("ABATES");
    }

    @Override
    public void handle(Bet7kMarketDto market, Bet7kEventDto event, SportType sportType, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
            return;
        }

        String mName = market.getEffectiveName().toUpperCase();
        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        String group = market.getGroup() != null ? market.getGroup().toUpperCase() : "";
        String category = market.getCategory() != null ? market.getCategory().toUpperCase() : "";
        String combined = (mName + " " + period + " " + group + " " + category).trim();

        BetScope scope = resolveScope(combined);

        // 1. First Blood
        if (isFirstBloodMarket(combined)) {
            handleFirstBlood(market, event, scope, items);
            return;
        }

        // 2. Kills (Total & Handicap)
        if (isKillMarket(combined)) {
            if (isHandicapMarket(combined)) {
                handleKillHandicap(market, event, scope, items);
            } else if (isTotalMarket(combined)) {
                handleKillTotal(market, event, scope, items);
            }
            return;
        }

        // 3. Rounds (Total & Handicap)
        if (isRoundMarket(combined)) {
            if (isHandicapMarket(combined)) {
                handleRoundHandicap(market, event, scope, items);
            } else if (isTotalMarket(combined)) {
                handleRoundTotal(market, event, scope, items);
            }
            return;
        }

        // 4. Map Total / Total Maps (Match Scope or explicit Maps Total)
        if (isMapTotalMarket(combined, scope)) {
            handleMapTotal(market, event, scope, items);
            return;
        }

        // 5. Map Handicap (Match Scope or explicit Maps Handicap)
        if (isMapHandicapMarket(combined, scope)) {
            handleMapHandicap(market, event, scope, items);
            return;
        }

        // 6. Map Winner (Map 1..7 scope)
        if (scope != BetScope.FULL_MATCH && isWinnerMarket(combined)) {
            handleMapWinner(market, event, scope, items);
            return;
        }

        // 7. Match Winner / Moneyline
        if (isWinnerMarket(combined) && scope == BetScope.FULL_MATCH) {
            handleMatchWinner(market, event, scope, items);
            return;
        }

        // 8. Double Chance
        if (isDoubleChanceMarket(combined)) {
            handleDoubleChance(market, event, scope, items);
            return;
        }

        // 9. Correct Score
        if (isCorrectScoreMarket(combined)) {
            handleCorrectScore(market, event, scope, items);
            return;
        }

        // 10. Fallback Total / Handicap routing based on scope and sport
        if (isTotalMarket(combined)) {
            handleFallbackTotal(market, event, scope, sportType, items);
        } else if (isHandicapMarket(combined)) {
            handleFallbackHandicap(market, event, scope, sportType, items);
        }
    }

    private boolean isFirstBloodMarket(String combined) {
        return combined.contains("FIRST BLOOD") || combined.contains("1ST BLOOD")
                || combined.contains("FIRSTBLOOD") || combined.contains("PRIMEIRO SANGUE")
                || combined.contains("1º SANGUE") || combined.contains("1° SANGUE")
                || combined.contains("PRIMEIRO ABATE") || combined.contains("1º ABATE")
                || combined.contains("1° ABATE") || combined.contains("FIRST KILL");
    }

    private boolean isKillMarket(String combined) {
        return combined.contains("KILL") || combined.contains("ABATE") || combined.contains("MORTE");
    }

    private boolean isRoundMarket(String combined) {
        return combined.contains("ROUND") || combined.contains("RODADA");
    }

    private boolean isHandicapMarket(String combined) {
        return combined.contains("HANDICAP") || combined.contains("SPREAD")
                || combined.contains("DESVANTAGEM") || combined.contains("PONTOS DE VANTAGEM")
                || combined.contains("VANTAGEM");
    }

    private boolean isTotalMarket(String combined) {
        return combined.contains("TOTAL") || combined.contains("OVER") || combined.contains("UNDER")
                || combined.contains("MAIS") || combined.contains("MENOS") || combined.contains("ACIMA")
                || combined.contains("ABAIXO") || combined.contains("O/U");
    }

    private boolean isMapTotalMarket(String combined, BetScope scope) {
        boolean hasMap = combined.contains("MAP") || combined.contains("MAPA");
        boolean hasTotal = combined.contains("TOTAL") || combined.contains("MAIS") || combined.contains("MENOS")
                || combined.contains("ACIMA") || combined.contains("ABAIXO") || combined.contains("OVER") || combined.contains("UNDER");
        boolean hasExclusions = combined.contains("ROUND") || combined.contains("RODADA") || combined.contains("KILL") || combined.contains("ABATE");
        return hasMap && hasTotal && !hasExclusions;
    }

    private boolean isMapHandicapMarket(String combined, BetScope scope) {
        boolean hasMap = combined.contains("MAP") || combined.contains("MAPA");
        boolean hasHandicap = combined.contains("HANDICAP") || combined.contains("SPREAD") || combined.contains("DESVANTAGEM");
        boolean hasExclusions = combined.contains("ROUND") || combined.contains("RODADA") || combined.contains("KILL") || combined.contains("ABATE");
        return hasMap && hasHandicap && !hasExclusions;
    }

    private boolean isWinnerMarket(String combined) {
        return combined.contains("VENCEDOR") || combined.contains("WINNER")
                || combined.contains("1X2") || combined.contains("MONEYLINE")
                || combined.contains("MATCH RESULT") || combined.contains("RESULTADO FINAL")
                || combined.contains("RESULTADO") || combined.contains("TO WIN");
    }

    private boolean isDoubleChanceMarket(String combined) {
        return combined.contains("DOUBLE CHANCE") || combined.contains("DUPLA CHANCE")
                || combined.contains("CHANCE DUPLA") || combined.contains("DUPLA HIPOTESE")
                || combined.contains("DUPLA HIPÓTESE");
    }

    private boolean isCorrectScoreMarket(String combined) {
        return combined.contains("CORRECT SCORE") || combined.contains("RESULTADO EXATO")
                || combined.contains("PLACAR EXATO") || combined.contains("SCORE EXATO")
                || combined.contains("EXACT SCORE");
    }

    private void handleFirstBlood(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("first_blood", scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            BetType betType = null;
            if (isTeam1(oName, event)) {
                betType = new BinaryMarketBet(scope, BetSubject.TEAM1, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD);
            } else if (isTeam2(oName, event)) {
                betType = new BinaryMarketBet(scope, BetSubject.TEAM2, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD);
            } else {
                String upper = oName.toUpperCase();
                if (upper.equals("SIM") || upper.equals("YES")) {
                    BetSubject subj = resolveSubject(market.getEffectiveName(), event);
                    betType = new BinaryMarketBet(scope, subj, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD);
                } else if (upper.equals("NÃO") || upper.equals("NAO") || upper.equals("NO")) {
                    BetSubject subj = resolveSubject(market.getEffectiveName(), event);
                    betType = new BinaryMarketBet(scope, subj, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.NO, StatType.FIRST_BLOOD);
                }
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleKillTotal(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String baseGroup = formatGroupName("kills_total", scope);
        handleTotalGeneric(market, event, scope, items, baseGroup, StatType.KILLS);
    }

    private void handleKillHandicap(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String baseGroup = formatGroupName("kills_handicap", scope);
        handleHandicapGeneric(market, event, scope, items, baseGroup, StatType.KILLS);
    }

    private void handleRoundTotal(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String baseGroup = formatGroupName("rounds_total", scope);
        handleTotalGeneric(market, event, scope, items, baseGroup, StatType.ROUNDS);
    }

    private void handleRoundHandicap(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String baseGroup = formatGroupName("rounds_handicap", scope);
        handleHandicapGeneric(market, event, scope, items, baseGroup, StatType.ROUNDS);
    }

    private void handleMapTotal(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String baseGroup = formatGroupName("maps_total", scope);
        handleTotalGeneric(market, event, scope, items, baseGroup, StatType.MAPS);
    }

    private void handleMapHandicap(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String baseGroup = formatGroupName("maps_handicap", scope);
        handleHandicapGeneric(market, event, scope, items, baseGroup, StatType.MAPS);
    }

    private void handleMapWinner(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("map_winner", scope);
        handleWinnerGeneric(market, event, scope, items, group);
    }

    private void handleMatchWinner(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        handleWinnerGeneric(market, event, scope, items, "moneyline");
    }

    private void handleWinnerGeneric(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items, String group) {
        boolean hasDraw = market.getOutcomes().stream().anyMatch(o -> {
            String n = o.getName() != null ? o.getName().toUpperCase() : "";
            return "X".equals(n) || n.contains("DRAW") || n.contains("EMPATE");
        });

        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            BetType betType = null;
            if (isTeam1(oName, event)) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                betType = new MatchResultBet(scope, res, StatType.MATCH);
            } else if (isTeam2(oName, event)) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                betType = new MatchResultBet(scope, res, StatType.MATCH);
            } else if ("X".equalsIgnoreCase(oName) || oName.toUpperCase().contains("DRAW") || oName.toUpperCase().contains("EMPATE")) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleDoubleChance(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("double_chance", scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.equals("1X") || upper.contains("1 OR X") || upper.contains("1 OU X")) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (upper.equals("12") || upper.contains("1 OR 2") || upper.contains("1 OU 2")) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (upper.equals("X2") || upper.equals("2X") || upper.contains("X OR 2") || upper.contains("X OU 2")) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCorrectScore(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("correct_score", scope);
        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            BetType betType = mapCorrectScoreRecord(oName, scope);
            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotalGeneric(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items, String baseGroup, StatType statType) {
        BetSubject subject = resolveSubject(market.getEffectiveName(), event);
        String finalGroup = (subject == BetSubject.MATCH) ? baseGroup : (baseGroup + "_" + subject.name().toLowerCase());

        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            TotalBet.Direction direction = null;
            if (isOver(oName)) {
                direction = TotalBet.Direction.OVER;
            } else if (isUnder(oName)) {
                direction = TotalBet.Direction.UNDER;
            }
            if (direction == null) continue;

            boolean isAsian = isQuarterAsian(points);
            TotalBet betType = new TotalBet(scope, subject, direction, points, isAsian, statType);
            addOddItem(items, finalGroup, oName, odds, betType);
        }
    }

    private void handleHandicapGeneric(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, List<OddItem> items, String baseGroup, StatType statType) {
        boolean isAsian = market.getEffectiveName().toUpperCase().contains("ASIAN") ||
                          market.getEffectiveName().toUpperCase().contains("ASIATICO") ||
                          market.getEffectiveName().toUpperCase().contains("ASIÁTICO");

        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double param = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (param == null) continue;

            HandicapBet.Outcome hcOutcome = null;
            if (isTeam1(oName, event)) {
                hcOutcome = HandicapBet.Outcome.TEAM1;
            } else if (isTeam2(oName, event)) {
                hcOutcome = HandicapBet.Outcome.TEAM2;
            } else if ("X".equalsIgnoreCase(oName) || oName.toUpperCase().contains("DRAW") || oName.toUpperCase().contains("EMPATE")) {
                hcOutcome = HandicapBet.Outcome.DRAW;
            }
            if (hcOutcome == null) continue;

            boolean asian = isAsian || isQuarterAsian(param);
            HandicapBet betType = new HandicapBet(scope, hcOutcome, param, asian, statType);
            addOddItem(items, baseGroup, oName, odds, betType);
        }
    }

    private void handleFallbackTotal(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, SportType sportType, List<OddItem> items) {
        Double firstLine = market.getOutcomes().stream()
                .map(o -> extractNumber(o.getName(), o.getHandicap(), market.getEffectiveName()))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (scope == BetScope.FULL_MATCH) {
            if (firstLine != null && firstLine <= 5.5) {
                handleMapTotal(market, event, scope, items);
            } else {
                handleRoundTotal(market, event, scope, items);
            }
        } else {
            if (sportType == SportType.DOTA2 || sportType == SportType.LEAGUE_OF_LEGENDS) {
                handleKillTotal(market, event, scope, items);
            } else {
                handleRoundTotal(market, event, scope, items);
            }
        }
    }

    private void handleFallbackHandicap(Bet7kMarketDto market, Bet7kEventDto event, BetScope scope, SportType sportType, List<OddItem> items) {
        Double firstParam = market.getOutcomes().stream()
                .map(o -> extractNumber(o.getName(), o.getHandicap(), market.getEffectiveName()))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (scope == BetScope.FULL_MATCH) {
            if (firstParam != null && Math.abs(firstParam) <= 3.5) {
                handleMapHandicap(market, event, scope, items);
            } else {
                handleRoundHandicap(market, event, scope, items);
            }
        } else {
            if (sportType == SportType.DOTA2 || sportType == SportType.LEAGUE_OF_LEGENDS) {
                handleKillHandicap(market, event, scope, items);
            } else {
                handleRoundHandicap(market, event, scope, items);
            }
        }
    }
}

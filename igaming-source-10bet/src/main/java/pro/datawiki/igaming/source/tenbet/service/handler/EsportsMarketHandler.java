package pro.datawiki.igaming.source.tenbet.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.tenbet.dto.TenBetEventDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetMarketDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetOutcomeDto;

import java.util.List;

/**
 * Dedicated OOP handler for Esports markets:
 * - Match Winner (Moneyline, 1X2)
 * - Map Winner (Map 1, Map 2, Map 3, ...)
 * - Total Maps & Map Handicap
 * - Total Rounds & Round Handicap (CS2, Valorant)
 * - Total Kills & Kill Handicap (Dota 2, LoL)
 * - First Blood (BinaryMarketBet)
 */
@Component
public class EsportsMarketHandler extends AbstractTenBetMarketHandler {

    @Override
    public boolean supports(TenBetMarketDto market, SportType sportType) {
        if (isEsports(sportType)) {
            return true;
        }
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("MAP ") || mName.contains("MAPS") ||
               mName.contains("ROUND ") || mName.contains("ROUNDS") ||
               mName.contains("GAME ") || mName.contains("GAMES") ||
               mName.contains("KILL") ||
               mName.contains("FIRST BLOOD") || mName.contains("FIRST KILL");
    }

    @Override
    public void handle(TenBetMarketDto market, TenBetEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        if (mName.contains("FIRST BLOOD") || mName.contains("FIRST KILL") || mName.contains("1ST BLOOD")) {
            handleFirstBlood(market, event, scope, items);
        } else if (mName.contains("ROUND HANDICAP") || (mName.contains("ROUND") && mName.contains("HANDICAP")) || mName.contains("ROUND SPREAD")) {
            handleRoundHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL ROUND") || mName.contains("ROUND TOTAL") ||
                   (mName.contains("ROUNDS") && (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("O/U")))) {
            handleTotalRounds(market, scope, items);
        } else if (mName.contains("KILL HANDICAP") || (mName.contains("KILL") && mName.contains("HANDICAP")) || mName.contains("KILL SPREAD")) {
            handleKillHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL KILL") || mName.contains("KILL TOTAL") ||
                   (mName.contains("KILLS") && (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("O/U")))) {
            handleTotalKills(market, scope, items);
        } else if (mName.contains("MAP HANDICAP") || mName.contains("GAME HANDICAP") || mName.contains("MAPS HANDICAP") ||
                   (mName.contains("HANDICAP") && !mName.contains("ROUND") && !mName.contains("KILL"))) {
            handleMapHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL MAP") || (mName.contains("TOTAL") && mName.contains("MAP")) ||
                   mName.contains("TOTAL GAME") || (mName.contains("TOTAL") && mName.contains("GAME")) ||
                   (mName.contains("TOTAL") && (mName.contains("OVER/UNDER") || mName.contains("O/U")) &&
                    scope == BetScope.FULL_MATCH && !mName.contains("ROUND") && !mName.contains("KILL"))) {
            handleTotalMaps(market, scope, items);
        } else if (scope != BetScope.FULL_MATCH && (mName.contains("WINNER") || mName.contains("RESULT") || mName.contains("MONEYLINE") || mName.contains("1X2"))) {
            handleMapWinner(market, event, scope, items);
        } else if (mName.contains("MATCH WINNER") || mName.contains("WINNER") || mName.contains("MONEYLINE") ||
                   mName.contains("1X2") || mName.contains("MATCH RESULT") || mName.contains("FULL TIME RESULT")) {
            handleMatchWinner(market, event, scope, items);
        }
    }

    private void handleMatchWinner(TenBetMarketDto market, TenBetEventDto event, BetScope scope, List<OddItem> items) {
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event)) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "esports_match_winner", oName, odds, betType);
            }
        }
    }

    private void handleMapWinner(TenBetMarketDto market, TenBetEventDto event, BetScope scope, List<OddItem> items) {
        String group = "esports_" + scope.name().toLowerCase() + "_winner";
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event)) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotalMaps(TenBetMarketDto market, BetScope scope, List<OddItem> items) {
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ")) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.MAPS, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ")) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.MAPS, false, points);
            }

            if (betType != null) {
                addOddItem(items, "esports_total_maps", oName, odds, betType);
            }
        }
    }

    private void handleMapHandicap(TenBetMarketDto market, TenBetEventDto event, BetScope scope, List<OddItem> items) {
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = mapHandicapRecord("1", scope, StatType.MAPS, false, hdp);
            } else if (isTeam2(upper, event)) {
                betType = mapHandicapRecord("2", scope, StatType.MAPS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, "esports_map_handicap", oName, odds, betType);
            }
        }
    }

    private void handleTotalRounds(TenBetMarketDto market, BetScope scope, List<OddItem> items) {
        String group = "esports_" + scope.name().toLowerCase() + "_total_rounds";
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ")) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.ROUNDS, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ")) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.ROUNDS, false, points);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleRoundHandicap(TenBetMarketDto market, TenBetEventDto event, BetScope scope, List<OddItem> items) {
        String group = "esports_" + scope.name().toLowerCase() + "_round_handicap";
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = mapHandicapRecord("1", scope, StatType.ROUNDS, false, hdp);
            } else if (isTeam2(upper, event)) {
                betType = mapHandicapRecord("2", scope, StatType.ROUNDS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotalKills(TenBetMarketDto market, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_total_kills" : ("esports_" + scope.name().toLowerCase() + "_total_kills");
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ")) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.KILLS, false, points);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ")) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.KILLS, false, points);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleKillHandicap(TenBetMarketDto market, TenBetEventDto event, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_kill_handicap" : ("esports_" + scope.name().toLowerCase() + "_kill_handicap");
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = mapHandicapRecord("1", scope, StatType.KILLS, false, hdp);
            } else if (isTeam2(upper, event)) {
                betType = mapHandicapRecord("2", scope, StatType.KILLS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleFirstBlood(TenBetMarketDto market, TenBetEventDto event, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_first_blood" : ("esports_" + scope.name().toLowerCase() + "_first_blood");
        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BinaryMarketBet.Outcome fbOutcome = null;
            if (isTeam1(upper, event)) {
                fbOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event)) {
                fbOutcome = BinaryMarketBet.Outcome.TEAM2;
            } else if ("YES".equals(upper) || upper.startsWith("YES") || "Y".equals(upper)) {
                fbOutcome = BinaryMarketBet.Outcome.YES;
            } else if ("NO".equals(upper) || upper.startsWith("NO") || "N".equals(upper)) {
                fbOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (fbOutcome != null) {
                BetType betType = new BinaryMarketBet(scope, BetSubject.MATCH,
                        BinaryMarketBet.MarketType.FIRST_BLOOD, fbOutcome, StatType.MATCH);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isTeam1(String outcomeName, TenBetEventDto event) {
        if (event.getHomeTeam() != null) {
            String home = event.getHomeTeam().toUpperCase();
            if (outcomeName.contains(home) || (home.length() >= 3 && outcomeName.length() >= 3 &&
                    home.contains(outcomeName.replaceAll("[^A-Z0-9 ]", "").trim()))) {
                return true;
            }
        }
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME") ||
               outcomeName.startsWith("TEAM 1") || outcomeName.startsWith("TEAM1") || outcomeName.startsWith("1 ");
    }

    private boolean isTeam2(String outcomeName, TenBetEventDto event) {
        if (event.getAwayTeam() != null) {
            String away = event.getAwayTeam().toUpperCase();
            if (outcomeName.contains(away) || (away.length() >= 3 && outcomeName.length() >= 3 &&
                    away.contains(outcomeName.replaceAll("[^A-Z0-9 ]", "").trim()))) {
                return true;
            }
        }
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY") ||
               outcomeName.startsWith("TEAM 2") || outcomeName.startsWith("TEAM2") || outcomeName.startsWith("2 ");
    }
}

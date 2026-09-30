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
 * Dedicated handler for Esports markets:
 * - Match Winner (Moneyline)
 * - Map Winner (Map 1, Map 2, ...)
 * - Total Maps & Map Handicap
 * - Total Rounds & Round Handicap
 * - First Blood
 */
@Component
public class EsportsMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
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
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        if (mName.contains("FIRST BLOOD") || mName.contains("FIRST KILL") || mName.contains("1ST BLOOD")) {
            handleFirstBlood(market, event, scope, items);
        } else if (mName.contains("ROUND HANDICAP") || (mName.contains("ROUND") && mName.contains("HANDICAP"))) {
            handleRoundHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL ROUND") || mName.contains("ROUND TOTAL") || (mName.contains("ROUNDS") && mName.contains("TOTAL"))) {
            handleTotalRounds(market, scope, items);
        } else if (mName.contains("KILL HANDICAP") || (mName.contains("KILL") && mName.contains("HANDICAP"))) {
            handleKillHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL KILL") || mName.contains("KILL TOTAL") || (mName.contains("KILLS") && (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("O/U")))) {
            handleTotalKills(market, scope, items);
        } else if (mName.contains("MAP HANDICAP") || mName.contains("GAME HANDICAP") || (mName.contains("HANDICAP") && !mName.contains("ROUND") && !mName.contains("KILL"))) {
            handleMapHandicap(market, event, scope, items);
        } else if (mName.contains("TOTAL MAP") || (mName.contains("TOTAL") && mName.contains("MAP")) ||
                   mName.contains("TOTAL GAME") || (mName.contains("TOTAL") && mName.contains("GAME"))) {
            handleTotalMaps(market, scope, items);
        } else if (scope != BetScope.FULL_MATCH && (mName.contains("WINNER") || mName.contains("RESULT") || mName.contains("MONEYLINE"))) {
            handleMapWinner(market, event, scope, items);
        } else if (mName.contains("MATCH WINNER") || mName.contains("WINNER") || mName.contains("MONEYLINE") || mName.contains("1X2")) {
            handleMatchWinner(market, event, scope, items);
        }
    }

    private void handleMatchWinner(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
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

    private void handleMapWinner(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = "esports_" + scope.name().toLowerCase() + "_winner";
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
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

    private void handleTotalMaps(BetwayMarketDto market, BetScope scope, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap());
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

    private void handleMapHandicap(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap());
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

    private void handleTotalRounds(BetwayMarketDto market, BetScope scope, List<OddItem> items) {
        String group = "esports_" + scope.name().toLowerCase() + "_total_rounds";
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap());
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

    private void handleRoundHandicap(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = "esports_" + scope.name().toLowerCase() + "_round_handicap";
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap());
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

    private void handleTotalKills(BetwayMarketDto market, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_total_kills" : ("esports_" + scope.name().toLowerCase() + "_total_kills");
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap());
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

    private void handleKillHandicap(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_kill_handicap" : ("esports_" + scope.name().toLowerCase() + "_kill_handicap");
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap());
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

    private void handleFirstBlood(BetwayMarketDto market, BetwayEventDto event, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_first_blood" : ("esports_" + scope.name().toLowerCase() + "_first_blood");
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
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

    private boolean isTeam1(String outcomeName, BetwayEventDto event) {
        if (event.getHomeTeam() != null && outcomeName.contains(event.getHomeTeam().toUpperCase())) return true;
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME") || outcomeName.startsWith("TEAM 1") || outcomeName.startsWith("TEAM1");
    }

    private boolean isTeam2(String outcomeName, BetwayEventDto event) {
        if (event.getAwayTeam() != null && outcomeName.contains(event.getAwayTeam().toUpperCase())) return true;
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY") || outcomeName.startsWith("TEAM 2") || outcomeName.startsWith("TEAM2");
    }
}

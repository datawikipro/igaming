package pro.datawiki.igaming.source.smarkets.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;

@Component
@Order(30)
public class SmarketsEsportsHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        if (mName.contains("corner") || mName.contains("card") || mName.contains("booking")) {
            return false;
        }

        if (isEsports(context.getSportType())) {
            return true;
        }

        return mName.contains("map ") || mName.contains("maps ") || mName.contains("map winner") ||
               mName.contains("kills") || mName.contains("first blood") || mName.contains("rounds") ||
               mtName.contains("esports");
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";
        String marketParam = market.getMarketType() != null ? market.getMarketType().getParam() : null;
        Double defaultParam = extractParam(marketParam, market.getName());

        BetScope scope = resolveScope(mName);
        String team1 = context.getTeam1();
        String team2 = context.getTeam2();

        // 1. First Blood
        if (mName.contains("first blood") || mName.contains("fb")) {
            handleFirstBlood(context, scope, team1, team2, items);
            return;
        }

        // 2. Kills markets (Totals / Handicaps)
        if (mName.contains("kill")) {
            handleKills(context, scope, team1, team2, defaultParam, items);
            return;
        }

        // 3. Round markets (Rounds Total / Rounds Handicap)
        if (mName.contains("round")) {
            handleRounds(context, scope, team1, team2, defaultParam, items);
            return;
        }

        // 4. Map Total (Total Maps played in match)
        if (isMapTotal(mName, mtName)) {
            handleMapTotal(context, defaultParam, items);
            return;
        }

        // 5. Map Handicap (Handicap in Maps)
        if (isMapHandicap(mName, mtName)) {
            handleMapHandicap(context, team1, team2, defaultParam, items);
            return;
        }

        // 6. Map Winner (e.g. Map 1 winner, Map 2 winner)
        if (scope != BetScope.FULL_MATCH && (mName.contains("winner") || mName.contains("result") || mtName.contains("winner") || isWinnerMarket(mName, mtName))) {
            handleMapWinner(context, scope, team1, team2, items);
            return;
        }

        // 7. General Esports markets (Totals, Handicaps, Match Winner)
        if (isEsports(context.getSportType())) {
            handleGeneralEsports(context, scope, team1, team2, defaultParam, items);
        }
    }

    private void handleFirstBlood(SmarketsMarketContext context, BetScope scope, String team1, String team2, List<OddItem> items) {
        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            BinaryMarketBet.Outcome outcome = null;
            if (isMatch(cName, team1) || cName.startsWith("1") || cName.toLowerCase().contains("home")) {
                outcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isMatch(cName, team2) || cName.startsWith("2") || cName.toLowerCase().contains("away")) {
                outcome = BinaryMarketBet.Outcome.TEAM2;
            } else if (cName.equalsIgnoreCase("yes") || cName.equalsIgnoreCase("да")) {
                outcome = BinaryMarketBet.Outcome.YES;
            } else if (cName.equalsIgnoreCase("no") || cName.equalsIgnoreCase("нет")) {
                outcome = BinaryMarketBet.Outcome.NO;
            }

            if (outcome != null) {
                BinaryMarketBet betType = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.FIRST_BLOOD, outcome, StatType.FIRST_BLOOD);
                String groupName = scope == BetScope.FULL_MATCH ? "first_blood" : ("first_blood_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), cName, backOdds, betType);
            }
        }
    }

    private void handleKills(SmarketsMarketContext context, BetScope scope, String team1, String team2, Double defaultParam, List<OddItem> items) {
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        boolean isHandicap = mName.contains("handicap") || mtName.contains("handicap") || mName.contains("spread");

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            Double param = extractParam(null, cName);
            if (param == null) param = defaultParam;

            if (isHandicap) {
                if (param == null) continue;
                HandicapBet.Outcome outcome = resolveHandicapOutcome(cName, team1, team2);
                if (outcome != null) {
                    boolean isAsian = (param % 0.5 != 0);
                    HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.KILLS);
                    String groupName = scope == BetScope.FULL_MATCH ? "kills_handicap" : ("kills_handicap_" + scope.name().toLowerCase());
                    addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
                }
            } else {
                if (param == null) continue;
                TotalBet.Direction dir = resolveTotalDirection(cName);
                if (dir != null) {
                    boolean isAsian = (param % 0.5 != 0);
                    TotalBet betType = new TotalBet(scope, BetSubject.MATCH, dir, param, isAsian, StatType.KILLS);
                    String groupName = scope == BetScope.FULL_MATCH ? "kills_total" : ("kills_total_" + scope.name().toLowerCase());
                    addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
                }
            }
        }
    }

    private void handleRounds(SmarketsMarketContext context, BetScope scope, String team1, String team2, Double defaultParam, List<OddItem> items) {
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        boolean isHandicap = mName.contains("handicap") || mtName.contains("handicap") || mName.contains("spread");

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            Double param = extractParam(null, cName);
            if (param == null) param = defaultParam;

            if (isHandicap) {
                if (param == null) continue;
                HandicapBet.Outcome outcome = resolveHandicapOutcome(cName, team1, team2);
                if (outcome != null) {
                    boolean isAsian = (param % 0.5 != 0);
                    HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.ROUNDS);
                    String groupName = scope == BetScope.FULL_MATCH ? "rounds_handicap" : ("rounds_handicap_" + scope.name().toLowerCase());
                    addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
                }
            } else {
                if (param == null) continue;
                TotalBet.Direction dir = resolveTotalDirection(cName);
                if (dir != null) {
                    boolean isAsian = (param % 0.5 != 0);
                    TotalBet betType = new TotalBet(scope, BetSubject.MATCH, dir, param, isAsian, StatType.ROUNDS);
                    String groupName = scope == BetScope.FULL_MATCH ? "rounds_total" : ("rounds_total_" + scope.name().toLowerCase());
                    addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
                }
            }
        }
    }

    private void handleMapTotal(SmarketsMarketContext context, Double defaultParam, List<OddItem> items) {
        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            Double param = extractParam(null, cName);
            if (param == null) param = defaultParam;
            if (param == null) continue;

            TotalBet.Direction dir = resolveTotalDirection(cName);
            if (dir != null) {
                boolean isAsian = (param % 0.5 != 0);
                TotalBet betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, dir, param, isAsian, StatType.MAPS);
                addOddItem(items, "maps_total", contract.getId(), cName + " (" + param + ")", backOdds, betType);
            }
        }
    }

    private void handleMapHandicap(SmarketsMarketContext context, String team1, String team2, Double defaultParam, List<OddItem> items) {
        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            Double param = extractParam(null, cName);
            if (param == null) param = defaultParam;
            if (param == null) continue;

            HandicapBet.Outcome outcome = resolveHandicapOutcome(cName, team1, team2);
            if (outcome != null) {
                boolean isAsian = (param % 0.5 != 0);
                HandicapBet betType = new HandicapBet(BetScope.FULL_MATCH, outcome, param, isAsian, StatType.MAPS);
                addOddItem(items, "maps_handicap", contract.getId(), cName + " (" + param + ")", backOdds, betType);
            }
        }
    }

    private void handleMapWinner(SmarketsMarketContext context, BetScope scope, String team1, String team2, List<OddItem> items) {
        boolean hasDraw = context.getContracts().stream().anyMatch(c -> isDraw(c.getName()));

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            MatchResultBet.Outcome outcome = null;

            if (isDraw(cName)) {
                outcome = MatchResultBet.Outcome.DRAW;
            } else if (isMatch(cName, team1) || cName.startsWith("1") || cName.toLowerCase().contains("home")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            } else if (isMatch(cName, team2) || cName.startsWith("2") || cName.toLowerCase().contains("away")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            }

            if (outcome != null) {
                MatchResultBet betType = new MatchResultBet(scope, outcome, StatType.MATCH);
                String groupName = "map_winner_" + scope.name().toLowerCase();
                addOddItem(items, groupName, contract.getId(), cName, backOdds, betType);
            }
        }
    }

    private void handleGeneralEsports(SmarketsMarketContext context, BetScope scope, String team1, String team2, Double defaultParam, List<OddItem> items) {
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        // Handicap
        if (mName.contains("handicap") || mtName.contains("handicap") || mName.contains("spread")) {
            for (SmarketsContract contract : context.getContracts()) {
                Double backOdds = context.getBestBackOdds(contract);
                if (backOdds == null || backOdds <= 1.0) continue;
                String cName = contract.getName() != null ? contract.getName().trim() : "";
                Double param = extractParam(null, cName);
                if (param == null) param = defaultParam;
                if (param == null) continue;
                HandicapBet.Outcome outcome = resolveHandicapOutcome(cName, team1, team2);
                if (outcome != null) {
                    boolean isAsian = (param % 0.5 != 0);
                    HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.MATCH);
                    String groupName = scope == BetScope.FULL_MATCH ? "handicap" : ("handicap_" + scope.name().toLowerCase());
                    addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
                }
            }
            return;
        }

        // Total
        if (mName.contains("over/under") || mName.contains("total") || mtName.contains("over_under")) {
            for (SmarketsContract contract : context.getContracts()) {
                Double backOdds = context.getBestBackOdds(contract);
                if (backOdds == null || backOdds <= 1.0) continue;
                String cName = contract.getName() != null ? contract.getName().trim() : "";
                Double param = extractParam(null, cName);
                if (param == null) param = defaultParam;
                if (param == null) continue;
                TotalBet.Direction dir = resolveTotalDirection(cName);
                if (dir != null) {
                    boolean isAsian = (param % 0.5 != 0);
                    TotalBet betType = new TotalBet(scope, BetSubject.MATCH, dir, param, isAsian, StatType.MATCH);
                    String groupName = scope == BetScope.FULL_MATCH ? "total" : ("total_" + scope.name().toLowerCase());
                    addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
                }
            }
            return;
        }

        // Match Winner
        boolean hasDraw = context.getContracts().stream().anyMatch(c -> isDraw(c.getName()));
        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;
            String cName = contract.getName() != null ? contract.getName().trim() : "";
            MatchResultBet.Outcome outcome = null;
            if (isDraw(cName)) {
                outcome = MatchResultBet.Outcome.DRAW;
            } else if (isMatch(cName, team1) || cName.startsWith("1") || cName.toLowerCase().contains("home")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            } else if (isMatch(cName, team2) || cName.startsWith("2") || cName.toLowerCase().contains("away")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            }
            if (outcome != null) {
                MatchResultBet betType = new MatchResultBet(scope, outcome, StatType.MATCH);
                String groupName = scope == BetScope.FULL_MATCH ? "match_result" : ("match_result_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), cName, backOdds, betType);
            }
        }
    }

    private boolean isMapTotal(String mName, String mtName) {
        if (!mName.contains("map")) return false;
        if (mName.contains("round") || mName.contains("kill")) return false;
        return mName.contains("total map") || mName.contains("map total") ||
               mName.contains("number of maps") || mName.contains("maps total") ||
               (mName.contains("maps") && (mName.contains("over/under") || mtName.contains("over_under") || mName.contains("total")));
    }

    private boolean isMapHandicap(String mName, String mtName) {
        if (!mName.contains("map")) return false;
        if (mName.contains("round") || mName.contains("kill")) return false;
        return mName.contains("map handicap") || mName.contains("handicap maps") ||
               mName.contains("maps handicap") ||
               (mName.contains("maps") && (mName.contains("handicap") || mtName.contains("handicap") || mName.contains("spread")));
    }

    private boolean isWinnerMarket(String mName, String mtName) {
        return mName.contains("winner") || mName.contains("match result") ||
               mtName.contains("winner") || mtName.contains("moneyline") ||
               mName.contains("moneyline");
    }

    private boolean isDraw(String name) {
        if (name == null) return false;
        String n = name.trim().toLowerCase();
        return n.equals("draw") || n.equals("tie") || n.equals("x");
    }

    private HandicapBet.Outcome resolveHandicapOutcome(String cName, String team1, String team2) {
        if (cName == null) return null;
        if (isMatch(cName, team1) || cName.startsWith("1") || cName.toLowerCase().contains("home")) {
            return HandicapBet.Outcome.TEAM1;
        }
        if (isMatch(cName, team2) || cName.startsWith("2") || cName.toLowerCase().contains("away")) {
            return HandicapBet.Outcome.TEAM2;
        }
        if (isDraw(cName)) {
            return HandicapBet.Outcome.DRAW;
        }
        return null;
    }

    private TotalBet.Direction resolveTotalDirection(String cName) {
        if (cName == null) return null;
        String upper = cName.toUpperCase().trim();
        if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.equals("OVER") || upper.equals("O")) {
            return TotalBet.Direction.OVER;
        }
        if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.equals("UNDER") || upper.equals("U")) {
            return TotalBet.Direction.UNDER;
        }
        return null;
    }
}

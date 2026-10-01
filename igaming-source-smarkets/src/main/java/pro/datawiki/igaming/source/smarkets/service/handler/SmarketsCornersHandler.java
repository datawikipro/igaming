package pro.datawiki.igaming.source.smarkets.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;

@Component
@Order(10)
public class SmarketsCornersHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";
        return mName.contains("corner") || mtName.contains("corner");
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

        // 1. First Corner / Last Corner
        if (mName.contains("first corner") || mName.contains("1st corner")) {
            handleBinaryCorner(context, scope, BinaryMarketBet.MarketType.FIRST_CORNER, "first_corner", team1, team2, items);
            return;
        }
        if (mName.contains("last corner")) {
            handleBinaryCorner(context, scope, BinaryMarketBet.MarketType.LAST_CORNER, "last_corner", team1, team2, items);
            return;
        }

        // 2. Corners 1X2 / Match Bet / Most Corners
        if (mName.contains("most corner") || mName.contains("corner match bet") ||
            mName.contains("corners 1x2") || mName.contains("corner 1x2") ||
            mName.contains("corners winner") || mName.contains("corner winner") ||
            mName.contains("corners result") || mName.contains("corner result")) {
            handleCorners1X2(context, scope, team1, team2, items);
            return;
        }

        // 3. Corners Handicap
        if (mName.contains("handicap") || mtName.contains("handicap") || mName.contains("spread")) {
            handleCornersHandicap(context, scope, team1, team2, defaultParam, items);
            return;
        }

        // 4. Corners Total (match or team totals)
        handleCornersTotal(context, scope, team1, team2, defaultParam, items);
    }

    private void handleBinaryCorner(SmarketsMarketContext context, BetScope scope, BinaryMarketBet.MarketType marketType,
                                     String baseGroup, String team1, String team2, List<OddItem> items) {
        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;
            String cName = contract.getName() != null ? contract.getName().trim() : "";
            BinaryMarketBet.Outcome outcome = null;
            if (isMatch(cName, team1) || cName.startsWith("1") || cName.toLowerCase().contains("home")) {
                outcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isMatch(cName, team2) || cName.startsWith("2") || cName.toLowerCase().contains("away")) {
                outcome = BinaryMarketBet.Outcome.TEAM2;
            } else if (cName.equalsIgnoreCase("draw") || cName.equalsIgnoreCase("neither") ||
                       cName.equalsIgnoreCase("no corner") || cName.equalsIgnoreCase("none")) {
                outcome = BinaryMarketBet.Outcome.DRAW;
            }
            if (outcome != null) {
                BinaryMarketBet betType = new BinaryMarketBet(scope, BetSubject.MATCH, marketType, outcome, StatType.CORNERS);
                String groupName = scope == BetScope.FULL_MATCH ? baseGroup : (baseGroup + "_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), cName, backOdds, betType);
            }
        }
    }

    private void handleCorners1X2(SmarketsMarketContext context, BetScope scope, String team1, String team2, List<OddItem> items) {
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
                MatchResultBet betType = new MatchResultBet(scope, outcome, StatType.CORNERS);
                String groupName = scope == BetScope.FULL_MATCH ? "corners_1x2" : ("corners_1x2_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), cName, backOdds, betType);
            }
        }
    }

    private void handleCornersHandicap(SmarketsMarketContext context, BetScope scope, String team1, String team2, Double defaultParam, List<OddItem> items) {
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
                HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.CORNERS);
                String groupName = scope == BetScope.FULL_MATCH ? "corners_handicap" : ("corners_handicap_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
            }
        }
    }

    private void handleCornersTotal(SmarketsMarketContext context, BetScope scope, String team1, String team2, Double defaultParam, List<OddItem> items) {
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";

        BetSubject subject = BetSubject.MATCH;
        if (mName.contains("home") || isMatch(mName, team1) || mName.contains("team 1")) {
            subject = BetSubject.TEAM1;
        } else if (mName.contains("away") || isMatch(mName, team2) || mName.contains("team 2")) {
            subject = BetSubject.TEAM2;
        }

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;
            String cName = contract.getName() != null ? contract.getName().trim() : "";

            BetSubject contractSubject = subject;
            if (contractSubject == BetSubject.MATCH) {
                if (cName.toLowerCase().contains("home") || isMatch(cName, team1)) {
                    contractSubject = BetSubject.TEAM1;
                } else if (cName.toLowerCase().contains("away") || isMatch(cName, team2)) {
                    contractSubject = BetSubject.TEAM2;
                }
            }

            Double param = extractParam(null, cName);
            if (param == null) param = defaultParam;
            if (param == null) continue;

            TotalBet.Direction dir = resolveTotalDirection(cName);
            if (dir != null) {
                boolean isAsian = (param % 0.5 != 0);
                TotalBet betType = new TotalBet(scope, contractSubject, dir, param, isAsian, StatType.CORNERS);
                String groupName = "corners_total";
                if (contractSubject != BetSubject.MATCH) {
                    groupName += "_" + contractSubject.name().toLowerCase();
                }
                if (scope != BetScope.FULL_MATCH) {
                    groupName += "_" + scope.name().toLowerCase();
                }
                addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
            }
        }
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

package pro.datawiki.igaming.source.wplay.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;

/**
 * Handler for Esports markets (CS2, Dota 2, League of Legends, Valorant):
 * - Match Winner / Map Winner (1X2-style)
 * - Total Maps / Total Rounds / Total Kills (Over/Under with StatType.MAPS/ROUNDS/MATCH)
 * - Map Handicap / Round Handicap
 * - First Blood / First to Kill (BinaryMarketBet YES/NO)
 * Scope: MAP_1..MAP_7, ROUND_1..ROUND_5, FULL_MATCH
 */
@Component
@Order(10)
public class EsportsMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        return isEsports(sportType);
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        // Determine market category
        boolean isFirstBlood = mName.contains("FIRST BLOOD") || mName.contains("PRIMERA SANGRE") ||
                               mName.contains("FIRST KILL") || mName.contains("PRIMERA BAJA");
        boolean isTotalRounds = mName.contains("ROUND") && (mName.contains("TOTAL") || mName.contains("OVER") ||
                                mName.contains("UNDER") || mName.contains("MÁS") || mName.contains("MAS"));
        boolean isTotalMaps = (mName.contains("MAP") || mName.contains("MAPA") || mName.contains("GAME")) &&
                              (mName.contains("TOTAL") || mName.contains("OVER") || mName.contains("UNDER") ||
                               mName.contains("MÁS") || mName.contains("MAS") || mName.contains("NÚMERO") ||
                               mName.contains("NUMERO"));
        boolean isTotalKills = mName.contains("KILL") && (mName.contains("TOTAL") || mName.contains("OVER") || mName.contains("UNDER"));
        boolean isHandicap = mName.contains("HANDICAP") || mName.contains("HÁNDICAP") || mName.contains("SPREAD") ||
                             mName.contains("VENTAJA");
        boolean isMapHandicap = isHandicap && (mName.contains("MAP") || mName.contains("MAPA") || mName.contains("GAME"));
        boolean isRoundHandicap = isHandicap && mName.contains("ROUND");
        boolean isMatchWinner = !isHandicap && !isTotalRounds && !isTotalMaps && !isTotalKills && !isFirstBlood &&
                                (mName.contains("WINNER") || mName.contains("GANADOR") || mName.contains("MATCH RESULT") ||
                                 mName.contains("MAP WINNER") || mName.contains("GANADOR DEL MAPA") ||
                                 mName.contains("MONEYLINE") || mName.contains("RESULT") ||
                                 (!mName.contains("TOTAL") && !mName.contains("ROUND") &&
                                  !mName.contains("KILL") && !mName.contains("BLOOD")));

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            if (isFirstBlood) {
                // First Blood: YES for team1, NO for team2 or vice versa; map to YES/NO binary
                BetType betType = null;
                if (isTeam1(upper, event)) {
                    betType = new BinaryMarketBet(scope, BetSubject.TEAM1, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.MATCH);
                } else if (isTeam2(upper, event)) {
                    betType = new BinaryMarketBet(scope, BetSubject.TEAM2, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.MATCH);
                } else if (upper.contains("YES") || upper.contains("SÍ") || upper.contains("SI")) {
                    betType = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.MATCH);
                } else if (upper.contains("NO")) {
                    betType = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.NO, StatType.MATCH);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("esports_first_blood", scope), oName, odds, betType);
                }

            } else if (isTotalMaps) {
                Double points = extractNumber(oName, outcome.getHandicap(), mName);
                if (points == null) continue;
                BetType betType = null;
                if (isOver(upper)) {
                    betType = mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MAPS, false, points);
                } else if (isUnder(upper)) {
                    betType = mapTotalRecord("UNDER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MAPS, false, points);
                }
                if (betType != null) {
                    addOddItem(items, "esports_total_maps", oName, odds, betType);
                }

            } else if (isTotalRounds) {
                Double points = extractNumber(oName, outcome.getHandicap(), mName);
                if (points == null) continue;
                BetType betType = null;
                if (isOver(upper)) {
                    betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.ROUNDS, false, points);
                } else if (isUnder(upper)) {
                    betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.ROUNDS, false, points);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("esports_total_rounds", scope), oName, odds, betType);
                }

            } else if (isTotalKills) {
                Double points = extractNumber(oName, outcome.getHandicap(), mName);
                if (points == null) continue;
                BetType betType = null;
                if (isOver(upper)) {
                    betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.MATCH, false, points);
                } else if (isUnder(upper)) {
                    betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.MATCH, false, points);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("esports_total_kills", scope), oName, odds, betType);
                }

            } else if (isMapHandicap) {
                Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
                if (hcp == null) continue;
                BetType betType = null;
                if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("1 ")) {
                    betType = mapHandicapRecord("1", BetScope.FULL_MATCH, StatType.MAPS, false, hcp);
                } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("2 ")) {
                    betType = mapHandicapRecord("2", BetScope.FULL_MATCH, StatType.MAPS, false, hcp);
                }
                if (betType != null) {
                    addOddItem(items, "esports_map_handicap", oName, odds, betType);
                }

            } else if (isRoundHandicap) {
                Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
                if (hcp == null) continue;
                BetType betType = null;
                if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("1 ")) {
                    betType = mapHandicapRecord("1", scope, StatType.ROUNDS, false, hcp);
                } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("2 ")) {
                    betType = mapHandicapRecord("2", scope, StatType.ROUNDS, false, hcp);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("esports_round_handicap", scope), oName, odds, betType);
                }

            } else if (isMatchWinner) {
                // Match/Map winner: map to 1X2-style (esports usually has no draw)
                BetType betType = null;
                if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("1 ")) {
                    betType = map1X2Record("1", scope, StatType.MATCH);
                } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("2 ")) {
                    betType = map1X2Record("2", scope, StatType.MATCH);
                }
                if (betType != null) {
                    String grp = (scope == BetScope.FULL_MATCH) ? "esports_match_winner" : formatGroupName("esports_map_winner", scope);
                    addOddItem(items, grp, oName, odds, betType);
                }
            }
        }
    }
}

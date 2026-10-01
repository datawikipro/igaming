package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Esports markets for Betano:
 * - Match Winner (Moneyline)
 * - Map Winner (Map 1, Map 2, ...)
 * - Total Maps & Map Handicap
 * - Total Rounds & Round Handicap
 * - First Blood
 */
@Component
public class BetanoEsportsMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) {
            return true;
        }
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("MAP ") || mName.contains("MAPS") ||
               mName.contains("ROUND ") || mName.contains("ROUNDS") ||
               mName.contains("GAME ") || mName.contains("GAMES") ||
               mName.contains("KILL") ||
               mName.contains("FIRST BLOOD") || mName.contains("FIRST KILL") ||
               mName.contains("PRIMEIRO ABATE") || mName.contains("FIRST TOWER");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        if (mName.contains("FIRST BLOOD") || mName.contains("FIRST KILL") || mName.contains("1ST BLOOD") || mName.contains("PRIMEIRO ABATE")) {
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
        } else if (scope != BetScope.FULL_MATCH && (mName.contains("WINNER") || mName.contains("RESULT") || mName.contains("MONEYLINE") || mName.contains("VENCEDOR"))) {
            handleMapWinner(market, event, scope, items);
        } else if (mName.contains("MATCH WINNER") || mName.contains("WINNER") || mName.contains("MONEYLINE") || mName.contains("1X2") || mName.contains("VENCEDOR")) {
            handleMatchWinner(market, event, scope, items);
        }
    }

    private void handleMatchWinner(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || "OT_DRAW".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "esports_match_winner", oName, odds, betType);
            }
        }
    }

    private void handleMapWinner(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = "esports_" + scope.name().toLowerCase() + "_winner";
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotalMaps(BetanoMarketDto market, BetScope scope, List<OddItem> items) {
        String group = "esports_maps_total";
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double line = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (line == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.startsWith("MAIS") || "OT_OVER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.MAPS, false, line);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.startsWith("MENOS") || "OT_UNDER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.MAPS, false, line);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleMapHandicap(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = "esports_maps_handicap";
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, StatType.MAPS, false, hdp);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, StatType.MAPS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotalRounds(BetanoMarketDto market, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("esports_rounds_total", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double line = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (line == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.startsWith("MAIS") || "OT_OVER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.ROUNDS, false, line);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.startsWith("MENOS") || "OT_UNDER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.ROUNDS, false, line);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleRoundHandicap(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("esports_rounds_handicap", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, StatType.ROUNDS, false, hdp);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, StatType.ROUNDS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotalKills(BetanoMarketDto market, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("esports_kills_total", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double line = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (line == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.startsWith("MAIS") || "OT_OVER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.KILLS, false, line);
            } else if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.startsWith("MENOS") || "OT_UNDER".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.KILLS, false, line);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleKillHandicap(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("esports_kills_handicap", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("1", scope, StatType.KILLS, false, hdp);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = mapHandicapRecord("2", scope, StatType.KILLS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleFirstBlood(BetanoMarketDto market, BetanoEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("esports_first_blood", scope);
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                bOutcome = BinaryMarketBet.Outcome.TEAM2;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.FIRST_BLOOD, bOutcome, StatType.KILLS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }
}

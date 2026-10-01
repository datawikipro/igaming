package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(5)
public class AtgEsportsHandler extends AbstractAtgMarketHandler {

    private static final Pattern MAP_PATTERN = Pattern.compile("map[\\s_-]?([1-5])", Pattern.CASE_INSENSITIVE);

    public static boolean isEsports(SportType sportType) {
        if (sportType == null) return false;
        return sportType == SportType.ESPORTS
                || sportType == SportType.CS2
                || sportType == SportType.DOTA2
                || sportType == SportType.LEAGUE_OF_LEGENDS
                || sportType == SportType.VALORANT
                || sportType == SportType.STARCRAFT
                || sportType == SportType.RAINBOW_SIX
                || sportType == SportType.ROCKET_LEAGUE
                || sportType == SportType.CALL_OF_DUTY
                || sportType == SportType.OVERWATCH
                || sportType == SportType.MOBILE_LEGENDS
                || sportType == SportType.PUBG;
    }

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        if (isEsports(sportType)) {
            return m.contains("MAP") || m.contains("ROUND") || m.contains("WINNER") || m.contains("MATCH");
        }
        return m.contains("MAP") || m.contains("ROUND");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        String mUpper = marketName != null ? marketName.toUpperCase(Locale.ROOT) : "";
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";
        Double line = outcome.getLine() != null ? outcome.getLine() : 0.0;
        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        // 1. Map Round Totals
        if (mUpper.contains("ROUND") && (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER"))) {
            BetScope scope = resolveMapScopeFromMarket(mUpper);
            if ("OT_OVER".equals(type) || label.startsWith("OVER") || label.startsWith(">")) {
                addTotal(items, outcome, marketName, runnerName, odds, scope, BetSubject.MATCH,
                        TotalBet.Direction.OVER, line, false, StatType.ROUNDS);
            } else if ("OT_UNDER".equals(type) || label.startsWith("UNDER") || label.startsWith("<")) {
                addTotal(items, outcome, marketName, runnerName, odds, scope, BetSubject.MATCH,
                        TotalBet.Direction.UNDER, line, false, StatType.ROUNDS);
            }
            return;
        }

        // 2. Map Round Handicap
        if (mUpper.contains("ROUND") && (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD"))) {
            BetScope scope = resolveMapScopeFromMarket(mUpper);
            if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM1, line, false, StatType.ROUNDS);
            } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM2, line, false, StatType.ROUNDS);
            }
            return;
        }

        // 3. Maps Total (Overall match map count)
        if ((mUpper.contains("MAPS TOTAL") || mUpper.contains("TOTAL MAPS") || mUpper.contains("NUMBER OF MAPS"))
                || (!mUpper.contains("ROUND") && mUpper.contains("MAP") && (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER")))) {
            if ("OT_OVER".equals(type) || label.startsWith("OVER") || label.startsWith(">")) {
                addTotal(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, BetSubject.MATCH,
                        TotalBet.Direction.OVER, line, false, StatType.MAPS);
            } else if ("OT_UNDER".equals(type) || label.startsWith("UNDER") || label.startsWith("<")) {
                addTotal(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, BetSubject.MATCH,
                        TotalBet.Direction.UNDER, line, false, StatType.MAPS);
            }
            return;
        }

        // 4. Maps Handicap (Overall match map handicap)
        if ((mUpper.contains("MAPS HANDICAP") || mUpper.contains("MAP HANDICAP"))
                || (!mUpper.contains("ROUND") && mUpper.contains("MAP") && (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD")))) {
            if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                addHandicap(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, line, false, StatType.MAPS);
            } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                addHandicap(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, line, false, StatType.MAPS);
            }
            return;
        }

        // 5. Individual Map Winner (Map 1, Map 2, Map 3, etc.)
        Matcher mapMatcher = MAP_PATTERN.matcher(mUpper);
        if (mapMatcher.find()) {
            int mapIdx = Integer.parseInt(mapMatcher.group(1));
            BetScope scope = resolveMapScope(mapIdx);
            if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.WIN2_2WAY, null);
            }
            return;
        }

        // 6. Match Winner for Esports (2-way)
        if (isEsports(sportType) && (mUpper.contains("MATCH") || mUpper.contains("WINNER") || mUpper.contains("MONEYLINE"))) {
            if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                addMatchResult(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                addMatchResult(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2_2WAY, null);
            }
        }
    }

    private BetScope resolveMapScopeFromMarket(String marketUpper) {
        Matcher mapMatcher = MAP_PATTERN.matcher(marketUpper);
        if (mapMatcher.find()) {
            int mapIdx = Integer.parseInt(mapMatcher.group(1));
            return resolveMapScope(mapIdx);
        }
        return BetScope.FULL_MATCH;
    }

    private BetScope resolveMapScope(int mapIdx) {
        return switch (mapIdx) {
            case 1 -> BetScope.MAP_1;
            case 2 -> BetScope.MAP_2;
            case 3 -> BetScope.MAP_3;
            case 4 -> BetScope.MAP_4;
            case 5 -> BetScope.MAP_5;
            default -> BetScope.FULL_MATCH;
        };
    }
}

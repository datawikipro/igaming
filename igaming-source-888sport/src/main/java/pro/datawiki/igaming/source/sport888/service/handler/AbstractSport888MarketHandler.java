package pro.datawiki.igaming.source.sport888.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiOutcome;

import java.util.List;

public abstract class AbstractSport888MarketHandler implements Sport888MarketHandler {

    protected Double extractDecimalOdds(KambiOutcome outcome) {
        if (outcome == null || outcome.getOdds() == null) {
            return null;
        }
        return outcome.getOdds() / 1000.0;
    }

    protected void addOddItem(List<OddItem> items, KambiOutcome outcome, String groupName, String rawOutcomeName, double value, BetType betType) {
        if (value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        String factorId = (outcome != null && outcome.getId() != null)
                ? String.valueOf(outcome.getId())
                : (groupName + "_" + rawOutcomeName.replaceAll("[^a-zA-Z0-9_+.-]", "_"));

        OddItem item = new OddItem();
        item.setFactorId(factorId);
        item.setGroupName(groupName);
        item.setName(rawOutcomeName);
        item.setValue(value);
        item.setBetType(betType);
        items.add(item);
    }

    protected void addOddItem(List<OddItem> items, String groupName, String rawOutcomeName, double value, BetType betType) {
        addOddItem(items, null, groupName, rawOutcomeName, value, betType);
    }

    protected BetScope resolveScope(String marketName) {
        if (marketName == null || marketName.isBlank()) {
            return BetScope.FULL_MATCH;
        }
        String m = marketName.toLowerCase();

        // Esports maps
        if (m.contains("map 1") || m.contains("1st map")) return BetScope.MAP_1;
        if (m.contains("map 2") || m.contains("2nd map")) return BetScope.MAP_2;
        if (m.contains("map 3") || m.contains("3rd map")) return BetScope.MAP_3;
        if (m.contains("map 4") || m.contains("4th map")) return BetScope.MAP_4;
        if (m.contains("map 5") || m.contains("5th map")) return BetScope.MAP_5;

        // Halves
        if (m.contains("1st half") || m.contains("first half") || m.contains("half 1") || m.contains("ht1")) return BetScope.HALF_1;
        if (m.contains("2nd half") || m.contains("second half") || m.contains("half 2") || m.contains("ht2")) return BetScope.HALF_2;

        // Quarters
        if (m.contains("1st quarter") || m.contains("quarter 1") || m.contains("1q")) return BetScope.QUARTER_1;
        if (m.contains("2nd quarter") || m.contains("quarter 2") || m.contains("2q")) return BetScope.QUARTER_2;
        if (m.contains("3rd quarter") || m.contains("quarter 3") || m.contains("3q")) return BetScope.QUARTER_3;
        if (m.contains("4th quarter") || m.contains("quarter 4") || m.contains("4q")) return BetScope.QUARTER_4;

        // Periods (Hockey)
        if (m.contains("1st period") || m.contains("period 1") || m.contains("1p")) return BetScope.PERIOD_1;
        if (m.contains("2nd period") || m.contains("period 2") || m.contains("2p")) return BetScope.PERIOD_2;
        if (m.contains("3rd period") || m.contains("period 3") || m.contains("3p")) return BetScope.PERIOD_3;

        // Sets (Tennis/Volleyball)
        if (m.contains("1st set") || m.contains("set 1")) return BetScope.SET_1;
        if (m.contains("2nd set") || m.contains("set 2")) return BetScope.SET_2;
        if (m.contains("3rd set") || m.contains("set 3")) return BetScope.SET_3;
        if (m.contains("4th set") || m.contains("set 4")) return BetScope.SET_4;
        if (m.contains("5th set") || m.contains("set 5")) return BetScope.SET_5;

        return BetScope.FULL_MATCH;
    }

    protected BetSubject determineTotalSubject(String marketDesc, String team1, String team2) {
        if (marketDesc == null || marketDesc.isBlank()) {
            return BetSubject.MATCH;
        }
        String lower = marketDesc.toLowerCase();

        boolean matchesTeam1 = team1 != null && !team1.isBlank() && lower.contains(team1.toLowerCase());
        boolean matchesTeam2 = team2 != null && !team2.isBlank() && lower.contains(team2.toLowerCase());

        if (matchesTeam1 && !matchesTeam2) {
            return BetSubject.TEAM1;
        }
        if (matchesTeam2 && !matchesTeam1) {
            return BetSubject.TEAM2;
        }

        if (lower.contains("home team") || lower.contains("home total") || lower.startsWith("home ") || lower.contains("team 1")) {
            return BetSubject.TEAM1;
        }
        if (lower.contains("away team") || lower.contains("away total") || lower.startsWith("away ") || lower.contains("team 2")) {
            return BetSubject.TEAM2;
        }

        return BetSubject.MATCH;
    }

    public static boolean isStats(String marketName) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase();
        return m.contains("CORNER") || m.contains("CARD") || m.contains("BOOKING") || m.contains("FOUL") || m.contains("OFFSIDE");
    }

    public static boolean isEsports(SportType sportType) {
        if (sportType == null) return false;
        return switch (sportType) {
            case ESPORTS, CS2, DOTA2, LEAGUE_OF_LEGENDS, VALORANT,
                 RAINBOW_SIX, ROCKET_LEAGUE, CALL_OF_DUTY, OVERWATCH,
                 PUBG, MOBILE_LEGENDS, CROSSFIRE, STARCRAFT,
                 CYBER_FOOTBALL, CYBER_BASKETBALL, CYBER_HOCKEY -> true;
            default -> false;
        };
    }
}

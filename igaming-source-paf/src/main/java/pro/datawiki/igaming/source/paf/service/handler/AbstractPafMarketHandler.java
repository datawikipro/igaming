package pro.datawiki.igaming.source.paf.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;

public abstract class AbstractPafMarketHandler implements PafMarketHandler {

    protected Double extractDecimalOdds(KambiOutcome outcome) {
        if (outcome == null || outcome.getOdds() == null) {
            return null;
        }
        return outcome.getOdds() / 1000.0;
    }

    protected Double normalizeLine(Double rawLine) {
        if (rawLine == null) {
            return null;
        }
        if (Math.abs(rawLine) >= 50.0) {
            return rawLine / 1000.0;
        }
        return rawLine;
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
        if (m.contains("map 1") || m.contains("1st map") || m.contains("kartta 1") || m.contains("1. kartta")) return BetScope.MAP_1;
        if (m.contains("map 2") || m.contains("2nd map") || m.contains("kartta 2") || m.contains("2. kartta")) return BetScope.MAP_2;
        if (m.contains("map 3") || m.contains("3rd map") || m.contains("kartta 3") || m.contains("3. kartta")) return BetScope.MAP_3;
        if (m.contains("map 4") || m.contains("4th map") || m.contains("kartta 4") || m.contains("4. kartta")) return BetScope.MAP_4;
        if (m.contains("map 5") || m.contains("5th map") || m.contains("kartta 5") || m.contains("5. kartta")) return BetScope.MAP_5;

        // Halves
        if (m.contains("1st half") || m.contains("first half") || m.contains("half 1") || m.contains("ht1")
                || m.contains("1. puoliaika") || m.contains("1. puoliaikaa") || m.contains("1º tempo") || m.contains("1. halvlek")) return BetScope.HALF_1;
        if (m.contains("2nd half") || m.contains("second half") || m.contains("half 2") || m.contains("ht2")
                || m.contains("2. puoliaika") || m.contains("2. puoliaikaa") || m.contains("2º tempo") || m.contains("2. halvlek")) return BetScope.HALF_2;

        // Quarters
        if (m.contains("1st quarter") || m.contains("quarter 1") || m.contains("1q") || m.contains("1. neljännes") || m.contains("1. kvartal")) return BetScope.QUARTER_1;
        if (m.contains("2nd quarter") || m.contains("quarter 2") || m.contains("2q") || m.contains("2. neljännes") || m.contains("2. kvartal")) return BetScope.QUARTER_2;
        if (m.contains("3rd quarter") || m.contains("quarter 3") || m.contains("3q") || m.contains("3. neljännes") || m.contains("3. kvartal")) return BetScope.QUARTER_3;
        if (m.contains("4th quarter") || m.contains("quarter 4") || m.contains("4q") || m.contains("4. neljännes") || m.contains("4. kvartal")) return BetScope.QUARTER_4;

        // Periods (Hockey)
        if (m.contains("1st period") || m.contains("period 1") || m.contains("1p") || m.contains("1. erä") || m.contains("1. period")) return BetScope.PERIOD_1;
        if (m.contains("2nd period") || m.contains("period 2") || m.contains("2p") || m.contains("2. erä") || m.contains("2. period")) return BetScope.PERIOD_2;
        if (m.contains("3rd period") || m.contains("period 3") || m.contains("3p") || m.contains("3. erä") || m.contains("3. period")) return BetScope.PERIOD_3;

        // Sets (Tennis/Volleyball)
        if (m.contains("1st set") || m.contains("set 1") || m.contains("1. erä (setti)") || m.contains("1. setti") || m.contains("1. set")) return BetScope.SET_1;
        if (m.contains("2nd set") || m.contains("set 2") || m.contains("2. erä (setti)") || m.contains("2. setti") || m.contains("2. set")) return BetScope.SET_2;
        if (m.contains("3rd set") || m.contains("set 3") || m.contains("3. erä (setti)") || m.contains("3. setti") || m.contains("3. set")) return BetScope.SET_3;
        if (m.contains("4th set") || m.contains("set 4") || m.contains("4. erä (setti)") || m.contains("4. setti") || m.contains("4. set")) return BetScope.SET_4;
        if (m.contains("5th set") || m.contains("set 5") || m.contains("5. erä (setti)") || m.contains("5. setti") || m.contains("5. set")) return BetScope.SET_5;

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

        if (lower.contains("home team") || lower.contains("home total") || lower.startsWith("home ")
                || lower.contains("team 1") || lower.contains("kotijoukkue") || lower.contains("koti")
                || lower.contains("hemmalag") || lower.contains("casa")) {
            return BetSubject.TEAM1;
        }
        if (lower.contains("away team") || lower.contains("away total") || lower.startsWith("away ")
                || lower.contains("team 2") || lower.contains("vierasjoukkue") || lower.contains("vieras")
                || lower.contains("bortalag") || lower.contains("fora")) {
            return BetSubject.TEAM2;
        }

        return BetSubject.MATCH;
    }

    public static boolean isStats(String marketName) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase();
        return m.contains("CORNER") || m.contains("KULMAPOTKU") || m.contains("HÖRN") || m.contains("ESCANT")
                || m.contains("CARD") || m.contains("KORTTI") || m.contains("VARNING") || m.contains("CART")
                || m.contains("BOOKING") || m.contains("FOUL") || m.contains("VIRHE") || m.contains("OFFSIDE")
                || m.contains("PAITSIO");
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

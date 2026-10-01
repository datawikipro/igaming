package pro.datawiki.igaming.source.bcgame.service.handler;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractBcgameMarketHandler implements BcgameMarketHandler {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");

    protected void addOddItem(List<OddItem> items, String factorId, String groupName, String outcomeName, Double value, BetType betType) {
        if (value == null || value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        String safeName = outcomeName != null ? outcomeName : betType.code();
        item.setFactorId(factorId != null ? factorId : (groupName + "_" + safeName.replace(" ", "_")));
        item.setGroupName(groupName);
        item.setName(safeName);
        item.setValue(value);
        item.setBetType(betType);
        items.add(item);
    }

    protected void addOddItem(List<OddItem> items, BcgameOutcomeDto outcome, String groupName, BetType betType) {
        if (outcome == null) return;
        Double price = outcome.getEffectiveOdds();
        addOddItem(items, outcome.getId(), groupName, outcome.getName(), price, betType);
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

    public static boolean isStats(String marketName) {
        if (marketName == null) return false;
        String m = marketName.toLowerCase();
        return m.contains("corner") || m.contains("card") || m.contains("booking")
                || m.contains("yellow") || m.contains("foul") || m.contains("offside")
                || m.contains("shot");
    }

    protected BetScope resolveScope(String marketName, SportType sportType) {
        if (marketName == null) return BetScope.FULL_MATCH;
        String m = marketName.toLowerCase();

        boolean esport = isEsports(sportType);

        // Maps (Esports)
        if (m.contains("map 1") || m.contains("map1") || m.contains("1st map") || m.contains("first map")) return BetScope.MAP_1;
        if (m.contains("map 2") || m.contains("map2") || m.contains("2nd map") || m.contains("second map")) return BetScope.MAP_2;
        if (m.contains("map 3") || m.contains("map3") || m.contains("3rd map") || m.contains("third map")) return BetScope.MAP_3;
        if (m.contains("map 4") || m.contains("map4") || m.contains("4th map") || m.contains("fourth map")) return BetScope.MAP_4;
        if (m.contains("map 5") || m.contains("map5") || m.contains("5th map") || m.contains("fifth map")) return BetScope.MAP_5;
        if (m.contains("map 6") || m.contains("map6")) return BetScope.MAP_6;
        if (m.contains("map 7") || m.contains("map7")) return BetScope.MAP_7;

        // Rounds
        if (m.contains("round 1") || m.contains("round1") || m.contains("1st round")) return BetScope.ROUND_1;
        if (m.contains("round 2") || m.contains("round2") || m.contains("2nd round")) return BetScope.ROUND_2;
        if (m.contains("round 3") || m.contains("round3") || m.contains("3rd round")) return BetScope.ROUND_3;
        if (m.contains("round 4") || m.contains("round4") || m.contains("4th round")) return BetScope.ROUND_4;
        if (m.contains("round 5") || m.contains("round5") || m.contains("5th round")) return BetScope.ROUND_5;

        // Halves
        if (m.contains("1st half") || m.contains("1 half") || m.contains("half 1") || m.contains("first half") || m.contains("ht1")
                || m.contains("1-й тайм") || m.contains("1 тайм") || m.contains("первый тайм") || m.contains("тайм 1")) {
            return BetScope.HALF_1;
        }
        if (m.contains("2nd half") || m.contains("2 half") || m.contains("half 2") || m.contains("second half") || m.contains("ht2")
                || m.contains("2-й тайм") || m.contains("2 тайм") || m.contains("второй тайм") || m.contains("тайм 2")) {
            return BetScope.HALF_2;
        }

        // Periods / Quarters / Sets
        if (m.contains("1st period") || m.contains("period 1") || m.contains("1st quarter") || m.contains("quarter 1") || m.contains("1st set") || m.contains("set 1")
                || m.contains("1-й период") || m.contains("1 период") || m.contains("период 1")
                || m.contains("1-я четверть") || m.contains("1 четверть") || m.contains("четверть 1")
                || m.contains("1-й сет") || m.contains("1 сет") || m.contains("сет 1")) {
            return esport ? BetScope.MAP_1 : BetScope.PERIOD_1;
        }
        if (m.contains("2nd period") || m.contains("period 2") || m.contains("2nd quarter") || m.contains("quarter 2") || m.contains("2nd set") || m.contains("set 2")
                || m.contains("2-й период") || m.contains("2 период") || m.contains("период 2")
                || m.contains("2-я четверть") || m.contains("2 четверть") || m.contains("четверть 2")
                || m.contains("2-й сет") || m.contains("2 сет") || m.contains("сет 2")) {
            return esport ? BetScope.MAP_2 : BetScope.PERIOD_2;
        }
        if (m.contains("3rd period") || m.contains("period 3") || m.contains("3rd quarter") || m.contains("quarter 3") || m.contains("3rd set") || m.contains("set 3")
                || m.contains("3-й период") || m.contains("3 период") || m.contains("период 3")
                || m.contains("3-я четверть") || m.contains("3 четверть") || m.contains("четверть 3")
                || m.contains("3-й сет") || m.contains("3 сет") || m.contains("сет 3")) {
            return esport ? BetScope.MAP_3 : BetScope.PERIOD_3;
        }
        if (m.contains("4th period") || m.contains("period 4") || m.contains("4th quarter") || m.contains("quarter 4") || m.contains("4th set") || m.contains("set 4")
                || m.contains("4-й период") || m.contains("4 период") || m.contains("период 4")
                || m.contains("4-я четверть") || m.contains("4 четверть") || m.contains("четверть 4")
                || m.contains("4-й сет") || m.contains("4 сет") || m.contains("сет 4")) {
            return esport ? BetScope.MAP_4 : BetScope.PERIOD_4;
        }
        if (m.contains("5th period") || m.contains("period 5") || m.contains("5th set") || m.contains("set 5")
                || m.contains("5-й период") || m.contains("5 период") || m.contains("период 5")
                || m.contains("5-й сет") || m.contains("5 сет") || m.contains("сет 5")) {
            return esport ? BetScope.MAP_5 : BetScope.PERIOD_5;
        }

        return BetScope.FULL_MATCH;
    }

    protected Double extractParam(String text) {
        if (text == null) return null;
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    protected Double resolveParam(BcgameOutcomeDto outcome, String marketName) {
        if (outcome != null && outcome.getEffectiveParam() != null) {
            return outcome.getEffectiveParam();
        }
        if (outcome != null && outcome.getName() != null) {
            Double p = extractParam(outcome.getName());
            if (p != null) return p;
        }
        return extractParam(marketName);
    }
}

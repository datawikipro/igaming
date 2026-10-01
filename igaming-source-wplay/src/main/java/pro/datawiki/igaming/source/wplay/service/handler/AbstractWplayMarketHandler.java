package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractWplayMarketHandler implements WplayMarketHandler {

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

    protected void addOddItem(List<OddItem> items, WplayOutcomeDto outcome, String groupName, BetType betType) {
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
        return m.contains("corner") || m.contains("córner") || m.contains("esquina")
                || m.contains("card") || m.contains("tarjeta") || m.contains("booking")
                || m.contains("yellow") || m.contains("amarilla") || m.contains("foul") || m.contains("falta")
                || m.contains("offside") || m.contains("fuera de juego") || m.contains("fuera de lugar")
                || m.contains("shot") || m.contains("tiro") || m.contains("disparo") || m.contains("sot");
    }

    protected BetScope resolveScope(String marketName, SportType sportType) {
        if (marketName == null) return BetScope.FULL_MATCH;
        String m = marketName.toLowerCase();
        boolean esport = isEsports(sportType);

        // Esports Maps
        if (m.contains("map 1") || m.contains("mapa 1") || m.contains("1st map") || m.contains("primer mapa") || m.contains("1er mapa")) return BetScope.MAP_1;
        if (m.contains("map 2") || m.contains("mapa 2") || m.contains("2nd map") || m.contains("segundo mapa") || m.contains("2do mapa")) return BetScope.MAP_2;
        if (m.contains("map 3") || m.contains("mapa 3") || m.contains("3rd map") || m.contains("tercer mapa") || m.contains("3er mapa")) return BetScope.MAP_3;
        if (m.contains("map 4") || m.contains("mapa 4") || m.contains("4th map") || m.contains("cuarto mapa") || m.contains("4to mapa")) return BetScope.MAP_4;
        if (m.contains("map 5") || m.contains("mapa 5") || m.contains("5th map") || m.contains("quinto mapa") || m.contains("5to mapa")) return BetScope.MAP_5;
        if (m.contains("map 6") || m.contains("mapa 6") || m.contains("6th map")) return BetScope.MAP_6;
        if (m.contains("map 7") || m.contains("mapa 7") || m.contains("7th map")) return BetScope.MAP_7;

        // Rounds
        if (m.contains("round 1") || m.contains("ronda 1") || m.contains("1st round")) return BetScope.ROUND_1;
        if (m.contains("round 2") || m.contains("ronda 2") || m.contains("2nd round")) return BetScope.ROUND_2;
        if (m.contains("round 3") || m.contains("ronda 3") || m.contains("3rd round")) return BetScope.ROUND_3;
        if (m.contains("round 4") || m.contains("ronda 4") || m.contains("4th round")) return BetScope.ROUND_4;
        if (m.contains("round 5") || m.contains("ronda 5") || m.contains("5th round")) return BetScope.ROUND_5;

        // Halves
        if (m.contains("1st half") || m.contains("1 half") || m.contains("first half") || m.contains("half 1")
                || m.contains("1er tiempo") || m.contains("1-er tiempo") || m.contains("primer tiempo")
                || m.contains("1er-tiempo") || m.contains("1 mitad") || m.contains("primera mitad") || m.contains("ht1")) {
            return BetScope.HALF_1;
        }
        if (m.contains("2nd half") || m.contains("2 half") || m.contains("second half") || m.contains("half 2")
                || m.contains("2do tiempo") || m.contains("2-do tiempo") || m.contains("segundo tiempo")
                || m.contains("2do-tiempo") || m.contains("2 mitad") || m.contains("segunda mitad") || m.contains("ht2")) {
            return BetScope.HALF_2;
        }

        // Periods / Quarters / Sets
        if (m.contains("1st period") || m.contains("period 1") || m.contains("1st quarter") || m.contains("quarter 1")
                || m.contains("1st set") || m.contains("set 1") || m.contains("1er periodo") || m.contains("1er cuarto")
                || m.contains("primer cuarto") || m.contains("1er set")) {
            return esport ? BetScope.MAP_1 : BetScope.PERIOD_1;
        }
        if (m.contains("2nd period") || m.contains("period 2") || m.contains("2nd quarter") || m.contains("quarter 2")
                || m.contains("2nd set") || m.contains("set 2") || m.contains("2do periodo") || m.contains("2do cuarto")
                || m.contains("segundo cuarto") || m.contains("2do set")) {
            return esport ? BetScope.MAP_2 : BetScope.PERIOD_2;
        }
        if (m.contains("3rd period") || m.contains("period 3") || m.contains("3rd quarter") || m.contains("quarter 3")
                || m.contains("3rd set") || m.contains("set 3") || m.contains("3er periodo") || m.contains("3er cuarto")
                || m.contains("tercer cuarto") || m.contains("3er set")) {
            return esport ? BetScope.MAP_3 : BetScope.PERIOD_3;
        }
        if (m.contains("4th period") || m.contains("period 4") || m.contains("4th quarter") || m.contains("quarter 4")
                || m.contains("4th set") || m.contains("set 4") || m.contains("4to periodo") || m.contains("4to cuarto")
                || m.contains("cuarto cuarto") || m.contains("4to set")) {
            return esport ? BetScope.MAP_4 : BetScope.PERIOD_4;
        }
        if (m.contains("5th period") || m.contains("period 5") || m.contains("5th set") || m.contains("set 5")
                || m.contains("5to periodo") || m.contains("5to set")) {
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

    protected Double resolveParam(WplayOutcomeDto outcome, String marketName) {
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

package pro.datawiki.igaming.source.digitain.strategy;

import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;

import java.util.Locale;

public class DigitainScopeResolver {

    public static BetScope resolve(String groupNameEn, String groupNameRu, SportType sportType) {
        String text = ((groupNameEn != null ? groupNameEn : "") + " " + (groupNameRu != null ? groupNameRu : ""))
                .toLowerCase(Locale.ROOT);

        // Halves
        if (text.contains("1st half") || text.contains("1-й тайм") || text.contains("1-ый тайм")
                || text.contains("1й тайм") || text.contains("первый тайм") || text.contains("1 half")) {
            return BetScope.HALF_1;
        }
        if (text.contains("2nd half") || text.contains("2-й тайм") || text.contains("2-ой тайм")
                || text.contains("2й тайм") || text.contains("второй тайм") || text.contains("2 half")) {
            return BetScope.HALF_2;
        }

        // Quarters (Basketball, American Football)
        if (text.contains("1st quarter") || text.contains("1-я четверть") || text.contains("1 четверть") || text.contains("1st qtr")) {
            return BetScope.QUARTER_1;
        }
        if (text.contains("2nd quarter") || text.contains("2-я четверть") || text.contains("2 четверть") || text.contains("2nd qtr")) {
            return BetScope.QUARTER_2;
        }
        if (text.contains("3rd quarter") || text.contains("3-я четверть") || text.contains("3 четверть") || text.contains("3rd qtr")) {
            return BetScope.QUARTER_3;
        }
        if (text.contains("4th quarter") || text.contains("4-я четверть") || text.contains("4 четверть") || text.contains("4th qtr")) {
            return BetScope.QUARTER_4;
        }

        // Periods (Ice Hockey)
        if (text.contains("1st period") || text.contains("1-й период") || text.contains("1 период")) {
            return BetScope.PERIOD_1;
        }
        if (text.contains("2nd period") || text.contains("2-й период") || text.contains("2 период")) {
            return BetScope.PERIOD_2;
        }
        if (text.contains("3rd period") || text.contains("3-й период") || text.contains("3 период")) {
            return BetScope.PERIOD_3;
        }

        // Sets (Tennis, Volleyball, Table Tennis)
        if (text.contains("1st set") || text.contains("1-й сет") || text.contains("1-я партия") || text.contains("1 сет")) {
            return BetScope.SET_1;
        }
        if (text.contains("2nd set") || text.contains("2-й сет") || text.contains("2-я партия") || text.contains("2 сет")) {
            return BetScope.SET_2;
        }
        if (text.contains("3rd set") || text.contains("3-й сет") || text.contains("3-я партия") || text.contains("3 сет")) {
            return BetScope.SET_3;
        }

        return BetScope.FULL_MATCH;
    }
}

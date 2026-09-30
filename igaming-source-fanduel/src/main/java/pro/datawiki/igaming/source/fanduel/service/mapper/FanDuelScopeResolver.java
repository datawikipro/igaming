package pro.datawiki.igaming.source.fanduel.service.mapper;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.market.BetScope;

@Component
public class FanDuelScopeResolver {

    public BetScope resolve(String marketName) {
        if (marketName == null || marketName.isBlank()) {
            return BetScope.FULL_MATCH;
        }
        String lower = marketName.toLowerCase();

        // 1. Halves
        if (lower.contains("1st half") || lower.contains("first half") || lower.contains("1h ") || lower.contains("half 1")) {
            return BetScope.FIRST_HALF;
        }
        if (lower.contains("2nd half") || lower.contains("second half") || lower.contains("2h ") || lower.contains("half 2")) {
            return BetScope.SECOND_HALF;
        }

        // 2. Quarters
        if (lower.contains("1st quarter") || lower.contains("1q ") || lower.contains("first quarter") || lower.contains("quarter 1")) {
            return BetScope.QUARTER_1;
        }
        if (lower.contains("2nd quarter") || lower.contains("2q ") || lower.contains("second quarter") || lower.contains("quarter 2")) {
            return BetScope.QUARTER_2;
        }
        if (lower.contains("3rd quarter") || lower.contains("3q ") || lower.contains("third quarter") || lower.contains("quarter 3")) {
            return BetScope.QUARTER_3;
        }
        if (lower.contains("4th quarter") || lower.contains("4q ") || lower.contains("fourth quarter") || lower.contains("quarter 4")) {
            return BetScope.QUARTER_4;
        }

        // 3. Periods (Hockey)
        if (lower.contains("1st period") || lower.contains("1p ") || lower.contains("first period") || lower.contains("period 1")) {
            return BetScope.PERIOD_1;
        }
        if (lower.contains("2nd period") || lower.contains("2p ") || lower.contains("second period") || lower.contains("period 2")) {
            return BetScope.PERIOD_2;
        }
        if (lower.contains("3rd period") || lower.contains("3p ") || lower.contains("third period") || lower.contains("period 3")) {
            return BetScope.PERIOD_3;
        }

        // 4. Sets (Tennis / Volleyball)
        if (lower.contains("set 1") || lower.contains("1st set") || lower.contains("first set")) {
            return BetScope.SET_1;
        }
        if (lower.contains("set 2") || lower.contains("2nd set") || lower.contains("second set")) {
            return BetScope.SET_2;
        }
        if (lower.contains("set 3") || lower.contains("3rd set") || lower.contains("third set")) {
            return BetScope.SET_3;
        }
        if (lower.contains("set 4") || lower.contains("4th set") || lower.contains("fourth set")) {
            return BetScope.SET_4;
        }
        if (lower.contains("set 5") || lower.contains("5th set") || lower.contains("fifth set")) {
            return BetScope.SET_5;
        }

        // 5. Maps (Esports)
        if (lower.contains("map 1") || lower.contains("1st map")) {
            return BetScope.MAP_1;
        }
        if (lower.contains("map 2") || lower.contains("2nd map")) {
            return BetScope.MAP_2;
        }
        if (lower.contains("map 3") || lower.contains("3rd map")) {
            return BetScope.MAP_3;
        }
        if (lower.contains("map 4") || lower.contains("4th map")) {
            return BetScope.MAP_4;
        }
        if (lower.contains("map 5") || lower.contains("5th map")) {
            return BetScope.MAP_5;
        }

        // 6. Innings (Baseball)
        if (lower.contains("first 5 innings") || lower.contains("1st 5 innings") || lower.contains("f5")) {
            return BetScope.FIRST_5_INNINGS;
        }
        if (lower.contains("1st inning") || lower.contains("first inning")) {
            return BetScope.INNING_1;
        }
        if (lower.contains("2nd inning")) {
            return BetScope.INNING_2;
        }
        if (lower.contains("3rd inning")) {
            return BetScope.INNING_3;
        }

        // 7. Extra time / Overtime
        if (lower.contains("extra time")) {
            return BetScope.EXTRA_TIME;
        }
        if (lower.contains("overtime") || lower.contains("incl. ot") || lower.contains("including overtime")) {
            return BetScope.FULL_MATCH_INCLUDING_OT;
        }

        return BetScope.FULL_MATCH;
    }
}

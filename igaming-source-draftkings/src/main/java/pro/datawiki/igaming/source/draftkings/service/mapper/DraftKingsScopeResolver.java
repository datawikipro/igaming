package pro.datawiki.igaming.source.draftkings.service.mapper;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.market.BetScope;

@Component
public class DraftKingsScopeResolver {

    public BetScope resolve(String categoryName, String marketName) {
        String combined = ((categoryName != null ? categoryName : "") + " " + (marketName != null ? marketName : "")).toLowerCase();

        // 1. Halves
        if (combined.contains("1st half") || combined.contains("first half") || combined.contains("1h ")) {
            return BetScope.FIRST_HALF;
        }
        if (combined.contains("2nd half") || combined.contains("second half") || combined.contains("2h ")) {
            return BetScope.SECOND_HALF;
        }

        // 2. Quarters
        if (combined.contains("1st quarter") || combined.contains("1q ") || combined.contains("first quarter")) {
            return BetScope.QUARTER_1;
        }
        if (combined.contains("2nd quarter") || combined.contains("2q ") || combined.contains("second quarter")) {
            return BetScope.QUARTER_2;
        }
        if (combined.contains("3rd quarter") || combined.contains("3q ") || combined.contains("third quarter")) {
            return BetScope.QUARTER_3;
        }
        if (combined.contains("4th quarter") || combined.contains("4q ") || combined.contains("fourth quarter")) {
            return BetScope.QUARTER_4;
        }

        // 3. Periods (Hockey)
        if (combined.contains("1st period") || combined.contains("1p ") || combined.contains("first period")) {
            return BetScope.PERIOD_1;
        }
        if (combined.contains("2nd period") || combined.contains("2p ") || combined.contains("second period")) {
            return BetScope.PERIOD_2;
        }
        if (combined.contains("3rd period") || combined.contains("3p ") || combined.contains("third period")) {
            return BetScope.PERIOD_3;
        }

        // 4. Sets (Tennis / Volleyball)
        if (combined.contains("set 1") || combined.contains("1st set")) {
            return BetScope.SET_1;
        }
        if (combined.contains("set 2") || combined.contains("2nd set")) {
            return BetScope.SET_2;
        }
        if (combined.contains("set 3") || combined.contains("3rd set")) {
            return BetScope.SET_3;
        }
        if (combined.contains("set 4") || combined.contains("4th set")) {
            return BetScope.SET_4;
        }
        if (combined.contains("set 5") || combined.contains("5th set")) {
            return BetScope.SET_5;
        }

        // 5. Maps (Esports)
        if (combined.contains("map 1") || combined.contains("1st map")) {
            return BetScope.MAP_1;
        }
        if (combined.contains("map 2") || combined.contains("2nd map")) {
            return BetScope.MAP_2;
        }
        if (combined.contains("map 3") || combined.contains("3rd map")) {
            return BetScope.MAP_3;
        }
        if (combined.contains("map 4") || combined.contains("4th map")) {
            return BetScope.MAP_4;
        }
        if (combined.contains("map 5") || combined.contains("5th map")) {
            return BetScope.MAP_5;
        }

        // 6. Innings (Baseball)
        if (combined.contains("first 5 innings") || combined.contains("1st 5 innings") || combined.contains("f5")) {
            return BetScope.FIRST_5_INNINGS;
        }
        if (combined.contains("1st inning") || combined.contains("first inning")) {
            return BetScope.INNING_1;
        }
        if (combined.contains("2nd inning")) {
            return BetScope.INNING_2;
        }
        if (combined.contains("3rd inning")) {
            return BetScope.INNING_3;
        }

        // 7. Extra time / Overtime
        if (combined.contains("extra time")) {
            return BetScope.EXTRA_TIME;
        }
        if (combined.contains("overtime") || combined.contains("incl. ot") || combined.contains("including overtime")) {
            return BetScope.FULL_MATCH_INCLUDING_OT;
        }

        return BetScope.FULL_MATCH;
    }
}

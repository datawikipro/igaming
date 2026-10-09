package pro.datawiki.igaming.source.betano.strategy;

import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;

import java.util.Locale;

public class BetanoScopeResolver {

    public static BetScope resolve(String marketName, String marketType, SportType sportType) {
        String text = ((marketName != null ? marketName : "") + " " + (marketType != null ? marketType : ""))
                .toLowerCase(Locale.ROOT);

        // Halves
        if (text.contains("1º tempo") || text.contains("1° tempo") || text.contains("primeiro tempo")
                || text.contains("1st half") || text.contains("first half") || text.contains("1h") || text.contains("half_1")) {
            return BetScope.HALF_1;
        }
        if (text.contains("2º tempo") || text.contains("2° tempo") || text.contains("segundo tempo")
                || text.contains("2nd half") || text.contains("second half") || text.contains("2h") || text.contains("half_2")) {
            return BetScope.HALF_2;
        }

        // Quarters (Basketball, NFL)
        if (text.contains("1º quarto") || text.contains("1° quarto") || text.contains("1st quarter") || text.contains("q1")) {
            return BetScope.QUARTER_1;
        }
        if (text.contains("2º quarto") || text.contains("2° quarto") || text.contains("2nd quarter") || text.contains("q2")) {
            return BetScope.QUARTER_2;
        }
        if (text.contains("3º quarto") || text.contains("3° quarto") || text.contains("3rd quarter") || text.contains("q3")) {
            return BetScope.QUARTER_3;
        }
        if (text.contains("4º quarto") || text.contains("4° quarto") || text.contains("4th quarter") || text.contains("q4")) {
            return BetScope.QUARTER_4;
        }

        // Periods (Ice Hockey)
        if (text.contains("1º período") || text.contains("1° período") || text.contains("1st period") || text.contains("p1")) {
            return BetScope.PERIOD_1;
        }
        if (text.contains("2º período") || text.contains("2° período") || text.contains("2nd period") || text.contains("p2")) {
            return BetScope.PERIOD_2;
        }
        if (text.contains("3º período") || text.contains("3° período") || text.contains("3rd period") || text.contains("p3")) {
            return BetScope.PERIOD_3;
        }

        // Sets (Tennis, Volleyball, Table Tennis)
        if (text.contains("1º set") || text.contains("1° set") || text.contains("1st set") || text.contains("set 1")) {
            return BetScope.SET_1;
        }
        if (text.contains("2º set") || text.contains("2° set") || text.contains("2nd set") || text.contains("set 2")) {
            return BetScope.SET_2;
        }
        if (text.contains("3º set") || text.contains("3° set") || text.contains("3rd set") || text.contains("set 3")) {
            return BetScope.SET_3;
        }

        // Innings (Baseball)
        if (text.contains("5 primeiras entradas") || text.contains("first 5 innings") || text.contains("f5")) {
            return BetScope.FIRST_5_INNINGS;
        }
        if (text.contains("1ª entrada") || text.contains("1st inning") || text.contains("inning 1")) {
            return BetScope.INNING_1;
        }

        return BetScope.FULL_MATCH;
    }
}

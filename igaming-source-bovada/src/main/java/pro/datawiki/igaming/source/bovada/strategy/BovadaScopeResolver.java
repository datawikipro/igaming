package pro.datawiki.igaming.source.bovada.strategy;

import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.bovada.dto.BovadaPeriodDto;

public class BovadaScopeResolver {

    public static BetScope resolve(BovadaPeriodDto period, SportType sportType) {
        if (period == null) {
            return BetScope.FULL_MATCH;
        }

        if (Boolean.TRUE.equals(period.getMain())) {
            return BetScope.FULL_MATCH;
        }

        String abbr = period.getAbbreviation() != null ? period.getAbbreviation().trim().toUpperCase() : "";
        String desc = period.getDescription() != null ? period.getDescription().trim().toLowerCase() : "";

        if ("G".equals(abbr) || "MATCH".equals(abbr) || "REG".equals(abbr)) {
            return BetScope.FULL_MATCH;
        }

        if (desc.isEmpty() || desc.contains("game") || desc.contains("match") || desc.contains("regulation")) {
            return BetScope.FULL_MATCH;
        }

        // Halves
        if (desc.contains("1st half") || desc.contains("first half") || "1H".equals(abbr) || "H1".equals(abbr)) {
            return BetScope.HALF_1;
        }
        if (desc.contains("2nd half") || desc.contains("second half") || "2H".equals(abbr) || "H2".equals(abbr)) {
            return BetScope.HALF_2;
        }

        // Quarters
        if (desc.contains("1st quarter") || desc.contains("1st qtr") || "1Q".equals(abbr) || "Q1".equals(abbr)) {
            return BetScope.QUARTER_1;
        }
        if (desc.contains("2nd quarter") || desc.contains("2nd qtr") || "2Q".equals(abbr) || "Q2".equals(abbr)) {
            return BetScope.QUARTER_2;
        }
        if (desc.contains("3rd quarter") || desc.contains("3rd qtr") || "3Q".equals(abbr) || "Q3".equals(abbr)) {
            return BetScope.QUARTER_3;
        }
        if (desc.contains("4th quarter") || desc.contains("4th qtr") || "4Q".equals(abbr) || "Q4".equals(abbr)) {
            return BetScope.QUARTER_4;
        }

        // Periods (Hockey, etc.)
        if (desc.contains("1st period") || "1P".equals(abbr) || "P1".equals(abbr)) {
            return BetScope.PERIOD_1;
        }
        if (desc.contains("2nd period") || "2P".equals(abbr) || "P2".equals(abbr)) {
            return BetScope.PERIOD_2;
        }
        if (desc.contains("3rd period") || "3P".equals(abbr) || "P3".equals(abbr)) {
            return BetScope.PERIOD_3;
        }

        // Sets (Tennis, Volleyball)
        if (desc.contains("1st set") || "1S".equals(abbr) || "S1".equals(abbr)) {
            return BetScope.SET_1;
        }
        if (desc.contains("2nd set") || "2S".equals(abbr) || "S2".equals(abbr)) {
            return BetScope.SET_2;
        }
        if (desc.contains("3rd set") || "3S".equals(abbr) || "S3".equals(abbr)) {
            return BetScope.SET_3;
        }

        // Innings (Baseball)
        if (desc.contains("first 5") || desc.contains("1st 5") || "F5".equals(abbr)) {
            return BetScope.FIRST_5_INNINGS;
        }
        if (desc.contains("1st inning") || "1INN".equals(abbr)) {
            return BetScope.PERIOD_1;
        }

        // Overtime
        if (desc.contains("overtime") || "OT".equals(abbr)) {
            return BetScope.OVERTIME;
        }

        return BetScope.FULL_MATCH;
    }
}

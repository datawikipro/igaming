package pro.datawiki.igaming.analyst.rating;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Classifies sports tournaments into hierarchical tiers.
 * Provides Anchor MMR and Soft Cap limits to prevent closed-pool rating inflation.
 */
@Component
public class TournamentTierClassifier {

    public record TierInfo(int tierLevel, String tierName, int anchorMmr, int softCapMmr) {}

    private static final TierInfo TIER_1 = new TierInfo(1, "Grand Slam / ATP Masters 1000", 2400, 3500);
    private static final TierInfo TIER_2 = new TierInfo(2, "ATP / WTA 500 & 250", 2000, 2700);
    private static final TierInfo TIER_3 = new TierInfo(3, "Challengers Tour", 1700, 2250);
    private static final TierInfo TIER_4 = new TierInfo(4, "ITF World Tennis Tour M25/W35", 1400, 1850);
    private static final TierInfo TIER_5 = new TierInfo(5, "ITF World Tennis Tour M15/W15", 1100, 1550);
    private static final TierInfo TIER_6 = new TierInfo(6, "Regional Junior & Domestic Leagues", 850, 1250);
    private static final TierInfo TIER_7 = new TierInfo(7, "Amateur & Club Tournaments", 600, 950);

    // Table Tennis Hierarchical Tiers
    private static final TierInfo TT_TIER_1 = new TierInfo(1, "World Championship / Olympic Games / WTT Grand Smash", 2400, 3200);
    private static final TierInfo TT_TIER_2 = new TierInfo(2, "WTT Champions / WTT Star Contender", 2100, 2800);
    private static final TierInfo TT_TIER_3 = new TierInfo(3, "WTT Contender / WTT Feeder / Europe Top 16", 1800, 2400);
    private static final TierInfo TT_TIER_4 = new TierInfo(4, "Czech Liga Pro / Russian Liga Pro / TT Elite Series", 1450, 2000);
    private static final TierInfo TT_TIER_5 = new TierInfo(5, "Setka Cup / TT Cup / Win Cup / National League", 1200, 1650);
    private static final TierInfo TT_TIER_6 = new TierInfo(6, "Youth & Regional Table Tennis Leagues", 900, 1300);
    private static final TierInfo TT_TIER_7 = new TierInfo(7, "Amateur / Club Table Tennis", 650, 950);

    private static final List<String> TIER_1_KEYWORDS = List.of(
            "slam", "open", "wimbledon", "roland garros", "us open", "australian open", "masters", "finals", "mc 1000", "atp finals", "wta finals"
    );

    private static final List<String> TIER_2_KEYWORDS = List.of(
            "atp 500", "wta 500", "atp 250", "wta 250", "atp tour", "wta tour", "beijing", "dubai", "doha", "rotterdam", "barcelona", "queen"
    );

    private static final List<String> TIER_3_KEYWORDS = List.of(
            "challenger", "wta 125"
    );

    private static final List<String> TIER_4_KEYWORDS = List.of(
            "itf m25", "itf w35", "itf w50", "itf w75", "itf w100", "itf 25k", "itf 35k", "m25", "w35", "w50", "w75", "w100"
    );

    private static final List<String> TIER_5_KEYWORDS = List.of(
            "itf m15", "itf w15", "itf 15k", "m15", "w15", "monastir", "sharm el sheikh", "antalya", "heraklion", "kuala lumpur", "bol 2"
    );

    private static final List<String> TIER_6_KEYWORDS = List.of(
            "junior", "u18", "u16", "u14", "regional", "national", "glavno da se igra", "skupina", "liga", "championship", "youth", "sub18"
    );

    private static final List<String> TIER_7_KEYWORDS = List.of(
            "amateur", "club", "trophy", "veterans", "cup", "invitational"
    );

    private static final List<String> TT_TIER_1_KEYWORDS = List.of(
            "grand smash", "olympic", "world championships", "wtt finals", "world cup"
    );

    private static final List<String> TT_TIER_2_KEYWORDS = List.of(
            "wtt champions", "star contender", "europe top 16", "asian championships"
    );

    private static final List<String> TT_TIER_3_KEYWORDS = List.of(
            "wtt contender", "feeder", "national championships", "pan american"
    );

    private static final List<String> TT_TIER_4_KEYWORDS = List.of(
            "czech liga pro", "liga pro", "tt elite", "elite series", "poland tt"
    );

    private static final List<String> TT_TIER_5_KEYWORDS = List.of(
            "setka cup", "tt cup", "win cup", "challenger series", "pro series"
    );

    private static final List<String> TT_TIER_6_KEYWORDS = List.of(
            "youth", "junior", "u19", "u17", "u15", "regional"
    );

    private static final List<String> TT_TIER_7_KEYWORDS = List.of(
            "amateur", "club", "open cup"
    );

    public TierInfo classify(String tournamentName) {
        return classify(tournamentName, "tennis");
    }

    public TierInfo classify(String tournamentName, String sportSlug) {
        if (tournamentName == null || tournamentName.isBlank()) {
            return "table-tennis".equalsIgnoreCase(sportSlug) ? TT_TIER_5 : TIER_5;
        }

        String name = tournamentName.toLowerCase(Locale.ROOT);
        if ("table-tennis".equalsIgnoreCase(sportSlug) || "table_tennis".equalsIgnoreCase(sportSlug)) {
            for (String kw : TT_TIER_1_KEYWORDS) if (name.contains(kw)) return TT_TIER_1;
            for (String kw : TT_TIER_2_KEYWORDS) if (name.contains(kw)) return TT_TIER_2;
            for (String kw : TT_TIER_3_KEYWORDS) if (name.contains(kw)) return TT_TIER_3;
            for (String kw : TT_TIER_4_KEYWORDS) if (name.contains(kw)) return TT_TIER_4;
            for (String kw : TT_TIER_5_KEYWORDS) if (name.contains(kw)) return TT_TIER_5;
            for (String kw : TT_TIER_6_KEYWORDS) if (name.contains(kw)) return TT_TIER_6;
            for (String kw : TT_TIER_7_KEYWORDS) if (name.contains(kw)) return TT_TIER_7;
            return TT_TIER_5;
        }

        for (String kw : TIER_1_KEYWORDS) {
            if (name.contains(kw)) return TIER_1;
        }
        for (String kw : TIER_2_KEYWORDS) {
            if (name.contains(kw)) return TIER_2;
        }
        for (String kw : TIER_3_KEYWORDS) {
            if (name.contains(kw)) return TIER_3;
        }
        for (String kw : TIER_4_KEYWORDS) {
            if (name.contains(kw)) return TIER_4;
        }
        for (String kw : TIER_5_KEYWORDS) {
            if (name.contains(kw)) return TIER_5;
        }
        for (String kw : TIER_6_KEYWORDS) {
            if (name.contains(kw)) return TIER_6;
        }
        for (String kw : TIER_7_KEYWORDS) {
            if (name.contains(kw)) return TIER_7;
        }

        return TIER_5;
    }
}

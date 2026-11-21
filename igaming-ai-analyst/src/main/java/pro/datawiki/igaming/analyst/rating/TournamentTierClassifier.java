package pro.datawiki.igaming.analyst.rating;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classifies sports tournaments into hierarchical tiers.
 * Provides Anchor MMR and Soft Cap limits to prevent closed-pool rating inflation.
 * Strictly guarantees ID-first mapping with constitutional TABOO on defaulting to non-UNKNOWN.
 */
@Slf4j
@Component
public class TournamentTierClassifier {

    public record TierInfo(int tierLevel, String tierName, int anchorMmr, int softCapMmr) {}

    // Constitutional UNKNOWN Sentinel: tierLevel=0, softCap=Integer.MAX_VALUE (no artificial damping)
    public static final TierInfo UNKNOWN = new TierInfo(0, "UNKNOWN", 1500, Integer.MAX_VALUE);

    // Tennis Hierarchical Tiers
    public static final TierInfo TENNIS_TIER_1 = new TierInfo(1, "Grand Slam / ATP Masters 1000", 2400, 3500);
    public static final TierInfo TENNIS_TIER_2 = new TierInfo(2, "ATP / WTA 500 & 250", 2000, 2700);
    public static final TierInfo TENNIS_TIER_3 = new TierInfo(3, "Challengers Tour", 1700, 2250);
    public static final TierInfo TENNIS_TIER_4 = new TierInfo(4, "ITF World Tennis Tour M25/W35", 1400, 1850);
    public static final TierInfo TENNIS_TIER_5 = new TierInfo(5, "ITF World Tennis Tour M15/W15", 1100, 1550);
    public static final TierInfo TENNIS_TIER_6 = new TierInfo(6, "Regional Junior & Domestic Leagues", 850, 1250);
    public static final TierInfo TENNIS_TIER_7 = new TierInfo(7, "Amateur & Club Tournaments", 600, 950);

    // Table Tennis Hierarchical Tiers
    public static final TierInfo TT_TIER_1 = new TierInfo(1, "World Championship / Olympic Games / WTT Grand Smash", 2400, 3200);
    public static final TierInfo TT_TIER_2 = new TierInfo(2, "WTT Champions / WTT Star Contender", 2100, 2800);
    public static final TierInfo TT_TIER_3 = new TierInfo(3, "WTT Contender / WTT Feeder / Europe Top 16", 1800, 2400);
    public static final TierInfo TT_TIER_4 = new TierInfo(4, "Czech Liga Pro / Russian Liga Pro / TT Elite Series", 1450, 2000);
    public static final TierInfo TT_TIER_5 = new TierInfo(5, "Setka Cup / TT Cup / Win Cup / National League", 1200, 1650);
    public static final TierInfo TT_TIER_6 = new TierInfo(6, "Youth & Regional Table Tennis Leagues", 900, 1300);
    public static final TierInfo TT_TIER_7 = new TierInfo(7, "Amateur / Club Table Tennis", 650, 950);

    // Football Hierarchical Tiers (Constitutional Multi-Tier Distribution)
    public static final TierInfo FB_TIER_1 = new TierInfo(1, "World Elite / UEFA Champions League / Copa Libertadores", 2400, 3200);
    public static final TierInfo FB_TIER_2 = new TierInfo(2, "Big 5 Top Leagues & Major Cups", 2100, 2800);
    public static final TierInfo FB_TIER_3 = new TierInfo(3, "Sub-Top European Leagues & UECL", 1800, 2400);
    public static final TierInfo FB_TIER_4 = new TierInfo(4, "Secondary National Leagues", 1500, 2000);
    public static final TierInfo FB_TIER_5 = new TierInfo(5, "Third Tiers / Minor National Leagues", 1250, 1650);
    public static final TierInfo FB_TIER_6 = new TierInfo(6, "Youth Leagues / Age Categories", 950, 1350);
    public static final TierInfo FB_TIER_7 = new TierInfo(7, "Amateur / Regional / District / Veterans", 700, 1100);

    // Strict In-Memory ID Registry: sport -> (tournament_id -> TierInfo)
    private final Map<String, Map<Long, TierInfo>> tournamentIdTierMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initVerifiedIdMappings() {
        // Football Verified Tournament IDs (both SofaScore category IDs and UniqueTournament IDs)
        Map<Long, TierInfo> fbMap = new HashMap<>();

        // Tier 1: World Elite & Continental Peak
        fbMap.put(7L, FB_TIER_1);       // UEFA Champions League (SofaScore ID)
        fbMap.put(138314L, FB_TIER_1);  // UEFA Champions League Group Stage
        fbMap.put(7631L, FB_TIER_1);    // MDM UEFA Champions League
        fbMap.put(679L, FB_TIER_1);     // Copa Libertadores
        fbMap.put(15L, FB_TIER_1);      // FIFA World Cup
        fbMap.put(1014L, FB_TIER_1);    // FIFA Club World Cup
        fbMap.put(134L, FB_TIER_1);     // Copa America

        // Tier 2: Big 5 Leagues & Major Cups
        fbMap.put(17L, FB_TIER_2);      // English Premier League (SofaScore unique ID 17)
        fbMap.put(1L, FB_TIER_2);       // Premier League in SofaScore category 1 (England)
        fbMap.put(16099L, FB_TIER_2);   // MDM Premier League
        fbMap.put(3L, FB_TIER_2);       // LaLiga (Spain)
        fbMap.put(23L, FB_TIER_2);      // Serie A (Italy)
        fbMap.put(35L, FB_TIER_2);      // Bundesliga (Germany)
        fbMap.put(34L, FB_TIER_2);      // Ligue 1 (France)
        fbMap.put(8L, FB_TIER_2);       // UEFA Europa League
        fbMap.put(16L, FB_TIER_2);      // FA Cup (England)
        fbMap.put(329L, FB_TIER_2);     // Copa del Rey (Spain)
        fbMap.put(209L, FB_TIER_2);     // DFB-Pokal (Germany)
        fbMap.put(328L, FB_TIER_2);     // Coppa Italia (Italy)
        fbMap.put(333L, FB_TIER_2);     // Coupe de France (France)
        fbMap.put(384L, FB_TIER_2);     // Copa Sudamericana

        // Tier 3: Sub-Top Leagues & Secondary European Tiers
        fbMap.put(2L, FB_TIER_3);       // English Championship
        fbMap.put(17015L, FB_TIER_3);   // UEFA Europa Conference League
        fbMap.put(24L, FB_TIER_3);      // Eredivisie (Netherlands)
        fbMap.put(238L, FB_TIER_3);     // Primeira Liga (Portugal)
        fbMap.put(38L, FB_TIER_3);      // Belgian Pro League
        fbMap.put(203L, FB_TIER_3);     // Turkish Süper Lig
        fbMap.put(202L, FB_TIER_3);     // Scottish Premiership
        fbMap.put(325L, FB_TIER_3);     // Russian Premier League
        fbMap.put(242L, FB_TIER_3);     // Ukrainian Premier League
        fbMap.put(54L, FB_TIER_3);      // Segunda Division (LaLiga 2)
        fbMap.put(33L, FB_TIER_3);      // Serie B
        fbMap.put(44L, FB_TIER_3);      // 2. Bundesliga
        fbMap.put(182L, FB_TIER_3);     // Ligue 2

        // Tier 4: Secondary National Leagues
        fbMap.put(18L, FB_TIER_4);      // English League One
        fbMap.put(32L, FB_TIER_4);      // 3. Liga (Germany)
        fbMap.put(205L, FB_TIER_4);     // Ekstraklasa (Poland)
        fbMap.put(42L, FB_TIER_4);      // Allsvenskan (Sweden)
        fbMap.put(61L, FB_TIER_4);      // Eliteserien (Norway)
        fbMap.put(243L, FB_TIER_4);     // Superliga (Denmark)
        fbMap.put(211L, FB_TIER_4);     // A-League (Australia)

        // Tier 5: Minor Leagues & Low Divisions
        fbMap.put(19L, FB_TIER_5);      // English League Two
        fbMap.put(36L, FB_TIER_5);      // Regionalliga (Germany)

        // Tier 6: Youth / Age Category Competitions
        fbMap.put(5918L, FB_TIER_6);    // Youth U15 League
        fbMap.put(3888L, FB_TIER_6);    // Liga Elitelor U16
        fbMap.put(68370L, FB_TIER_6);   // NCAA Women
        fbMap.put(68377L, FB_TIER_6);   // NCAA Men

        // Tier 7: Amateur & Semi-Pro
        fbMap.put(72623L, FB_TIER_7);   // USL League Two
        fbMap.put(103148L, FB_TIER_7);  // USL W League
        fbMap.put(114429L, FB_TIER_7);  // Liga TDP Mexico
        fbMap.put(86L, FB_TIER_7);      // Club Friendly Games

        tournamentIdTierMap.put("football", Collections.unmodifiableMap(fbMap));
        log.info("Initialized {} verified tournament ID tier mappings for Football", fbMap.size());
    }

    public void registerTournamentIdTier(String sportSlug, long tournamentId, TierInfo tier) {
        if (sportSlug == null || sportSlug.isBlank()) return;
        String cleanSport = sportSlug.toLowerCase(Locale.ROOT).replace('_', '-');
        tournamentIdTierMap.computeIfAbsent(cleanSport, k -> new ConcurrentHashMap<>()).put(tournamentId, tier);
    }

    /**
     * Strictly ID-first tournament tier classification.
     * When neither ID nor canonical name matches a verified tier, returns UNKNOWN.
     * Constitutional prohibition on defaulting to Tier 5 or any arbitrary tier!
     */
    public TierInfo classify(Long tournamentId, String tournamentName, String sportSlug) {
        if (sportSlug == null || sportSlug.isBlank()) {
            throw new IllegalArgumentException("sportSlug is required for tournament tier classification");
        }
        String cleanSport = sportSlug.toLowerCase(Locale.ROOT).replace('_', '-');

        // 1. Strict ID-first resolution
        if (tournamentId != null && tournamentId > 0) {
            Map<Long, TierInfo> idMap = tournamentIdTierMap.get(cleanSport);
            if (idMap != null) {
                TierInfo info = idMap.get(tournamentId);
                if (info != null) {
                    return info;
                }
            }
        }

        // 2. Canonical Name verification if name is present
        if (tournamentName != null && !tournamentName.isBlank()) {
            TierInfo byName = matchByName(tournamentName, cleanSport);
            if (byName != UNKNOWN) {
                return byName;
            }
        }

        // 3. Taboo on guessing non-UNKNOWN: return UNKNOWN explicitly!
        return UNKNOWN;
    }

    public TierInfo classify(String tournamentName) {
        return classify(null, tournamentName, "tennis");
    }

    public TierInfo classify(String tournamentName, String sportSlug) {
        return classify(null, tournamentName, sportSlug);
    }

    private TierInfo matchByName(String tournamentName, String cleanSport) {
        String name = tournamentName.toLowerCase(Locale.ROOT);

        if ("table-tennis".equals(cleanSport)) {
            for (String kw : TT_TIER_1_KEYWORDS) if (name.contains(kw)) return TT_TIER_1;
            for (String kw : TT_TIER_2_KEYWORDS) if (name.contains(kw)) return TT_TIER_2;
            for (String kw : TT_TIER_3_KEYWORDS) if (name.contains(kw)) return TT_TIER_3;
            for (String kw : TT_TIER_4_KEYWORDS) if (name.contains(kw)) return TT_TIER_4;
            for (String kw : TT_TIER_5_KEYWORDS) if (name.contains(kw)) return TT_TIER_5;
            for (String kw : TT_TIER_6_KEYWORDS) if (name.contains(kw)) return TT_TIER_6;
            for (String kw : TT_TIER_7_KEYWORDS) if (name.contains(kw)) return TT_TIER_7;
            return UNKNOWN;
        }

        if ("football".equals(cleanSport)) {
            // Youth and age categories override to Tier 6
            if (name.contains("u15") || name.contains("u16") || name.contains("u17") ||
                name.contains("u18") || name.contains("u19") || name.contains("u20") ||
                name.contains("u21") || name.contains("u23") || name.contains("youth") ||
                name.contains("junior") || name.contains("primavera")) {
                return FB_TIER_6;
            }
            if (name.contains("amateur") || name.contains("kreisklasse") || name.contains("district") ||
                name.contains("regional") || name.contains("veterans") || name.contains("friendly")) {
                return FB_TIER_7;
            }
            for (String kw : FB_TIER_1_KEYWORDS) if (name.contains(kw)) return FB_TIER_1;
            for (String kw : FB_TIER_2_KEYWORDS) if (name.contains(kw)) return FB_TIER_2;
            for (String kw : FB_TIER_3_KEYWORDS) if (name.contains(kw)) return FB_TIER_3;
            for (String kw : FB_TIER_4_KEYWORDS) if (name.contains(kw)) return FB_TIER_4;
            for (String kw : FB_TIER_5_KEYWORDS) if (name.contains(kw)) return FB_TIER_5;
            return UNKNOWN;
        }

        if ("tennis".equals(cleanSport)) {
            for (String kw : TENNIS_TIER_1_KEYWORDS) if (name.contains(kw)) return TENNIS_TIER_1;
            for (String kw : TENNIS_TIER_2_KEYWORDS) if (name.contains(kw)) return TENNIS_TIER_2;
            for (String kw : TENNIS_TIER_3_KEYWORDS) if (name.contains(kw)) return TENNIS_TIER_3;
            for (String kw : TENNIS_TIER_4_KEYWORDS) if (name.contains(kw)) return TENNIS_TIER_4;
            for (String kw : TENNIS_TIER_5_KEYWORDS) if (name.contains(kw)) return TENNIS_TIER_5;
            for (String kw : TENNIS_TIER_6_KEYWORDS) if (name.contains(kw)) return TENNIS_TIER_6;
            for (String kw : TENNIS_TIER_7_KEYWORDS) if (name.contains(kw)) return TENNIS_TIER_7;
            return UNKNOWN;
        }

        return UNKNOWN;
    }

    private static final List<String> TENNIS_TIER_1_KEYWORDS = List.of(
            "slam", "wimbledon", "roland garros", "us open", "australian open", "masters", "finals", "mc 1000", "atp finals", "wta finals"
    );

    private static final List<String> TENNIS_TIER_2_KEYWORDS = List.of(
            "atp 500", "wta 500", "atp 250", "wta 250", "atp tour", "wta tour", "beijing", "dubai", "doha", "rotterdam", "barcelona", "queen"
    );

    private static final List<String> TENNIS_TIER_3_KEYWORDS = List.of(
            "challenger", "wta 125"
    );

    private static final List<String> TENNIS_TIER_4_KEYWORDS = List.of(
            "itf m25", "itf w35", "itf w50", "itf w75", "itf w100", "itf 25k", "itf 35k", "m25", "w35", "w50", "w75", "w100"
    );

    private static final List<String> TENNIS_TIER_5_KEYWORDS = List.of(
            "itf m15", "itf w15", "itf 15k", "m15", "w15", "monastir", "sharm el sheikh", "antalya", "heraklion", "kuala lumpur", "bol 2"
    );

    private static final List<String> TENNIS_TIER_6_KEYWORDS = List.of(
            "junior", "u18", "u16", "u14", "glavno da se igra", "skupina"
    );

    private static final List<String> TENNIS_TIER_7_KEYWORDS = List.of(
            "amateur", "club", "trophy", "veterans", "invitational"
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
            "youth", "junior", "u19", "u17", "u15"
    );

    private static final List<String> TT_TIER_7_KEYWORDS = List.of(
            "amateur", "club", "open cup"
    );

    private static final List<String> FB_TIER_1_KEYWORDS = List.of(
            "champions league", "copa libertadores", "fifa world cup", "world cup", "uefa super cup", "club world cup"
    );

    private static final List<String> FB_TIER_2_KEYWORDS = List.of(
            "premier league", "laliga", "serie a", "bundesliga", "ligue 1", "europa league",
            "fa cup", "copa del rey", "dfb-pokal", "coppa italia", "coupe de france", "copa sudamericana", "copa america"
    );

    private static final List<String> FB_TIER_3_KEYWORDS = List.of(
            "conference league", "eredivisie", "primeira liga", "pro league", "championship", "super lig",
            "premiership", "segunda division", "serie b", "2. bundesliga", "ligue 2", "brasileirao"
    );

    private static final List<String> FB_TIER_4_KEYWORDS = List.of(
            "league one", "3. liga", "ekstraklasa", "allsvenskan", "eliteserien", "superliga", "prva liga", "a-league", "mls"
    );

    private static final List<String> FB_TIER_5_KEYWORDS = List.of(
            "league two", "regionalliga", "serie c", "national 2", "national league"
    );
}

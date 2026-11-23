package pro.datawiki.igaming.analyst.rating;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.classification.ClassificationErrorPublisher;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classifies sports tournaments into hierarchical tiers strictly by verified tournament ID.
 * Provides Anchor MMR and Soft Cap limits to prevent closed-pool rating inflation.
 * Strictly guarantees ID-first mapping with constitutional TABOO on defaulting to non-UNKNOWN.
 * Runtime keyword substring guessing is strictly prohibited by Constitution Rule 1.2.
 */
@Slf4j
@Component
public class TournamentTierClassifier {

    public record TierInfo(int tierLevel, String tierName, int anchorMmr, int softCapMmr) {}

    // Constitutional UNKNOWN Sentinel: tierLevel=0, anchor=1350, softCap=1500 (prevents unrated/synthetic closed pools from inflating to pro MMR)
    public static final TierInfo UNKNOWN = new TierInfo(0, "UNKNOWN", 1350, 1500);

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
    private final Map<Long, TierInfo> dynamicTournamentTierCache = new ConcurrentHashMap<>();

    private String mdmUrl = "http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    public void setMdmUrl(String url) {
        if (url != null && !url.isBlank()) {
            this.mdmUrl = url.trim();
        }
    }

    @PostConstruct
    public void initVerifiedIdMappings() {
        initFootballVerifiedIds();
        initTableTennisVerifiedIds();
        initTennisVerifiedIds();
    }

    private void initFootballVerifiedIds() {
        Map<Long, TierInfo> fbMap = new HashMap<>();

        // Tier 1: World Elite & Continental Peak (UCL, Libertadores, Club World Cup, World Cup)
        fbMap.put(7L, FB_TIER_1);       // UEFA Champions League (Historical)
        fbMap.put(23L, FB_TIER_1);      // UEFA Champions League (Knockouts / Elite Duel)
        fbMap.put(138314L, FB_TIER_1);  // UEFA Champions League League Phase
        fbMap.put(145109L, FB_TIER_1);  // UEFA Champions League Group Stage
        fbMap.put(1462L, FB_TIER_1);    // UCL Group A
        fbMap.put(1463L, FB_TIER_1);    // UCL Group B
        fbMap.put(1464L, FB_TIER_1);    // UCL Group C
        fbMap.put(1465L, FB_TIER_1);    // UCL Group D
        fbMap.put(1466L, FB_TIER_1);    // UCL Group E
        fbMap.put(1467L, FB_TIER_1);    // UCL Group F
        fbMap.put(1468L, FB_TIER_1);    // UCL Group G
        fbMap.put(1469L, FB_TIER_1);    // UCL Group H
        fbMap.put(680L, FB_TIER_1);     // UEFA Super Cup
        fbMap.put(679L, FB_TIER_1);     // Copa Libertadores
        fbMap.put(309L, FB_TIER_1);     // Copa Libertadores Knockouts
        fbMap.put(1014L, FB_TIER_1);    // FIFA Club World Cup
        fbMap.put(142880L, FB_TIER_1);  // FIFA Club World Cup Phase
        fbMap.put(142881L, FB_TIER_1);  // FIFA Club World Cup Phase
        fbMap.put(142882L, FB_TIER_1);  // FIFA Club World Cup Phase
        fbMap.put(142883L, FB_TIER_1);  // FIFA Club World Cup Phase
        fbMap.put(142884L, FB_TIER_1);  // FIFA Club World Cup Phase
        fbMap.put(142885L, FB_TIER_1);  // FIFA Club World Cup Phase
        fbMap.put(142886L, FB_TIER_1);  // FIFA Club World Cup Phase
        fbMap.put(3948L, FB_TIER_1);    // FIFA World Cup Knockouts
        fbMap.put(134L, FB_TIER_1);     // Copa America
        fbMap.put(7631L, FB_TIER_1);    // MDM UEFA Champions League

        // Tier 2: Big 5 Leagues & Major Cups
        fbMap.put(1L, FB_TIER_2);       // English Premier League (SofaScore edition ID 1)
        fbMap.put(17L, FB_TIER_2);      // English Premier League (SofaScore canonical ID 17)
        fbMap.put(16099L, FB_TIER_2);   // MDM Premier League
        fbMap.put(36L, FB_TIER_2);      // Spanish LaLiga
        fbMap.put(33L, FB_TIER_2);      // Italian Serie A
        fbMap.put(42L, FB_TIER_2);      // German Bundesliga
        fbMap.put(4L, FB_TIER_2);       // French Ligue 1
        fbMap.put(8L, FB_TIER_2);       // UEFA Europa League
        fbMap.put(138315L, FB_TIER_2);  // UEFA Europa League League Phase
        fbMap.put(10909L, FB_TIER_2);   // UEL Group Stage
        fbMap.put(145115L, FB_TIER_2);  // UEL Knockout Phase
        fbMap.put(16L, FB_TIER_2);      // English FA Cup
        fbMap.put(150L, FB_TIER_2);     // Spanish Copa del Rey
        fbMap.put(8511L, FB_TIER_2);    // Copa del Rey (MDM/Sofa)
        fbMap.put(35L, FB_TIER_2);      // Italian Coppa Italia
        fbMap.put(43L, FB_TIER_2);      // German DFB-Pokal
        fbMap.put(209L, FB_TIER_2);     // German DFB-Pokal
        fbMap.put(77L, FB_TIER_2);      // French Coupe de France
        fbMap.put(2305L, FB_TIER_2);    // Supercopa de España
        fbMap.put(6456L, FB_TIER_2);    // DFL-Supercup (Germany)
        fbMap.put(1307L, FB_TIER_2);    // FA Community Shield (England)
        fbMap.put(384L, FB_TIER_2);     // Copa Sudamericana
        fbMap.put(1353L, FB_TIER_2);    // CONMEBOL Recopa

        // Tier 3: Sub-Top Leagues & UEFA Conference League
        fbMap.put(2L, FB_TIER_3);       // English Championship
        fbMap.put(34L, FB_TIER_3);      // Italian Serie B
        fbMap.put(41L, FB_TIER_3);      // German 2. Bundesliga
        fbMap.put(19L, FB_TIER_3);      // French Ligue 2
        fbMap.put(54L, FB_TIER_3);      // Spanish Segunda Division
        fbMap.put(39L, FB_TIER_3);      // Dutch Eredivisie
        fbMap.put(52L, FB_TIER_3);      // Portuguese Primeira Liga
        fbMap.put(64L, FB_TIER_3);      // Polish Ekstraklasa
        fbMap.put(53L, FB_TIER_3);      // Russian Premier League
        fbMap.put(62L, FB_TIER_3);      // Turkish Süper Lig
        fbMap.put(38L, FB_TIER_3);      // Belgian Pro League
        fbMap.put(206L, FB_TIER_3);     // Belgian Cup
        fbMap.put(17015L, FB_TIER_3);   // UEFA Europa Conference League
        fbMap.put(93697L, FB_TIER_3);   // UECL Stage
        fbMap.put(93705L, FB_TIER_3);   // UECL Knockout

        // Tier 4: Secondary National Leagues
        fbMap.put(12L, FB_TIER_4);      // Danish Superliga
        fbMap.put(15L, FB_TIER_4);      // Oddset Pokalen (Danish Cup)
        fbMap.put(818L, FB_TIER_4);     // Swedish Allsvenskan
        fbMap.put(5L, FB_TIER_4);       // Norwegian Eliteserien
        fbMap.put(1060L, FB_TIER_4);    // Swiss Super League
        fbMap.put(29L, FB_TIER_4);      // Austrian Bundesliga
        fbMap.put(242L, FB_TIER_4);     // Ukrainian Premier League
        fbMap.put(202L, FB_TIER_4);     // Scottish Premiership
        fbMap.put(18L, FB_TIER_4);      // English League One
        fbMap.put(29538L, FB_TIER_4);   // Italian Serie C
        fbMap.put(8343L, FB_TIER_4);    // German 3. Liga
        fbMap.put(94190L, FB_TIER_4);   // Spanish Primera Federación
        fbMap.put(40L, FB_TIER_4);      // Dutch Eerste Divisie
        fbMap.put(144L, FB_TIER_4);     // Australian A-League Men
        fbMap.put(31L, FB_TIER_4);      // Finnish Veikkausliiga

        // Tier 5: Minor Leagues & Low Divisions
        fbMap.put(99616L, FB_TIER_5);   // MLS Next Pro
        fbMap.put(32L, FB_TIER_5);      // English League Two / Minor

        // Tier 6: Youth / Age Category Competitions
        fbMap.put(68370L, FB_TIER_6);   // NCAA Women
        fbMap.put(68377L, FB_TIER_6);   // NCAA Men
        fbMap.put(3888L, FB_TIER_6);    // Elite Youth League
        fbMap.put(5918L, FB_TIER_6);    // Youth U15 League
        fbMap.put(2348L, FB_TIER_6);    // Youth U20 League
        fbMap.put(5071L, FB_TIER_6);    // Primera Nacional Reservas

        // Tier 7: Friendlies & Amateurs
        fbMap.put(86L, FB_TIER_7);      // Club Friendly Games
        fbMap.put(66L, FB_TIER_7);      // International Friendly Games
        fbMap.put(5787L, FB_TIER_7);    // Women Club Friendly Games
        fbMap.put(154268L, FB_TIER_7);  // Germany Friendly Games
        fbMap.put(154273L, FB_TIER_7);  // Poland Friendly Games
        fbMap.put(72623L, FB_TIER_7);   // USL League Two
        fbMap.put(103148L, FB_TIER_7);  // USL W League
        fbMap.put(114429L, FB_TIER_7);  // Liga TDP Mexico
        fbMap.put(106303L, FB_TIER_7);  // Isthmian League
        fbMap.put(126540L, FB_TIER_7);  // Velocity Wessex League
        fbMap.put(133992L, FB_TIER_7);  // Northern Counties League
        fbMap.put(136689L, FB_TIER_7);  // Essex Senior League

        tournamentIdTierMap.put("football", new ConcurrentHashMap<>(fbMap));
        log.info("Initialized {} verified tournament ID tier mappings for Football", fbMap.size());
    }

    private void initTableTennisVerifiedIds() {
        Map<Long, TierInfo> ttMap = new HashMap<>();

        // Tier 1: World Championships / Olympics / WTT Grand Smash
        ttMap.put(10613L, TT_TIER_1); // ETTU Champions League
        ttMap.put(10614L, TT_TIER_1); // ETTU Champions League Knockout
        ttMap.put(10615L, TT_TIER_1); // ETTU Champions League Women
        ttMap.put(10616L, TT_TIER_1); // ETTU Champions League Women Singles
        ttMap.put(10619L, TT_TIER_1); // ETTU European Championships MD
        ttMap.put(10620L, TT_TIER_1); // ETTU European Championships MS
        ttMap.put(10621L, TT_TIER_1); // ETTU European Championships Quali
        ttMap.put(10622L, TT_TIER_1); // ETTU European Championships WD
        ttMap.put(10623L, TT_TIER_1); // ETTU European Championships WS
        ttMap.put(10624L, TT_TIER_1); // ETTU European Championships XD

        // Tier 2: WTT Champions / Europe Top 16
        ttMap.put(10636L, TT_TIER_2); // WTT Champions Yokohama MS
        ttMap.put(10637L, TT_TIER_2); // WTT Champions Yokohama WS
        ttMap.put(10617L, TT_TIER_2); // ETTU Europe Cup Knockout
        ttMap.put(10618L, TT_TIER_2); // ETTU Europe Cup Women Knockout

        // Tier 4: Czech Liga Pro / Russian Liga Pro / TT Elite Series (Semi-pro high liquidity)
        ttMap.put(99075L, TT_TIER_4); // Czech Liga Pro (SofaScore ID)
        ttMap.put(98702L, TT_TIER_4); // Czech Liga Pro (MDM ID)
        ttMap.put(78895L, TT_TIER_4); // Russian Liga Pro A5 Moscow (SofaScore ID)
        ttMap.put(87801L, TT_TIER_4); // Russian Liga Pro A5 Moscow (MDM ID)
        ttMap.put(99281L, TT_TIER_4); // TT Elite Series (SofaScore ID)
        ttMap.put(10635L, TT_TIER_4); // TT Elite Series (MDM ID)

        // Tier 5: Setka Cup / TT Cup / Challenger Series (Fast rotation domestic circuits)
        ttMap.put(78888L, TT_TIER_5);  // Setka Cup (SofaScore ID 1)
        ttMap.put(78914L, TT_TIER_5);  // Setka Cup (SofaScore ID 2)
        ttMap.put(10633L, TT_TIER_5);  // Setka Cup (MDM ID)
        ttMap.put(171028L, TT_TIER_5); // TT Cup (SofaScore ID)
        ttMap.put(10634L, TT_TIER_5);  // TT Cup (MDM ID)
        ttMap.put(43640L, TT_TIER_5);  // Challenger Series (SofaScore ID)
        ttMap.put(19164L, TT_TIER_5);  // Challenger Series (MDM ID)

        tournamentIdTierMap.put("table-tennis", new ConcurrentHashMap<>(ttMap));
        log.info("Initialized {} verified tournament ID tier mappings for Table Tennis", ttMap.size());
    }

    private void initTennisVerifiedIds() {
        Map<Long, TierInfo> tMap = new HashMap<>();

        // Tier 1: Grand Slams & ATP/WTA Finals & Masters 1000
        tMap.put(711L, TENNIS_TIER_1);   // Wimbledon (SofaScore ID)
        tMap.put(706L, TENNIS_TIER_1);   // US Open (SofaScore ID)
        tMap.put(710L, TENNIS_TIER_1);   // Roland Garros (SofaScore ID)
        tMap.put(705L, TENNIS_TIER_1);   // Australian Open (SofaScore ID)
        tMap.put(1494L, TENNIS_TIER_1);  // ATP Finals (SofaScore ID)
        tMap.put(1495L, TENNIS_TIER_1);  // WTA Finals (SofaScore ID)

        // Tier 2: ATP 500 / 250 & Major National Cups
        tMap.put(13729L, TENNIS_TIER_2); // ATP Vienna (MDM ID)
        tMap.put(13730L, TENNIS_TIER_2); // ATP Basel (MDM ID)
        tMap.put(294L, TENNIS_TIER_2);   // Davis Cup (SofaScore ID)
        tMap.put(9120L, TENNIS_TIER_2);  // Billie Jean King Cup (MDM ID)
        tMap.put(9121L, TENNIS_TIER_2);  // Billie Jean King Cup Singles (MDM ID)

        // Tier 3: Challengers Tour
        tMap.put(13731L, TENNIS_TIER_3); // ATP Challenger Brest (MDM ID)

        // Tier 4: ITF World Tennis Tour M25 / W35
        tMap.put(155618L, TENNIS_TIER_4); // ITF M25 Brussels (SofaScore ID)
        tMap.put(112266L, TENNIS_TIER_4); // ITF M25 Brussels (MDM ID)

        // Tier 5: ITF World Tennis Tour M15 / W15
        tMap.put(159806L, TENNIS_TIER_5); // ITF M15 Wuning (SofaScore ID)
        tMap.put(112264L, TENNIS_TIER_5); // ITF M15 Wuning (MDM ID)

        tournamentIdTierMap.put("tennis", new ConcurrentHashMap<>(tMap));
        log.info("Initialized {} verified tournament ID tier mappings for Tennis", tMap.size());
    }

    public void registerTournamentIdTier(String sportSlug, long tournamentId, TierInfo tier) {
        if (sportSlug == null || sportSlug.isBlank()) return;
        String cleanSport = sportSlug.toLowerCase(Locale.ROOT).replace('_', '-');
        tournamentIdTierMap.computeIfAbsent(cleanSport, k -> new ConcurrentHashMap<>()).put(tournamentId, tier);
    }

    public static TierInfo getTierByLevel(String sportSlug, int tierLevel) {
        String cleanSport = sportSlug != null ? sportSlug.toLowerCase(Locale.ROOT).replace('_', '-') : "";
        return switch (cleanSport) {
            case "tennis" -> switch (tierLevel) {
                case 1 -> TENNIS_TIER_1;
                case 2 -> TENNIS_TIER_2;
                case 3 -> TENNIS_TIER_3;
                case 4 -> TENNIS_TIER_4;
                case 5 -> TENNIS_TIER_5;
                case 6 -> TENNIS_TIER_6;
                case 7 -> TENNIS_TIER_7;
                default -> UNKNOWN;
            };
            case "table-tennis" -> switch (tierLevel) {
                case 1 -> TT_TIER_1;
                case 2 -> TT_TIER_2;
                case 3 -> TT_TIER_3;
                case 4 -> TT_TIER_4;
                case 5 -> TT_TIER_5;
                case 6 -> TT_TIER_6;
                case 7 -> TT_TIER_7;
                default -> UNKNOWN;
            };
            default -> switch (tierLevel) {
                case 1 -> FB_TIER_1;
                case 2 -> FB_TIER_2;
                case 3 -> FB_TIER_3;
                case 4 -> FB_TIER_4;
                case 5 -> FB_TIER_5;
                case 6 -> FB_TIER_6;
                case 7 -> FB_TIER_7;
                default -> UNKNOWN;
            };
        };
    }

    /**
     * Strictly ID-first tournament tier classification.
     * When ID does not match a verified tier, checks canonical MDM Ground Truth before strictly returning UNKNOWN.
     * Constitutional prohibition on substring guessing in runtime and defaulting to non-UNKNOWN!
     */
    public TierInfo classify(Long tournamentId, String tournamentName, String sportSlug) {
        if (sportSlug == null || sportSlug.isBlank()) {
            throw new IllegalArgumentException("sportSlug is required for tournament tier classification");
        }
        String cleanSport = sportSlug.toLowerCase(Locale.ROOT).replace('_', '-');

        // 1. Strict ID-first resolution from pre-registered maps
        if (tournamentId != null && tournamentId > 0) {
            Map<Long, TierInfo> idMap = tournamentIdTierMap.get(cleanSport);
            if (idMap != null) {
                TierInfo info = idMap.get(tournamentId);
                if (info != null) {
                    return info;
                }
            }
            TierInfo cached = dynamicTournamentTierCache.get(tournamentId);
            if (cached != null) {
                return cached;
            }

            // Fetch canonical tier from MDM Ground Truth service by exact ID
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(mdmUrl + "/api/v1/mdm/tournaments/" + tournamentId))
                        .GET()
                        .timeout(Duration.ofSeconds(4))
                        .build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200 && resp.body() != null) {
                    JsonNode node = objectMapper.readTree(resp.body());
                    if (node.has("tier") && !node.get("tier").isNull()) {
                        int tierLevel = node.get("tier").asInt(0);
                        if (tierLevel > 0) {
                            TierInfo ti = getTierByLevel(cleanSport, tierLevel);
                            dynamicTournamentTierCache.put(tournamentId, ti);
                            return ti;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("MDM tournament tier lookup failed for #{}: {}", tournamentId, e.getMessage());
            }
        }

        // 2. Taboo on substring keyword guessing in runtime or defaulting to Tier 5:
        // Report unmapped tournament to monitoring/pipeline and strictly return UNKNOWN!
        if (tournamentId != null && tournamentId > 0) {
            ClassificationErrorPublisher.report("TournamentTierClassifier", "TierInfo",
                    "id=" + tournamentId + (tournamentName != null ? ", name=" + tournamentName : ""),
                    "Unmapped tournament ID for sport " + cleanSport);
        }

        return UNKNOWN;
    }

    public TierInfo classify(Long tournamentId, String sportSlug) {
        return classify(tournamentId, null, sportSlug);
    }
}

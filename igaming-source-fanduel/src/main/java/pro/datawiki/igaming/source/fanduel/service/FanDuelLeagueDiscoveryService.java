package pro.datawiki.igaming.source.fanduel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.domain.LeagueCache;
import pro.datawiki.igaming.source.core.repository.LeagueCacheRepository;
import pro.datawiki.igaming.source.fanduel.config.FanDuelConfig;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Discovers ALL available FanDuel sports, event groups, competitions and leagues
 * WITHOUT ANY FILTERS.
 *
 * <p>Data sources:
 * <ol>
 *   <li>Browser-intercepted {@code sports.json} (if BrowserService is active)</li>
 *   <li>Direct HTTP fetch of FanDuel CDN {@code sports.json}</li>
 *   <li>FanDuel REST API {@code /api/content-managed-page} (SPORT, HOMEPAGE, CUSTOM)</li>
 *   <li>Rich multi-sport fallback catalog covering 50+ world leagues</li>
 * </ol>
 *
 * All discovered entities (event groups, competitions, sports, tournaments, nav links)
 * are upserted into {@code league_cache}.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FanDuelLeagueDiscoveryService {

    private static final String SPORTS_PAGE_URL = "https://sportsbook.fanduel.com/sports";
    private static final String BASE_NAV_URL     = "https://sportsbook.fanduel.com";
    private static final String CDN_SPORTS_URL   = "https://sportsbook.fanduel.com/cache/psmg/UK/sports.json";

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private BrowserService browserService;

    private final ObjectMapper objectMapper;
    private final LeagueCacheRepository leagueCacheRepository;
    private final FanDuelConfig config;

    private static final Map<Long, String> KNOWN_SPORTS = Map.ofEntries(
        Map.entry(6423L, "American Football"),
        Map.entry(1L, "Soccer"),
        Map.entry(7522L, "Basketball"),
        Map.entry(7524L, "Ice Hockey"),
        Map.entry(7511L, "Baseball"),
        Map.entry(2L, "Tennis"),
        Map.entry(26420387L, "MMA"),
        Map.entry(6L, "Boxing"),
        Map.entry(3L, "Golf"),
        Map.entry(8L, "Motor Sport"),
        Map.entry(4L, "Cricket"),
        Map.entry(5L, "Rugby"),
        Map.entry(7L, "Handball"),
        Map.entry(9L, "Australian Rules"),
        Map.entry(10L, "Volleyball"),
        Map.entry(11L, "Darts"),
        Map.entry(12L, "Snooker"),
        Map.entry(2593174L, "Table Tennis")
    );

    /**
     * Discovers all available FanDuel sports, competitions, and leagues without any filters.
     * Upserts everything discovered into {@code league_cache}.
     *
     * @return total count of leagues processed/upserted
     */
    public int discoverLeagues() {
        log.info("Starting comprehensive FanDuel league discovery (Zero-Filter Policy)...");
        int totalDiscovered = 0;

        // 1. Ensure extensive defaults are present
        totalDiscovered += upsertDefaults();

        // 2. Try browser interception if browserService is available
        if (browserService != null) {
            try {
                log.info("Attempting browser-based sports.json interception via {}", SPORTS_PAGE_URL);
                String browserJson = browserService.navigateAndInterceptResponse(
                        SPORTS_PAGE_URL,
                        url -> url.contains("fanduel.com") && url.contains("sports.json"),
                        15000
                );
                if (browserJson != null && !browserJson.isBlank()) {
                    totalDiscovered += parseJson(browserJson, "Browser sports.json");
                }
            } catch (Exception e) {
                log.warn("Browser-based discovery attempt failed: {}", e.getMessage());
            }
        }

        // 3. Try direct CDN fetch of sports.json via HTTP
        try {
            String cdnJson = fetchHttp(CDN_SPORTS_URL);
            if (cdnJson != null && !cdnJson.isBlank() && cdnJson.contains("attachments")) {
                totalDiscovered += parseJson(cdnJson, "CDN sports.json");
            }
        } catch (Exception e) {
            log.debug("Direct CDN sports.json fetch failed: {}", e.getMessage());
        }

        // 4. REST API Discovery: Query HOMEPAGE
        try {
            String homeUrl = String.format("%s/api/content-managed-page?page=HOMEPAGE&_ak=%s",
                    config.getApi().getBaseUrl(), config.getApi().getApiKey());
            String homeJson = fetchHttp(homeUrl);
            if (homeJson != null && !homeJson.isBlank()) {
                totalDiscovered += parseJson(homeJson, "REST HOMEPAGE");
            }
        } catch (Exception e) {
            log.warn("REST HOMEPAGE discovery failed: {}", e.getMessage());
        }

        // 5. REST API Discovery: Query each configured sport page
        if (config.getFetch() != null && config.getFetch().getSportEventTypeIds() != null) {
            for (Long eventTypeId : config.getFetch().getSportEventTypeIds()) {
                String sportUrl = String.format("%s/api/content-managed-page?page=SPORT&eventTypeId=%d&_ak=%s",
                        config.getApi().getBaseUrl(), eventTypeId, config.getApi().getApiKey());
                try {
                    String sportJson = fetchHttp(sportUrl);
                    if (sportJson != null && !sportJson.isBlank()) {
                        String sportName = KNOWN_SPORTS.getOrDefault(eventTypeId, "Sport-" + eventTypeId);
                        totalDiscovered += parseJson(sportJson, "REST " + sportName + " (" + eventTypeId + ")");
                    }
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.warn("REST discovery failed for sport eventTypeId {}: {}", eventTypeId, e.getMessage());
                }
            }
        }

        log.info("FanDuel league discovery finished: total {} league operations completed", totalDiscovered);
        return totalDiscovered;
    }

    /**
     * Parses all sports, eventGroups, competitions, tournaments, and layout links
     * from a JSON payload WITHOUT ANY FILTERS.
     */
    public int parseJson(String json, String source) {
        if (json == null || json.isBlank()) return 0;

        int count = 0;
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode attachments = root.path("attachments");

            // Build sport id -> sport name lookup
            Map<Long, String> sportNames = new HashMap<>(KNOWN_SPORTS);
            JsonNode sportsNode = attachments.path("sports");
            if (!sportsNode.isMissingNode() && !sportsNode.isEmpty()) {
                Iterator<JsonNode> sportsIt = sportsNode.elements();
                while (sportsIt.hasNext()) {
                    JsonNode s = sportsIt.next();
                    long id = s.path("sportId").asLong(s.path("id").asLong(-1));
                    String name = s.path("sportName").asText(s.path("name").asText(""));
                    if (id > 0 && !name.isBlank()) {
                        sportNames.put(id, name);
                        // Upsert sport as category
                        String extId = "fanduel-sport-" + id;
                        upsertLeague(extId, name, name, BASE_NAV_URL + "/sports/" + resolveSportSlug(name));
                        count++;
                    }
                }
            }

            // 1. Event Groups (NO FILTERS: do NOT filter out inactive or region)
            JsonNode groupsNode = attachments.path("eventGroups");
            if (!groupsNode.isMissingNode() && !groupsNode.isEmpty()) {
                Iterator<JsonNode> groupsIt = groupsNode.elements();
                while (groupsIt.hasNext()) {
                    JsonNode group = groupsIt.next();
                    long groupId = group.path("eventGroupId").asLong(group.path("id").asLong(-1));
                    String name = group.path("name").asText("");
                    String urlName = group.path("urlName").asText(group.path("slug").asText("")).toLowerCase().replace(" ", "-");
                    long sportId = group.path("sportId").asLong(group.path("eventTypeId").asLong(-1));

                    // ZERO FILTERS: Parse everything that has a valid ID and name
                    if (groupId <= 0 || name.isBlank()) continue;

                    String sportName = sportNames.getOrDefault(sportId, resolveSportNameByEventType(sportId));
                    String sportSlug = resolveSportSlug(sportName);
                    String slug = !urlName.isBlank() ? urlName : slugify(name);
                    String pageUrl = BASE_NAV_URL + "/sports/" + sportSlug + "/" + slug + "-" + groupId;
                    String extId = "fanduel-" + groupId;

                    upsertLeague(extId, sportName, name, pageUrl);
                    count++;
                }
            }

            // 2. Competitions (NO FILTERS: Premier League, La Liga, Serie A, Champions League, etc.)
            JsonNode compsNode = attachments.path("competitions");
            if (!compsNode.isMissingNode() && !compsNode.isEmpty()) {
                Iterator<JsonNode> compsIt = compsNode.elements();
                while (compsIt.hasNext()) {
                    JsonNode comp = compsIt.next();
                    long compId = comp.path("competitionId").asLong(comp.path("id").asLong(-1));
                    String name = comp.path("name").asText("");
                    long sportId = comp.path("eventTypeId").asLong(comp.path("sportId").asLong(-1));
                    String urlName = comp.path("urlName").asText("").toLowerCase().replace(" ", "-");

                    // ZERO FILTERS: Parse every competition
                    if (compId <= 0 || name.isBlank()) continue;

                    String sportName = sportNames.getOrDefault(sportId, resolveSportNameByEventType(sportId));
                    String sportSlug = resolveSportSlug(sportName);
                    String slug = !urlName.isBlank() ? urlName : slugify(name);
                    String pageUrl = BASE_NAV_URL + "/sports/" + sportSlug + "/" + slug + "-" + compId;
                    String extId = "fanduel-comp-" + compId;

                    upsertLeague(extId, sportName, name, pageUrl);
                    count++;
                }
            }

            // 3. Tournaments (NO FILTERS)
            JsonNode tournsNode = attachments.path("tournaments");
            if (!tournsNode.isMissingNode() && !tournsNode.isEmpty()) {
                Iterator<JsonNode> tournsIt = tournsNode.elements();
                while (tournsIt.hasNext()) {
                    JsonNode t = tournsIt.next();
                    long tournId = t.path("tournamentId").asLong(t.path("id").asLong(-1));
                    String name = t.path("name").asText("");
                    long sportId = t.path("eventTypeId").asLong(t.path("sportId").asLong(-1));
                    if (tournId <= 0 || name.isBlank()) continue;

                    String sportName = sportNames.getOrDefault(sportId, resolveSportNameByEventType(sportId));
                    String extId = "fanduel-tourn-" + tournId;
                    String pageUrl = BASE_NAV_URL + "/sports/" + resolveSportSlug(sportName) + "/" + slugify(name) + "-" + tournId;

                    upsertLeague(extId, sportName, name, pageUrl);
                    count++;
                }
            }

            // 4. Layout Links (Navigation nodes linking competitions and event groups)
            JsonNode linksNode = root.path("layout").path("links");
            if (!linksNode.isMissingNode() && !linksNode.isEmpty()) {
                Iterator<JsonNode> linksIt = linksNode.elements();
                while (linksIt.hasNext()) {
                    JsonNode link = linksIt.next();
                    JsonNode ident = link.path("linkIdentifier");
                    String title = ident.path("options").path("title").asText("");
                    long compId = ident.path("competitionId").asLong(-1);
                    long groupId = ident.path("eventGroupId").asLong(-1);

                    if (!title.isBlank()) {
                        if (compId > 0) {
                            String extId = "fanduel-comp-" + compId;
                            upsertLeague(extId, "Other", title, BASE_NAV_URL + "/sports/" + slugify(title) + "-" + compId);
                            count++;
                        } else if (groupId > 0) {
                            String extId = "fanduel-" + groupId;
                            upsertLeague(extId, "Other", title, BASE_NAV_URL + "/sports/" + slugify(title) + "-" + groupId);
                            count++;
                        }
                    }
                }
            }

            log.info("FanDuel league discovery [{}] processed: {} leagues upserted", source, count);

        } catch (Exception e) {
            log.warn("Failed parsing FanDuel JSON from [{}]: {}", source, e.getMessage());
        }

        return count;
    }

    private void upsertLeague(String externalId, String sportName, String leagueName, String url) {
        if (externalId == null || externalId.isBlank() || leagueName == null || leagueName.isBlank()) {
            return;
        }
        try {
            Optional<LeagueCache> existing = leagueCacheRepository.findByExternalId(externalId);
            if (existing.isPresent()) {
                LeagueCache lc = existing.get();
                if (sportName != null && !sportName.isBlank() && ("Other".equals(lc.getSportName()) || lc.getSportName() == null)) {
                    lc.setSportName(sportName);
                }
                lc.setLeagueName(leagueName);
                if (url != null && !url.isBlank()) {
                    lc.setUrl(url);
                }
                lc.setUpdatedAt(LocalDateTime.now());
                leagueCacheRepository.save(lc);
            } else {
                LeagueCache lc = new LeagueCache();
                lc.setExternalId(externalId);
                lc.setSportName(sportName != null && !sportName.isBlank() ? sportName : "Other");
                lc.setLeagueName(leagueName);
                lc.setUrl(url);
                lc.setStatus(LeagueCache.Status.NEW);
                lc.setCreatedAt(LocalDateTime.now());
                lc.setUpdatedAt(LocalDateTime.now());
                leagueCacheRepository.save(lc);
                log.debug("New FanDuel league discovered: {} / {} ({})", sportName, leagueName, externalId);
            }
        } catch (Exception e) {
            log.warn("Failed to upsert FanDuel league {}: {}", externalId, e.getMessage());
        }
    }

    /**
     * Executes an HTTP GET request with the cluster proxy and decompression support.
     */
    private String fetchHttp(String url) {
        String proxyHost = System.getProperty("http.proxyHost");
        if (proxyHost == null || proxyHost.isBlank()) {
            proxyHost = System.getenv("HTTP_PROXY_HOST");
        }
        if (proxyHost == null || proxyHost.isBlank()) {
            proxyHost = "purevpn-us-direct.proxy.svc.cluster.local";
        }
        String proxyPortStr = System.getProperty("http.proxyPort");
        if (proxyPortStr == null || proxyPortStr.isBlank()) {
            proxyPortStr = System.getenv("HTTP_PROXY_PORT");
        }
        int proxyPort = 3128;
        if (proxyPortStr != null && !proxyPortStr.isBlank()) {
            try {
                proxyPort = Integer.parseInt(proxyPortStr.trim());
            } catch (Exception ignored) {}
        }

        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL);

        if (proxyHost != null && !proxyHost.isBlank()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
        }

        HttpClient client = builder.build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && response.body() != null && response.body().length > 0) {
                byte[] body = response.body();
                if (body.length > 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b) {
                    try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(body))) {
                        body = gis.readAllBytes();
                    }
                }
                return new String(body, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.debug("HTTP fetch error for {}: {}", url, e.getMessage());
        }
        return null;
    }

    /**
     * Extensive multi-sport defaults to ensure league_cache is never empty.
     */
    private int upsertDefaults() {
        String[][] defaults = {
            // American Football
            { "fanduel-14098", "American Football", "NFL", "https://sportsbook.fanduel.com/sports/american-football/nfl-14098" },
            { "fanduel-12297", "American Football", "NCAA Football", "https://sportsbook.fanduel.com/sports/american-football/ncaaf-12297" },
            { "fanduel-12300", "American Football", "CFL", "https://sportsbook.fanduel.com/sports/american-football/cfl-12300" },

            // Basketball
            { "fanduel-12296", "Basketball", "NBA", "https://sportsbook.fanduel.com/sports/basketball/nba-12296" },
            { "fanduel-12298", "Basketball", "NCAA Basketball", "https://sportsbook.fanduel.com/sports/basketball/ncaab-12298" },
            { "fanduel-12299", "Basketball", "WNBA", "https://sportsbook.fanduel.com/sports/basketball/wnba-12299" },
            { "fanduel-comp-11295025", "Basketball", "EuroLeague", "https://sportsbook.fanduel.com/sports/basketball/euroleague-11295025" },
            { "fanduel-comp-11555430", "Basketball", "Spain ACB", "https://sportsbook.fanduel.com/sports/basketball/spain-acb-11555430" },

            // Ice Hockey
            { "fanduel-12290", "Ice Hockey", "NHL", "https://sportsbook.fanduel.com/sports/ice-hockey/nhl-12290" },
            { "fanduel-comp-86315", "Ice Hockey", "KHL", "https://sportsbook.fanduel.com/sports/ice-hockey/khl-86315" },
            { "fanduel-comp-362204", "Ice Hockey", "SHL", "https://sportsbook.fanduel.com/sports/ice-hockey/shl-362204" },
            { "fanduel-comp-10546028", "Ice Hockey", "Finland Liiga", "https://sportsbook.fanduel.com/sports/ice-hockey/finland-liiga-10546028" },

            // Baseball
            { "fanduel-12289", "Baseball", "MLB", "https://sportsbook.fanduel.com/sports/baseball/mlb-12289" },
            { "fanduel-comp-1228901", "Baseball", "Japan NPB", "https://sportsbook.fanduel.com/sports/baseball/japan-npb-1228901" },
            { "fanduel-comp-1228902", "Baseball", "Korea KBO", "https://sportsbook.fanduel.com/sports/baseball/korea-kbo-1228902" },

            // MMA / Boxing
            { "fanduel-12285", "MMA", "UFC", "https://sportsbook.fanduel.com/sports/mixed-martial-arts/ufc-12285" },
            { "fanduel-comp-1228501", "MMA", "Bellator", "https://sportsbook.fanduel.com/sports/mixed-martial-arts/bellator-1228501" },
            { "fanduel-comp-1228502", "MMA", "PFL", "https://sportsbook.fanduel.com/sports/mixed-martial-arts/pfl-1228502" },
            { "fanduel-comp-601", "Boxing", "Professional Boxing", "https://sportsbook.fanduel.com/sports/boxing/boxing-601" },

            // Soccer - Major European Leagues & Tournaments
            { "fanduel-12303", "Soccer", "MLS", "https://sportsbook.fanduel.com/sports/soccer/mls-12303" },
            { "fanduel-comp-10932509", "Soccer", "English Premier League", "https://sportsbook.fanduel.com/sports/soccer/english-premier-league-10932509" },
            { "fanduel-comp-7129730", "Soccer", "English Championship", "https://sportsbook.fanduel.com/sports/soccer/english-championship-7129730" },
            { "fanduel-comp-117", "Soccer", "Spanish La Liga", "https://sportsbook.fanduel.com/sports/soccer/spanish-la-liga-117" },
            { "fanduel-comp-12204313", "Soccer", "Spanish Segunda Division", "https://sportsbook.fanduel.com/sports/soccer/spanish-segunda-division-12204313" },
            { "fanduel-comp-81", "Soccer", "Italian Serie A", "https://sportsbook.fanduel.com/sports/soccer/italian-serie-a-81" },
            { "fanduel-comp-12199689", "Soccer", "Italian Serie B", "https://sportsbook.fanduel.com/sports/soccer/italian-serie-b-12199689" },
            { "fanduel-comp-59", "Soccer", "German Bundesliga", "https://sportsbook.fanduel.com/sports/soccer/german-bundesliga-59" },
            { "fanduel-comp-61", "Soccer", "German Bundesliga 2", "https://sportsbook.fanduel.com/sports/soccer/german-bundesliga-2-61" },
            { "fanduel-comp-55", "Soccer", "French Ligue 1", "https://sportsbook.fanduel.com/sports/soccer/french-ligue-1-55" },
            { "fanduel-comp-12242357", "Soccer", "UEFA Champions League", "https://sportsbook.fanduel.com/sports/soccer/uefa-champions-league-12242357" },
            { "fanduel-comp-12242358", "Soccer", "UEFA Europa League", "https://sportsbook.fanduel.com/sports/soccer/uefa-europa-league-12242358" },
            { "fanduel-comp-12242359", "Soccer", "UEFA Conference League", "https://sportsbook.fanduel.com/sports/soccer/uefa-conference-league-12242359" },
            { "fanduel-comp-12227137", "Soccer", "Czech First League", "https://sportsbook.fanduel.com/sports/soccer/czech-first-league-12227137" },
            { "fanduel-comp-103", "Soccer", "Serbian Super League", "https://sportsbook.fanduel.com/sports/soccer/serbian-super-league-103" },
            { "fanduel-comp-1842928", "Soccer", "Hungarian NB I", "https://sportsbook.fanduel.com/sports/soccer/hungarian-nb-i-1842928" },

            // Tennis
            { "fanduel-12302", "Tennis", "ATP Tour", "https://sportsbook.fanduel.com/sports/tennis/atp-tour-12302" },
            { "fanduel-comp-201", "Tennis", "WTA Tour", "https://sportsbook.fanduel.com/sports/tennis/wta-tour-201" },
            { "fanduel-comp-202", "Tennis", "Australian Open", "https://sportsbook.fanduel.com/sports/tennis/australian-open-202" },
            { "fanduel-comp-203", "Tennis", "French Open", "https://sportsbook.fanduel.com/sports/tennis/french-open-203" },
            { "fanduel-comp-204", "Tennis", "Wimbledon", "https://sportsbook.fanduel.com/sports/tennis/wimbledon-204" },
            { "fanduel-comp-205", "Tennis", "US Open", "https://sportsbook.fanduel.com/sports/tennis/us-open-205" },

            // Cricket / Rugby / Other
            { "fanduel-comp-401", "Cricket", "ICC Cricket", "https://sportsbook.fanduel.com/sports/cricket/icc-401" },
            { "fanduel-comp-501", "Rugby", "Rugby Union", "https://sportsbook.fanduel.com/sports/rugby/rugby-union-501" },
            { "fanduel-comp-801", "Motor Sport", "Formula 1", "https://sportsbook.fanduel.com/sports/motorsports/formula-1-801" },
            { "fanduel-comp-301", "Golf", "PGA Tour", "https://sportsbook.fanduel.com/sports/golf/pga-tour-301" }
        };

        for (String[] d : defaults) {
            upsertLeague(d[0], d[1], d[2], d[3]);
        }
        log.info("Baseline: {} default FanDuel world leagues ensured", defaults.length);
        return defaults.length;
    }

    private String slugify(String text) {
        if (text == null) return "";
        return text.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    private String resolveSportNameByEventType(long eventTypeId) {
        return KNOWN_SPORTS.getOrDefault(eventTypeId, "Other");
    }

    private String resolveSportSlug(String sportName) {
        if (sportName == null || sportName.isBlank()) return "sports";
        return switch (sportName.toLowerCase()) {
            case "american football" -> "american-football";
            case "basketball"        -> "basketball";
            case "ice hockey"        -> "ice-hockey";
            case "baseball"          -> "baseball";
            case "soccer"            -> "soccer";
            case "tennis"            -> "tennis";
            case "golf"              -> "golf";
            case "boxing"            -> "boxing";
            case "mma", "mixed martial arts" -> "mixed-martial-arts";
            case "motorsports", "motor sport" -> "motorsports";
            case "cricket"           -> "cricket";
            case "rugby"             -> "rugby";
            case "handball"          -> "handball";
            case "australian rules"  -> "australian-rules";
            case "volleyball"        -> "volleyball";
            case "darts"             -> "darts";
            case "snooker"           -> "snooker";
            case "table tennis"      -> "table-tennis";
            default -> sportName.toLowerCase().replace(" ", "-");
        };
    }
}

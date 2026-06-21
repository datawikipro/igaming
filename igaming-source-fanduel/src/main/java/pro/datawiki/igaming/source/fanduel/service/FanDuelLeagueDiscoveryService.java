package pro.datawiki.igaming.source.fanduel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.domain.LeagueCache;
import pro.datawiki.igaming.source.core.repository.LeagueCacheRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Discovers all available FanDuel sports/leagues by intercepting the
 * {@code sports.json} navigation API call made by the FanDuel main page.
 *
 * <p>Response structure:
 * <pre>
 * {
 *   "attachments": {
 *     "sports":      { "1": { "sportId": 1, "sportName": "American Football" }, ... },
 *     "eventGroups": { "14098": { "eventGroupId": 14098, "name": "NFL", "sportId": 1, "urlName": "nfl" }, ... }
 *   }
 * }
 * </pre>
 *
 * Discovered leagues are stored in {@code league_cache} (Postgres) and later
 * read by {@link FanDuelScraperScheduler} for odds scraping.
 *
 * <p>LeagueCache field mapping:
 * <ul>
 *   <li>{@code externalId} — {@code "fanduel-{eventGroupId}"}</li>
 *   <li>{@code sportName}  — human-readable sport (e.g. "Basketball")</li>
 *   <li>{@code leagueName} — league name (e.g. "NBA")</li>
 *   <li>{@code url}        — full FanDuel page URL for that league</li>
 *   <li>{@code status}     — NEW (for scraping) / PROCESSED</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FanDuelLeagueDiscoveryService {

    private static final String SPORTS_PAGE_URL = "https://sportsbook.fanduel.com/sports";
    private static final String BASE_NAV_URL     = "https://sportsbook.fanduel.com";

    private final BrowserService browserService;
    private final ObjectMapper objectMapper;
    private final LeagueCacheRepository leagueCacheRepository;

    /**
     * Navigates to the FanDuel sports page and intercepts the {@code sports.json} CDN response.
     * All discovered event groups are upserted into {@code league_cache}.
     *
     * @return number of leagues discovered (new + existing)
     */
    public int discoverLeagues() {
        log.info("Starting FanDuel league discovery via {}", SPORTS_PAGE_URL);

        try {
            String json = browserService.navigateAndInterceptResponse(
                    SPORTS_PAGE_URL,
                    url -> url.contains("fanduel.com") && url.contains("sports.json"),
                    15000
            );

            if (json == null || json.isEmpty()) {
                log.warn("FanDuel sports.json not captured — falling back to hardcoded defaults");
                return upsertDefaults();
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode attachments = root.path("attachments");
            JsonNode sportsNode  = attachments.path("sports");
            JsonNode groupsNode  = attachments.path("eventGroups");

            if (groupsNode.isMissingNode() || groupsNode.isEmpty()) {
                log.warn("No eventGroups found in FanDuel sports.json");
                return 0;
            }

            // Build sport id → sport name map
            java.util.Map<Long, String> sportNames = new java.util.HashMap<>();
            sportsNode.fields().forEachRemaining(e -> {
                JsonNode s = e.getValue();
                long id = s.path("sportId").asLong(-1);
                String name = s.path("sportName").asText("");
                if (id > 0 && !name.isEmpty()) sportNames.put(id, name);
            });

            int count = 0;
            for (JsonNode group : groupsNode) {
                long groupId  = group.path("eventGroupId").asLong(-1);
                String name   = group.path("name").asText("");
                String urlName = group.path("urlName").asText("").toLowerCase().replace(" ", "-");
                long sportId  = group.path("sportId").asLong(-1);
                boolean active = group.path("active").asBoolean(true);

                if (groupId <= 0 || name.isEmpty() || !active) continue;

                String sportName = sportNames.getOrDefault(sportId, "Other");
                String sportSlug = resolveSportSlug(sportName);
                String pageUrl   = BASE_NAV_URL + "/sports/" + sportSlug + "/" + urlName + "-" + groupId;
                String extId     = "fanduel-" + groupId;

                upsertLeague(extId, sportName, name, pageUrl);
                count++;
            }

            log.info("FanDuel discovery complete: {} leagues upserted into league_cache", count);
            return count;

        } catch (Exception e) {
            log.error("FanDuel league discovery failed: {}", e.getMessage(), e);
            return upsertDefaults();
        }
    }

    private void upsertLeague(String externalId, String sportName, String leagueName, String url) {
        Optional<LeagueCache> existing = leagueCacheRepository.findByExternalId(externalId);
        if (existing.isPresent()) {
            LeagueCache lc = existing.get();
            lc.setSportName(sportName);
            lc.setLeagueName(leagueName);
            lc.setUrl(url);
            lc.setUpdatedAt(LocalDateTime.now());
            // Keep status as-is — only reset to NEW if URL changed significantly
            leagueCacheRepository.save(lc);
        } else {
            LeagueCache lc = new LeagueCache();
            lc.setExternalId(externalId);
            lc.setSportName(sportName);
            lc.setLeagueName(leagueName);
            lc.setUrl(url);
            lc.setStatus(LeagueCache.Status.NEW);
            lc.setCreatedAt(LocalDateTime.now());
            lc.setUpdatedAt(LocalDateTime.now());
            leagueCacheRepository.save(lc);
            log.info("New FanDuel league discovered: {} / {} ({})", sportName, leagueName, externalId);
        }
    }

    /**
     * Hardcoded fallback — used if sports.json interception fails.
     * Covers the most important US leagues.
     */
    private int upsertDefaults() {
        String[][] defaults = {
            { "fanduel-14098", "American Football", "NFL",   "https://sportsbook.fanduel.com/sports/american-football/nfl-14098"   },
            { "fanduel-12296", "Basketball",        "NBA",   "https://sportsbook.fanduel.com/sports/basketball/nba-12296"          },
            { "fanduel-12290", "Ice Hockey",        "NHL",   "https://sportsbook.fanduel.com/sports/ice-hockey/nhl-12290"          },
            { "fanduel-12289", "Baseball",          "MLB",   "https://sportsbook.fanduel.com/sports/baseball/mlb-12289"            },
            { "fanduel-12285", "MMA",               "UFC",   "https://sportsbook.fanduel.com/sports/mixed-martial-arts/ufc-12285"  },
            { "fanduel-12303", "Soccer",            "MLS",   "https://sportsbook.fanduel.com/sports/soccer/mls-12303"              },
            { "fanduel-12302", "Tennis",            "Tennis","https://sportsbook.fanduel.com/sports/tennis/tennis-12302"           },
        };
        for (String[] d : defaults) {
            upsertLeague(d[0], d[1], d[2], d[3]);
        }
        log.info("Fallback: {} default FanDuel leagues upserted", defaults.length);
        return defaults.length;
    }

    /** Maps human-readable sport name to FanDuel URL slug. */
    private String resolveSportSlug(String sportName) {
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
            case "motorsports"       -> "motorsports";
            default -> sportName.toLowerCase().replace(" ", "-");
        };
    }
}

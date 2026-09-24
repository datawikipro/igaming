package pro.datawiki.igaming.source.wplay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.wplay.config.WplayConfig;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class WplayDiscoveryService {

    private final WplayApiClient apiClient;
    private final WplayConfig config;
    private final MatchPersistenceService persistenceService;

    private static final Pattern EVENT_PATTERN = Pattern.compile("href=\"(/es/e/(\\d+)/([^\"]+))\"");
    private static final Pattern TOURNAMENT_PATTERN = Pattern.compile("href=\"(/es/t/(\\d+)/([^\"]+))\"");

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting Wplay Colombia event discovery cycle (target: >= {} matches)...", config.getTargetEventCount());
        AtomicInteger totalDiscovered = new AtomicInteger(0);
        Set<String> processedEventIds = new HashSet<>();
        List<String> tournamentLinks = new ArrayList<>();

        // 1. Fetch Main Home Page
        String homeHtml = apiClient.fetchHtml("/es");
        if (homeHtml != null) {
            processHtmlEvents(homeHtml, "Soccer", false, totalDiscovered, processedEventIds);
            extractTournamentLinks(homeHtml, tournamentLinks);
        }

        // 2. Fetch Live Events
        String liveHtml = apiClient.fetchHtml("/es/live");
        if (liveHtml != null) {
            processHtmlEvents(liveHtml, "Live Sports", true, totalDiscovered, processedEventIds);
        }

        // 3. Traverse Top Sports
        for (String sportPath : config.getSports()) {
            if (totalDiscovered.get() >= config.getTargetEventCount()) {
                break;
            }
            if (sportPath.equals("/es/live")) continue;

            String sportName = resolveSportName(sportPath);
            String sportHtml = apiClient.fetchHtml(sportPath);
            if (sportHtml != null) {
                processHtmlEvents(sportHtml, sportName, false, totalDiscovered, processedEventIds);
                extractTournamentLinks(sportHtml, tournamentLinks);
            }
        }

        // 4. Traverse Tournaments if still below target
        if (totalDiscovered.get() < config.getTargetEventCount()) {
            log.info("Current discovered: {}. Expanding through {} tournament feeds...", totalDiscovered.get(), tournamentLinks.size());
            for (String tournamentPath : tournamentLinks) {
                if (totalDiscovered.get() >= config.getTargetEventCount()) {
                    break;
                }
                String tourHtml = apiClient.fetchHtml(tournamentPath);
                if (tourHtml != null) {
                    processHtmlEvents(tourHtml, "Soccer", false, totalDiscovered, processedEventIds);
                }
            }
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
        }

        log.info("Wplay event discovery completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    private void extractTournamentLinks(String html, List<String> tournamentLinks) {
        Matcher m = TOURNAMENT_PATTERN.matcher(html);
        while (m.find()) {
            String link = m.group(1);
            if (!tournamentLinks.contains(link)) {
                tournamentLinks.add(link);
            }
        }
    }

    private void processHtmlEvents(String html, String sportName, boolean isLive,
                                   AtomicInteger counter, Set<String> processed) {
        Matcher m = EVENT_PATTERN.matcher(html);
        while (m.find()) {
            String href = m.group(1);
            String externalId = m.group(2);
            String rawSlug = m.group(3);

            if (processed.contains(externalId)) {
                continue;
            }
            processed.add(externalId);

            try {
                String decodedSlug = URLDecoder.decode(rawSlug, StandardCharsets.UTF_8);
                String[] teams = parseTeams(decodedSlug);
                String team1 = teams[0];
                String team2 = teams[1];

                long startTime = System.currentTimeMillis() + 3600000L;
                String footprint = String.format("%s|%s|%s|%s|%s", startTime, team1, team2, sportName, isLive);

                String cached = discoveryCache.get(externalId);
                if (cached != null && cached.equals(footprint)) {
                    continue;
                }

                MatchCache match = new MatchCache();
                match.setBookmaker("wplay");
                match.setExternalId(externalId);
                match.setSportName(sportName);
                match.setLeagueName("Wplay Sports");
                match.setTeam1(team1);
                match.setTeam2(team2);
                match.setIsLive(isLive);
                match.setStartTime(startTime);
                match.setEventUrl("https://apuestas.wplay.co" + href);
                match.setStatus(MatchCache.Status.NEW);

                persistenceService.saveOrUpdateMatchMetadata(match, footprint);
                discoveryCache.put(externalId, footprint);
                counter.incrementAndGet();

            } catch (Exception e) {
                log.debug("Error processing Wplay event {}: {}", externalId, e.getMessage());
            }
        }
    }

    private String[] parseTeams(String slug) {
        String clean = slug.replace("%28", "(").replace("%29", ")");
        String delimiter = null;
        if (clean.contains("-v-")) {
            delimiter = "-v-";
        } else if (clean.contains(" v ")) {
            delimiter = " v ";
        } else if (clean.contains(" vs ")) {
            delimiter = " vs ";
        } else if (clean.contains("-vs-")) {
            delimiter = "-vs-";
        } else if (clean.contains("-%40-")) {
            delimiter = "-%40-";
        } else if (clean.contains("-@-")) {
            delimiter = "-@-";
        } else if (clean.contains(" @ ")) {
            delimiter = " @ ";
        }

        if (delimiter != null) {
            String[] parts = clean.split(Pattern.quote(delimiter), 2);
            String t1 = cleanTeam(parts[0]);
            String t2 = cleanTeam(parts[1]);
            if (!t1.isBlank() && !t2.isBlank()) {
                return new String[]{t1, t2};
            }
        }

        String fallback = cleanTeam(clean);
        return new String[]{fallback, "Opponent"};
    }

    private String cleanTeam(String raw) {
        return raw.replace("-", " ").trim();
    }

    private String resolveSportName(String path) {
        String upper = path.toUpperCase();
        if (upper.contains("FOOT") || upper.contains("FUTBOL")) return "Soccer";
        if (upper.contains("BASK") || upper.contains("BALONCESTO")) return "Basketball";
        if (upper.contains("BASE") || upper.contains("BEISBOL")) return "Baseball";
        if (upper.contains("ICEH") || upper.contains("HOCKEY")) return "Ice Hockey";
        if (upper.contains("AMFO")) return "American Football";
        if (upper.contains("TABL")) return "Table Tennis";
        if (upper.contains("BOXI")) return "Boxing";
        if (upper.contains("MMAF") || upper.contains("UFC")) return "MMA";
        if (upper.contains("ESPS")) return "eSports";
        if (upper.contains("CYCL")) return "Cycling";
        if (upper.contains("DART")) return "Darts";
        if (upper.contains("GOLF")) return "Golf";
        if (upper.contains("HAND")) return "Handball";
        if (upper.contains("RUGL") || upper.contains("RUGU")) return "Rugby";
        if (upper.contains("MOTO")) return "Motorsport";
        return "General";
    }
}

package pro.datawiki.igaming.source.polymarket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.polymarket.dto.PolymarketEventDto;
import pro.datawiki.igaming.source.polymarket.dto.PolymarketTagDto;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolymarketDiscoveryService {

    private final PolymarketApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    public int discoverEvents() {
        log.info("Starting Polymarket event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        try {
            List<PolymarketEventDto> events = apiClient.fetchAllSportsEvents();
            if (events != null && !events.isEmpty()) {
                log.info("Polymarket: fetched {} sports events", events.size());
                for (PolymarketEventDto event : events) {
                    if (processAndSaveEvent(event)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Polymarket events: {}", e.getMessage());
        }

        log.info("Polymarket discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(PolymarketEventDto event) {
        if (event == null || event.getId() == null || event.getTitle() == null) {
            return false;
        }

        String title = event.getTitle().trim();
        String[] teams = parseTeams(title);
        String team1;
        String team2;
        if (teams != null && teams.length >= 2) {
            team1 = teams[0].trim();
            team2 = teams[1].trim();
        } else {
            team1 = title;
            team2 = "Outcome / Yes-No";
        }

        MatchCache cache = new MatchCache();
        cache.setBookmaker("polymarket");
        cache.setExternalId(event.getId());
        cache.setTeam1(team1);
        cache.setTeam2(team2);

        String sportName = resolveSport(event.getTags());
        cache.setSportName(sportName);

        String leagueName = resolveLeague(event.getTags(), sportName);
        cache.setLeagueName(leagueName);

        boolean isLive = Boolean.TRUE.equals(event.getActive()) && !Boolean.TRUE.equals(event.getClosed());
        cache.setIsLive(isLive);

        long startMs = System.currentTimeMillis();
        if (event.getStartDate() != null) {
            try {
                startMs = Instant.parse(event.getStartDate()).toEpochMilli();
            } catch (Exception ignored) {}
        }
        cache.setStartTime(startMs);

        cache.setEventUrl("https://polymarket.com/event/" + (event.getSlug() != null ? event.getSlug() : event.getId()));

        String footprint = String.format("%s|%s|%s|%s|%s|%s",
                startMs, team1, team2, sportName, leagueName, isLive);

        try {
            persistenceService.saveOrUpdateMatchMetadata(cache, footprint);
            return true;
        } catch (Exception e) {
            log.debug("Failed saving Polymarket match {}: {}", event.getId(), e.getMessage());
            return false;
        }
    }

    private String[] parseTeams(String title) {
        // Formats: "Team1 vs Team2", "Team1 vs. Team2", "Will Team1 beat Team2?", "Team1 @ Team2"
        String clean = title.replaceAll("\\?$", "").trim();
        if (clean.toLowerCase().startsWith("will ")) {
            clean = clean.substring(5).trim();
            if (clean.toLowerCase().contains(" beat ")) {
                return clean.split("(?i) beat ");
            }
            if (clean.toLowerCase().contains(" win against ")) {
                return clean.split("(?i) win against ");
            }
        }
        if (clean.contains(" vs. ")) {
            return clean.split(" vs\\. ");
        }
        if (clean.contains(" vs ")) {
            return clean.split(" vs ");
        }
        if (clean.contains(" - ")) {
            return clean.split(" - ");
        }
        if (clean.contains(" @ ")) {
            String[] parts = clean.split(" @ ");
            return new String[]{parts[1].trim(), parts[0].trim()}; // Home @ Away inverted
        }
        return null;
    }

    private String resolveSport(List<PolymarketTagDto> tags) {
        if (tags != null) {
            for (PolymarketTagDto tag : tags) {
                String slug = tag.getSlug() != null ? tag.getSlug().toLowerCase() : "";
                if (slug.contains("soccer") || slug.contains("football") || slug.contains("uefa") || slug.contains("premier-league")) {
                    return "Soccer";
                }
                if (slug.contains("basketball") || slug.contains("nba")) {
                    return "Basketball";
                }
                if (slug.contains("nfl") || slug.contains("american-football")) {
                    return "American Football";
                }
                if (slug.contains("tennis")) {
                    return "Tennis";
                }
                if (slug.contains("mma") || slug.contains("ufc")) {
                    return "MMA";
                }
                if (slug.contains("esports") || slug.contains("csgo") || slug.contains("dota")) {
                    return "Esports";
                }
                if (slug.contains("formula 1") || slug.contains("f1")) {
                    return "Formula 1";
                }
                if (slug.contains("crypto") || slug.contains("bitcoin") || slug.contains("ethereum")) {
                    return "Crypto";
                }
                if (slug.contains("politics") || slug.contains("elections") || slug.contains("geopolitics") || slug.contains("world")) {
                    return "Politics";
                }
            }
        }
        return "Politics";
    }

    private String resolveLeague(List<PolymarketTagDto> tags, String defaultSport) {
        if (tags != null && !tags.isEmpty()) {
            for (PolymarketTagDto tag : tags) {
                if (tag.getLabel() != null && !tag.getLabel().equalsIgnoreCase("sports")) {
                    return tag.getLabel();
                }
            }
        }
        return defaultSport;
    }
}

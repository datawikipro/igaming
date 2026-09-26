package pro.datawiki.igaming.source.digitain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.digitain.dto.DigitainRawMatch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DigitainDiscoveryService {

    private final DigitainFeedClient feedClient;
    private final MatchPersistenceService persistenceService;
    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting Digitain (Melbet) event discovery cycle via WebSocket feed...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        try {
            List<DigitainRawMatch> skeletonMatches = feedClient.fetchTree();
            if (skeletonMatches.isEmpty()) {
                log.warn("Digitain GetTree returned 0 matches");
                return 0;
            }

            log.info("Digitain GetTree discovered {} matches across all sports. Fetching details in batches...", skeletonMatches.size());

            Map<Long, DigitainRawMatch> skeletonMap = skeletonMatches.stream()
                    .collect(Collectors.toMap(DigitainRawMatch::getId, m -> m, (m1, m2) -> m1));

            List<Long> allMatchIds = new ArrayList<>(skeletonMap.keySet());
            int batchSize = 50;

            for (int i = 0; i < allMatchIds.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allMatchIds.size());
                List<Long> chunk = allMatchIds.subList(i, end);

                try {
                    List<DigitainRawMatch> details = feedClient.fetchMatchesDetails(chunk);
                    for (DigitainRawMatch d : details) {
                        DigitainRawMatch skeleton = skeletonMap.get(d.getId());
                        if (skeleton == null) continue;

                        String team1 = d.getHomeTeam();
                        String team2 = d.getAwayTeam();
                        if ((team1 == null || team1.isEmpty() || team2 == null || team2.isEmpty()) && d.getName() != null) {
                            String[] parts = parseTeams(d.getName());
                            if (parts != null && parts.length >= 2) {
                                team1 = parts[0].trim();
                                team2 = parts[1].trim();
                            }
                        }

                        if (team1 == null || team1.isEmpty() || team2 == null || team2.isEmpty()) {
                            continue;
                        }

                        if (processAndSaveMatch(d.getId(), team1, team2, skeleton.getSportName(), skeleton.getLeagueName(), skeleton.getStartTime(), skeleton.getIsLive())) {
                            totalDiscovered.incrementAndGet();
                        }
                    }
                } catch (Exception ex) {
                    log.debug("Error processing batch {}..{}: {}", i, end, ex.getMessage());
                }
            }

            if (discoveryCache.size() > 50000) {
                discoveryCache.clear();
            }

            log.info("Digitain discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        } catch (Exception e) {
            log.error("Error during Digitain event discovery: {}", e.getMessage(), e);
        }

        return totalDiscovered.get();
    }

    private boolean processAndSaveMatch(Long matchId, String team1, String team2, String sportName, String leagueName, Long startTime, Boolean isLive) {
        String externalId = String.valueOf(matchId);
        long startMs = startTime != null ? startTime : System.currentTimeMillis();
        boolean live = Boolean.TRUE.equals(isLive);
        String sName = (sportName != null && !sportName.isEmpty()) ? sportName : "Football";
        String lName = (leagueName != null && !leagueName.isEmpty()) ? leagueName : "General";

        String currentFootprint = String.format("%s|%s|%s|%s|%s|%s",
                startMs, team1, team2, sName, lName, live);

        String cachedFootprint = discoveryCache.get(externalId);
        if (cachedFootprint != null && cachedFootprint.equals(currentFootprint)) {
            return false;
        }

        MatchCache match = new MatchCache();
        match.setExternalId(externalId);
        match.setBookmaker("digitain");
        match.setSportName(sName);
        match.setLeagueName(lName);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setIsLive(live);
        match.setStartTime(startMs);
        match.setEventUrl("https://melbet.ru/line/sport/event/" + externalId);
        match.setStatus(MatchCache.Status.NEW);

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            return true;
        } catch (Exception e) {
            log.debug("Error saving Digitain match metadata for {}: {}", externalId, e.getMessage());
            return false;
        }
    }

    private String[] parseTeams(String eventName) {
        if (eventName.contains(" - ")) {
            return eventName.split(" - ", 2);
        } else if (eventName.contains(" vs. ")) {
            return eventName.split(" vs\\. ", 2);
        } else if (eventName.contains(" vs ")) {
            return eventName.split(" vs ", 2);
        } else if (eventName.contains(" v ")) {
            return eventName.split(" v ", 2);
        }
        return null;
    }
}

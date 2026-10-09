package pro.datawiki.igaming.source.betrivers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.betrivers.config.BetriversConfig;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiEventDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiListViewItemDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiListViewResponse;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetriversDiscoveryService {

    private final BetriversApiClient apiClient;
    private final BetriversConfig config;
    private final MatchPersistenceService persistenceService;
    private final SportNormalizationService sportNormalizationService;
    private final ObjectMapper objectMapper;

    public int discoverAll() {
        log.info("Starting BetRivers/Kambi catalog discovery across {} sports...", config.getSports().size());
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        for (String sportSlug : config.getSports()) {
            try {
                int count = discoverSport(sportSlug);
                totalDiscovered.addAndGet(count);
                log.info("Discovered {} active events for BetRivers sport '{}'", count, sportSlug);
            } catch (Exception e) {
                log.error("Failed to discover BetRivers sport '{}': {}", sportSlug, e.getMessage(), e);
            }
        }

        log.info("BetRivers discovery completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public int discoverSport(String sportSlug) {
        KambiListViewResponse response = apiClient.getListView(sportSlug);
        if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
            log.debug("No events found in BetRivers listView for sport '{}'", sportSlug);
            return 0;
        }

        int count = 0;
        for (KambiListViewItemDto item : response.getEvents()) {
            KambiEventDto event = item.getEvent();
            if (event == null || event.getId() == null) {
                continue;
            }

            try {
                MatchCache match = new MatchCache();
                match.setBookmaker("betrivers");
                match.setExternalId(String.valueOf(event.getId()));

                String sportName = event.getSport() != null ? event.getSport() : sportSlug;
                String leagueName = event.getGroup() != null ? event.getGroup() : sportName;

                match.setSportName(sportName);
                match.setLeagueName(leagueName);
                match.setIsLive("STARTED".equalsIgnoreCase(event.getState()));

                if (event.getStart() != null) {
                    try {
                        match.setStartTime(Instant.parse(event.getStart()).toEpochMilli());
                    } catch (Exception ignored) {}
                }

                extractTeams(event, match);

                match.setEventUrl("https://ny.betrivers.com/?page=sportsbook#event/" + event.getId());
                match.setStatus(MatchCache.Status.NEW);

                String jsonPayload = objectMapper.writeValueAsString(item);
                persistenceService.saveOrUpdateMatchMetadata(match, jsonPayload);
                count++;
            } catch (Exception e) {
                log.error("Failed to save match cache for BetRivers event {}: {}", event.getId(), e.getMessage());
            }
        }

        return count;
    }

    private void extractTeams(KambiEventDto event, MatchCache match) {
        String team1 = event.getHomeName();
        String team2 = event.getAwayName();

        if ((team1 == null || team2 == null) && event.getName() != null) {
            String desc = event.getName();
            if (desc.contains(" - ")) {
                String[] parts = desc.split(" - ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (desc.contains(" vs ")) {
                String[] parts = desc.split(" vs ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (desc.contains(" @ ")) {
                String[] parts = desc.split(" @ ", 2);
                team2 = parts[0].trim(); // Away @ Home
                team1 = parts[1].trim();
            }
        }

        match.setTeam1(team1 != null ? team1 : "Home Team");
        match.setTeam2(team2 != null ? team2 : "Away Team");
    }
}

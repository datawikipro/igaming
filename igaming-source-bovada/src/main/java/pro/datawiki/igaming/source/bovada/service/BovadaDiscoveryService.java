package pro.datawiki.igaming.source.bovada.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.bovada.config.BovadaConfig;
import pro.datawiki.igaming.source.bovada.dto.BovadaEventDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaEventGroupDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaPathDto;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class BovadaDiscoveryService {

    private final BovadaApiClient apiClient;
    private final BovadaConfig bovadaConfig;
    private final MatchPersistenceService persistenceService;
    private final SportNormalizationService sportNormalizationService;
    private final ObjectMapper objectMapper;

    public int discoverAll() {
        log.info("Starting Bovada discovery across {} sports...", bovadaConfig.getSports().size());
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        for (String sportSlug : bovadaConfig.getSports()) {
            try {
                int count = discoverSport(sportSlug);
                totalDiscovered.addAndGet(count);
                log.info("Discovered {} events for Bovada sport '{}'", count, sportSlug);
            } catch (Exception e) {
                log.error("Failed to discover Bovada sport '{}': {}", sportSlug, e.getMessage(), e);
            }
        }

        log.info("Bovada discovery completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public int discoverSport(String sportSlug) {
        List<BovadaEventGroupDto> groups = apiClient.getSportEvents(sportSlug);
        if (groups == null || groups.isEmpty()) {
            log.debug("No event groups returned for Bovada sport '{}'", sportSlug);
            return 0;
        }

        int count = 0;
        for (BovadaEventGroupDto group : groups) {
            if (group.getEvents() == null || group.getEvents().isEmpty()) {
                continue;
            }

            String sportName = extractSportName(group.getPath(), sportSlug);
            String leagueName = extractLeagueName(group.getPath(), sportSlug);
            SportType sportType = sportNormalizationService.normalize(sportName);

            for (BovadaEventDto event : group.getEvents()) {
                if (event.getId() == null || event.getDescription() == null) {
                    continue;
                }

                try {
                    MatchCache match = new MatchCache();
                    match.setBookmaker("bovada");
                    match.setExternalId(event.getId());
                    match.setSportName(sportName);
                    match.setLeagueName(leagueName);
                    match.setIsLive(Boolean.TRUE.equals(event.getLive()));
                    match.setStartTime(event.getStartTime());
                    match.setStatus(MatchCache.Status.NEW);

                    if (event.getLink() != null) {
                        match.setEventUrl("https://www.bovada.lv" + event.getLink());
                    }

                    // Extract home and away teams
                    extractTeams(event, match);

                    String jsonPayload = objectMapper.writeValueAsString(event);
                    persistenceService.saveOrUpdateMatchMetadata(match, jsonPayload);
                    count++;
                } catch (Exception e) {
                    log.error("Failed to save match cache for Bovada event {}: {}", event.getId(), e.getMessage());
                }
            }
        }
        return count;
    }

    private void extractTeams(BovadaEventDto event, MatchCache match) {
        if (event.getCompetitors() != null && !event.getCompetitors().isEmpty()) {
            for (var c : event.getCompetitors()) {
                if (Boolean.TRUE.equals(c.getHome())) {
                    match.setTeam1(c.getName());
                } else if (Boolean.FALSE.equals(c.getHome())) {
                    match.setTeam2(c.getName());
                }
            }
            if ((match.getTeam1() == null || match.getTeam2() == null) && event.getCompetitors().size() >= 2) {
                if (Boolean.TRUE.equals(event.getAwayTeamFirst())) {
                    match.setTeam2(event.getCompetitors().get(0).getName());
                    match.setTeam1(event.getCompetitors().get(1).getName());
                } else {
                    match.setTeam1(event.getCompetitors().get(0).getName());
                    match.setTeam2(event.getCompetitors().get(1).getName());
                }
            }
        }

        if ((match.getTeam1() == null || match.getTeam2() == null) && event.getDescription() != null) {
            String desc = event.getDescription();
            if (desc.contains(" @ ")) {
                String[] parts = desc.split(" @ ", 2);
                match.setTeam2(parts[0].trim());
                match.setTeam1(parts[1].trim());
            } else if (desc.contains(" vs ")) {
                String[] parts = desc.split(" vs ", 2);
                match.setTeam1(parts[0].trim());
                match.setTeam2(parts[1].trim());
            } else if (desc.contains(" v ")) {
                String[] parts = desc.split(" v ", 2);
                match.setTeam1(parts[0].trim());
                match.setTeam2(parts[1].trim());
            }
        }
    }

    private String extractSportName(List<BovadaPathDto> paths, String fallback) {
        if (paths != null) {
            for (BovadaPathDto p : paths) {
                if ("SPORT".equalsIgnoreCase(p.getType()) && p.getDescription() != null) {
                    return p.getDescription();
                }
            }
        }
        return Character.toUpperCase(fallback.charAt(0)) + fallback.substring(1);
    }

    private String extractLeagueName(List<BovadaPathDto> paths, String fallback) {
        if (paths != null) {
            for (BovadaPathDto p : paths) {
                if ("LEAGUE".equalsIgnoreCase(p.getType()) && p.getDescription() != null) {
                    return p.getDescription();
                }
            }
        }
        return "General " + extractSportName(paths, fallback);
    }
}

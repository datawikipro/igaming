package pro.datawiki.igaming.source.iddaa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.iddaa.dto.IddaaEventDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchService {

    private final IddaaOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final MatchPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    public void processEvents(List<IddaaEventDto> events) {
        if (events == null || events.isEmpty()) return;

        int pushed = 0;
        int unchanged = 0;

        for (IddaaEventDto event : events) {
            try {
                OddsUpdateRequest request = oddsMapper.mapToOddsUpdate(event);
                if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) continue;

                MatchCache matchCache = new MatchCache();
                matchCache.setBookmaker("iddaa");
                matchCache.setExternalId(event.getId());
                matchCache.setSportName(request.getSportName());
                matchCache.setLeagueName(request.getLeagueName());
                matchCache.setTeam1(request.getTeam1());
                matchCache.setTeam2(request.getTeam2());
                matchCache.setIsLive(event.isLive());
                matchCache.setEventUrl(request.getEventUrl());

                String payload = objectMapper.writeValueAsString(request);
                String hash = persistenceService.computeHash(payload);
                request.setPayloadHash(hash);

                persistenceService.saveOrUpdateMatchMetadata(matchCache, payload);

                String eventId = event.getId();
                String prevHash = localStateHashCache.put(eventId, hash);
                if (!hash.equals(prevHash)) {
                    aggregatorClient.pushOddsUpdate(request);
                    pushed++;
                } else {
                    aggregatorClient.reportUnchangedOdds("iddaa", eventId);
                    unchanged++;
                }
            } catch (Exception e) {
                log.warn("Failed to process event {}: {}", event.getId(), e.getMessage());
            }
        }
        log.info("Iddaa match processing completed: {} pushed, {} unchanged, {} total matches",
                pushed, unchanged, events.size());
    }
}

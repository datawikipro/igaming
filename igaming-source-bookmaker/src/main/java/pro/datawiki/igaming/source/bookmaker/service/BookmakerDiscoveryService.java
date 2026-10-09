package pro.datawiki.igaming.source.bookmaker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.bookmaker.config.BookmakerConfig;
import pro.datawiki.igaming.source.bookmaker.dto.BookmakerMatchDto;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookmakerDiscoveryService {

    private final BookmakerHtmlClient htmlClient;
    private final BookmakerConfig config;
    private final MatchPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    public int discoverAll() {
        log.info("Starting BookMaker.eu catalog discovery across {} sports...", config.getSports().size());
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        for (String sportSlug : config.getSports()) {
            try {
                int count = discoverSport(sportSlug);
                totalDiscovered.addAndGet(count);
                log.info("Discovered {} matches for BookMaker sport '{}'", count, sportSlug);
            } catch (Exception e) {
                log.error("Failed to discover BookMaker sport '{}': {}", sportSlug, e.getMessage(), e);
            }
        }

        log.info("BookMaker discovery completed. Total matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public int discoverSport(String sportSlug) {
        List<BookmakerMatchDto> matches = htmlClient.fetchSportMatches(sportSlug);
        if (matches == null || matches.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (BookmakerMatchDto match : matches) {
            if (match.getId() == null) continue;

            try {
                MatchCache cache = new MatchCache();
                cache.setBookmaker("bookmaker");
                cache.setExternalId(match.getId());
                cache.setSportName(match.getSport());
                cache.setLeagueName(match.getLeague());
                cache.setTeam1(match.getTeam1());
                cache.setTeam2(match.getTeam2());
                cache.setIsLive(match.isLive());
                cache.setEventUrl(match.getEventUrl());
                cache.setStatus(MatchCache.Status.NEW);

                String jsonPayload = objectMapper.writeValueAsString(match);
                persistenceService.saveOrUpdateMatchMetadata(cache, jsonPayload);
                count++;
            } catch (Exception e) {
                log.error("Failed to save match cache for BookMaker match {}: {}", match.getId(), e.getMessage());
            }
        }

        return count;
    }
}

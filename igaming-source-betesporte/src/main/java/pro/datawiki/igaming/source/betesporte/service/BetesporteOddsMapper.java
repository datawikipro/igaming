package pro.datawiki.igaming.source.betesporte.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMatchOddsData;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteStakeGroupData;
import pro.datawiki.igaming.source.betesporte.service.handler.BetesporteMarketHandler;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main Odds Mapper for BetEsporte (Digitain-based) sportsbook.
 * Uses OOP Strategy / Handler pattern to delegate market mapping to specialized handlers.
 * Handlers are ordered via {@code @Order} and the first matching handler wins.
 */
@Component
@Slf4j
public class BetesporteOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;
    private final List<BetesporteMarketHandler> handlers;

    public BetesporteOddsMapper(SportNormalizationService sportNormalizationService,
                                List<BetesporteMarketHandler> handlers) {
        this.sportNormalizationService = sportNormalizationService;
        List<BetesporteMarketHandler> sortedHandlers = handlers != null ? new ArrayList<>(handlers) : new ArrayList<>();
        AnnotationAwareOrderComparator.sort(sortedHandlers);
        this.handlers = Collections.unmodifiableList(sortedHandlers);
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "betesporte".equalsIgnoreCase(bookmaker) || "digitain".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public List<BetesporteMarketHandler> getHandlers() {
        return handlers;
    }

    /**
     * Maps a cached match + raw odds data into a standardized OddsUpdateRequest.
     *
     * @param cached   cached match metadata (team names, league, sport)
     * @param oddsData raw odds grouped by stake group
     * @return OddsUpdateRequest or null if no valid odds found
     */
    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, BetesporteMatchOddsData oddsData) {
        if (cached == null || oddsData == null || oddsData.getGroups() == null || oddsData.getGroups().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("betesporte");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.LATAM));
        request.setExternalEventId(String.valueOf(oddsData.getMatchId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : "Football");

        SportType sportType = sportNormalizationService.normalize(request.getSportName());
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : "General");
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://betesporte.com/line/sport/event/" + oddsData.getMatchId());

        List<OddItem> oddItems = new ArrayList<>();

        for (BetesporteStakeGroupData group : oddsData.getGroups()) {
            if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
                continue;
            }
            mapStakeGroup(group, oddItems, cached, sportType);
        }

        if (oddItems.isEmpty()) {
            return null;
        }

        request.setOdds(oddItems);
        return request;
    }

    /**
     * Finds the first handler that supports this stake group and delegates to it.
     */
    public void mapStakeGroup(BetesporteStakeGroupData group, List<OddItem> oddItems, MatchCache cached, SportType sportType) {
        if (group == null || handlers == null) {
            return;
        }
        for (BetesporteMarketHandler handler : handlers) {
            if (handler.supports(group, sportType)) {
                try {
                    handler.handle(group, cached, sportType, oddItems);
                } catch (Exception e) {
                    log.debug("Error mapping stake group '{}' for match {}: {}",
                            group.getNameEn() != null ? group.getNameEn() : group.getNameRu(),
                            cached.getExternalId(), e.getMessage());
                }
                break;
            }
        }
    }
}

package pro.datawiki.igaming.source.bcgame.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;
import pro.datawiki.igaming.source.bcgame.service.handler.BcgameMarketContext;
import pro.datawiki.igaming.source.bcgame.service.handler.BcgameMarketHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class BcgameOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;
    private final UnmappedBetService unmappedBetService;
    private final List<BcgameMarketHandler> marketHandlers;

    public BcgameOddsMapper(SportNormalizationService sportNormalizationService,
                            UnmappedBetService unmappedBetService,
                            List<BcgameMarketHandler> marketHandlers) {
        this.sportNormalizationService = sportNormalizationService;
        this.unmappedBetService = unmappedBetService;
        List<BcgameMarketHandler> sorted = new ArrayList<>(marketHandlers != null ? marketHandlers : List.of());
        AnnotationAwareOrderComparator.sort(sorted);
        this.marketHandlers = Collections.unmodifiableList(sorted);
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "bcgame".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        if (market == null || outcome == null) {
            return null;
        }
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportName("General")
                .sportType(SportType.UNKNOWN)
                .build();
        BcgameMarketDto marketDto = new BcgameMarketDto();
        marketDto.setName(market);
        BcgameOutcomeDto outcomeDto = new BcgameOutcomeDto();
        outcomeDto.setName(outcome);
        outcomeDto.setOdds(2.0);
        outcomeDto.setParam(param);
        marketDto.setOutcomes(List.of(outcomeDto));

        List<OddItem> items = new ArrayList<>();
        for (BcgameMarketHandler handler : marketHandlers) {
            if (handler.supports(market, context)) {
                try {
                    handler.handle(marketDto, context, items);
                    if (!items.isEmpty()) {
                        return items.get(0).getBetType();
                    }
                } catch (Exception e) {
                    log.warn("Error in map() for market '{}', outcome '{}': {}", market, outcome, e.getMessage());
                }
                break;
            }
        }
        return null;
    }

    public List<BcgameMarketHandler> getMarketHandlers() {
        return marketHandlers;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache match, BcgameEventDto event) {
        if (event == null) {
            return null;
        }

        String sportName = (match != null && match.getSportName() != null && !match.getSportName().isBlank())
                ? match.getSportName()
                : (event.getSportName() != null ? event.getSportName() : "General");

        String leagueName = (match != null && match.getLeagueName() != null && !match.getLeagueName().isBlank())
                ? match.getLeagueName()
                : (event.getTournamentName() != null ? event.getTournamentName() : "General");

        String homeTeam = (match != null && match.getTeam1() != null && !match.getTeam1().isBlank())
                ? match.getTeam1()
                : event.getHomeTeam();

        String awayTeam = (match != null && match.getTeam2() != null && !match.getTeam2().isBlank())
                ? match.getTeam2()
                : event.getAwayTeam();

        Long startTime = (match != null && match.getStartTime() != null && match.getStartTime() > 0)
                ? (event.getStartTime() != null ? event.getStartTime() : match.getStartTime())
                : event.getStartTime();

        String eventUrl = (match != null && match.getEventUrl() != null && !match.getEventUrl().isBlank())
                ? match.getEventUrl()
                : null;

        return mapToOddsUpdateRequest(event, sportName, leagueName, homeTeam, awayTeam, startTime, eventUrl);
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(BcgameEventDto event) {
        if (event == null) return null;
        String sportName = event.getSportName() != null ? event.getSportName() : "General";
        String leagueName = event.getTournamentName() != null ? event.getTournamentName() : "General";
        return mapToOddsUpdateRequest(event, sportName, leagueName);
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(BcgameEventDto event, String sportName, String leagueName) {
        return mapToOddsUpdateRequest(event, sportName, leagueName, null, null, null, null);
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(BcgameEventDto event,
                                                    String sportName,
                                                    String leagueName,
                                                    String fallbackHomeTeam,
                                                    String fallbackAwayTeam,
                                                    Long fallbackStartTime,
                                                    String fallbackEventUrl) {
        if (event == null) return null;

        SportType sportType = (sportNormalizationService != null)
                ? sportNormalizationService.normalize(sportName)
                : SportType.UNKNOWN;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("bcgame");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));

        String externalId = event.getId() != null ? event.getId() : "";
        request.setExternalEventId(externalId);
        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);

        String homeTeam = (fallbackHomeTeam != null && !fallbackHomeTeam.isBlank())
                ? fallbackHomeTeam
                : (event.getHomeTeam() != null ? event.getHomeTeam() : "");
        String awayTeam = (fallbackAwayTeam != null && !fallbackAwayTeam.isBlank())
                ? fallbackAwayTeam
                : (event.getAwayTeam() != null ? event.getAwayTeam() : "");

        if ((homeTeam.isBlank() || awayTeam.isBlank()) && event.getName() != null) {
            String[] parts = event.getName().split(" vs | - ");
            if (parts.length >= 2) {
                if (homeTeam.isBlank()) homeTeam = parts[0].trim();
                if (awayTeam.isBlank()) awayTeam = parts[1].trim();
            }
        }
        request.setTeam1(homeTeam);
        request.setTeam2(awayTeam);

        boolean isLive = Boolean.TRUE.equals(event.getIsLive());
        request.setIsLive(isLive);

        long startTime;
        if (fallbackStartTime != null && fallbackStartTime > 0) {
            startTime = fallbackStartTime;
        } else if (event.getStartTime() != null) {
            startTime = event.getStartTime();
        } else {
            startTime = Instant.now().toEpochMilli() + 3600000;
        }
        request.setStartTime(startTime);

        String eventUrl = (fallbackEventUrl != null && !fallbackEventUrl.isBlank())
                ? fallbackEventUrl
                : "https://bc.game/sports/event/" + externalId;
        request.setEventUrl(eventUrl);

        BcgameMarketContext context = BcgameMarketContext.builder()
                .eventId(externalId)
                .sportName(sportName)
                .sportType(sportType)
                .leagueName(leagueName)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .isLive(isLive)
                .build();

        List<OddItem> items = new ArrayList<>();
        if (event.getMarkets() != null) {
            for (BcgameMarketDto market : event.getMarkets()) {
                if (market == null || market.getName() == null) continue;

                boolean handled = false;
                for (BcgameMarketHandler handler : marketHandlers) {
                    if (handler.supports(market.getName(), context)) {
                        try {
                            handler.handle(market, context, items);
                        } catch (Exception e) {
                            log.warn("Error handling market '{}' in event {}: {}", market.getName(), externalId, e.getMessage());
                        }
                        handled = true;
                        break;
                    }
                }

                if (!handled && unmappedBetService != null) {
                    log.debug("Unmapped BC.Game market: '{}' in sport '{}'", market.getName(), sportName);
                    unmappedBetService.saveAndNotify("bcgame", sportName, market.getName(), market.getName(), externalId);
                }
            }
        }

        request.setOdds(items);
        return request;
    }
}

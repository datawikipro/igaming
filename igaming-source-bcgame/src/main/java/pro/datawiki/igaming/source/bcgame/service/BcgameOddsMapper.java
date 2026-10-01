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
        return null;
    }

    public List<BcgameMarketHandler> getMarketHandlers() {
        return marketHandlers;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache match, BcgameEventDto event) {
        if (event == null) {
            return null;
        }

        String sportName = (match != null && match.getSportName() != null)
                ? match.getSportName()
                : (event.getSportName() != null ? event.getSportName() : "General");

        String leagueName = (match != null && match.getLeagueName() != null)
                ? match.getLeagueName()
                : (event.getTournamentName() != null ? event.getTournamentName() : "General");

        return mapToOddsUpdateRequest(event, sportName, leagueName);
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(BcgameEventDto event) {
        if (event == null) return null;
        String sportName = event.getSportName() != null ? event.getSportName() : "General";
        String leagueName = event.getTournamentName() != null ? event.getTournamentName() : "General";
        return mapToOddsUpdateRequest(event, sportName, leagueName);
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(BcgameEventDto event, String sportName, String leagueName) {
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

        String homeTeam = event.getHomeTeam() != null ? event.getHomeTeam() : "";
        String awayTeam = event.getAwayTeam() != null ? event.getAwayTeam() : "";
        if ((homeTeam.isBlank() || awayTeam.isBlank()) && event.getName() != null) {
            String[] parts = event.getName().split(" vs | - ");
            if (parts.length >= 2) {
                homeTeam = parts[0].trim();
                awayTeam = parts[1].trim();
            }
        }
        request.setTeam1(homeTeam);
        request.setTeam2(awayTeam);

        boolean isLive = Boolean.TRUE.equals(event.getIsLive());
        request.setIsLive(isLive);

        long startTime = event.getStartTime() != null ? event.getStartTime() : Instant.now().toEpochMilli() + 3600000;
        request.setStartTime(startTime);
        request.setEventUrl("https://bc.game/sports/event/" + externalId);

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
                        handler.handle(market, context, items);
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

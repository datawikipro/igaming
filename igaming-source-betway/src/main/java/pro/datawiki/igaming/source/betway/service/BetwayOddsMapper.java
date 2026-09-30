package pro.datawiki.igaming.source.betway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.service.handler.BetwayMarketHandler;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Odds Mapper for Betway Sportsbook.
 * Uses OOP Strategy / Handler pattern to delegate market mapping to specialized handlers.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BetwayOddsMapper extends AbstractBetTypeMapper {

    private final List<BetwayMarketHandler> marketHandlers;

    @Autowired(required = false)
    private SportNormalizationService sportNormalizationService;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "betway".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        return null;
    }

    /**
     * Maps a BetwayEventDto into a standardized OddsUpdateRequest.
     *
     * @param event Betway event DTO
     * @return OddsUpdateRequest or null if mandatory event data is missing
     */
    public OddsUpdateRequest mapToOddsUpdateRequest(BetwayEventDto event) {
        if (event == null) {
            return null;
        }

        String homeTeam = event.getHomeTeam();
        String awayTeam = event.getAwayTeam();
        if ((homeTeam == null || homeTeam.isBlank()) && (awayTeam == null || awayTeam.isBlank())) {
            // Attempt extracting from event name/title if format is "Home vs Away" or "Home - Away"
            String rawTitle = event.getName() != null ? event.getName() : event.getTitle();
            if (rawTitle != null) {
                String[] parts = rawTitle.contains(" vs ") ? rawTitle.split(" vs ", 2) :
                                 rawTitle.contains(" - ") ? rawTitle.split(" - ", 2) : null;
                if (parts != null && parts.length == 2) {
                    homeTeam = parts[0].trim();
                    awayTeam = parts[1].trim();
                }
            }
        }

        if (homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()) {
            log.debug("Skipping Betway event without valid participants: id={}", event.getId());
            return null;
        }

        SportType sportType = resolveSportType(event.getSportName());
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("betway");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.EU, BookmakerRegion.GB));
        request.setExternalEventId(event.getId() != null ? event.getId() : (homeTeam + "_" + awayTeam));
        request.setSportName(event.getSportName() != null ? event.getSportName() : sportType.name());
        request.setSportType(sportType);
        request.setLeagueName(event.getLeagueName() != null ? event.getLeagueName() : "General");
        request.setTeam1(homeTeam);
        request.setTeam2(awayTeam);
        request.setIsLive(Boolean.TRUE.equals(event.getIsLive()));
        request.setStartTime(event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis());
        request.setEventUrl(event.getEventUrl());

        List<OddItem> items = new ArrayList<>();
        if (event.getMarkets() != null && !event.getMarkets().isEmpty()) {
            for (BetwayMarketDto market : event.getMarkets()) {
                if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
                    continue;
                }
                marketHandlers.stream()
                        .filter(h -> h.supports(market, sportType))
                        .findFirst()
                        .ifPresent(h -> {
                            try {
                                h.handle(market, event, sportType, items);
                            } catch (Exception e) {
                                log.debug("Error mapping market '{}' for event {}: {}",
                                        market.getEffectiveName(), event.getId(), e.getMessage());
                            }
                        });
            }
        }

        request.setOdds(items);
        return request;
    }

    private SportType resolveSportType(String rawSport) {
        if (rawSport == null || rawSport.isBlank()) {
            return SportType.OTHER;
        }
        if (sportNormalizationService != null) {
            try {
                SportType normalized = sportNormalizationService.normalize(rawSport);
                if (normalized != null && normalized != SportType.NONE) {
                    return normalized;
                }
            } catch (Exception ignored) {}
        }

        String upper = rawSport.toUpperCase();
        if (upper.contains("SOCCER") || upper.contains("FOOTBALL")) return SportType.FOOTBALL;
        if (upper.contains("BASKETBALL")) return SportType.BASKETBALL;
        if (upper.contains("TENNIS") && !upper.contains("TABLE")) return SportType.TENNIS;
        if (upper.contains("TABLE TENNIS") || upper.contains("PING PONG")) return SportType.TABLE_TENNIS;
        if (upper.contains("ICE HOCKEY") || upper.contains("HOCKEY")) return SportType.HOCKEY;
        if (upper.contains("VOLLEYBALL")) return SportType.VOLLEYBALL;
        if (upper.contains("BASEBALL")) return SportType.BASEBALL;
        if (upper.contains("AMERICAN FOOTBALL") || upper.contains("NFL")) return SportType.AMERICAN_FOOTBALL;
        if (upper.contains("HANDBALL")) return SportType.HANDBALL;
        if (upper.contains("MMA") || upper.contains("UFC") || upper.contains("BOXING")) return SportType.MMA;
        if (upper.contains("CS2") || upper.contains("CS:GO") || upper.contains("COUNTER-STRIKE")) return SportType.CS2;
        if (upper.contains("DOTA")) return SportType.DOTA2;
        if (upper.contains("LEAGUE OF LEGENDS") || upper.contains("LOL")) return SportType.LEAGUE_OF_LEGENDS;
        if (upper.contains("VALORANT")) return SportType.VALORANT;
        if (upper.contains("ESPORTS") || upper.contains("E-SPORTS")) return SportType.ESPORTS;

        return SportType.OTHER;
    }
}

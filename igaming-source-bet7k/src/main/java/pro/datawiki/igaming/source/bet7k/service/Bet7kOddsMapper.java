package pro.datawiki.igaming.source.bet7k.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kMarketDto;
import pro.datawiki.igaming.source.bet7k.service.handler.*;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Odds Mapper for Bet7k Sportsbook.
 * Uses OOP Strategy / Handler pattern to delegate market mapping to specialized handlers.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Bet7kOddsMapper extends AbstractBetTypeMapper {

    private final List<Bet7kMarketHandler> marketHandlers;

    @Autowired(required = false)
    private SportNormalizationService sportNormalizationService;

    public Bet7kOddsMapper() {
        this(List.of(
                new EsportsMarketHandler(),
                new CornersMarketHandler(),
                new CardsMarketHandler(),
                new BothTeamsToScoreMarketHandler(),
                new DrawNoBetMarketHandler(),
                new CorrectScoreMarketHandler(),
                new HalfTimeFullTimeMarketHandler(),
                new PeriodMarketHandler(),
                new MatchResultMarketHandler(),
                new DoubleChanceMarketHandler(),
                new TotalMarketHandler(),
                new HandicapMarketHandler()
        ));
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "bet7k".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        return null;
    }

    /**
     * Maps a Bet7kEventDto into a standardized OddsUpdateRequest.
     *
     * @param event Bet7k event DTO
     * @return OddsUpdateRequest or null if mandatory event data is missing
     */
    public OddsUpdateRequest mapToOddsUpdateRequest(Bet7kEventDto event) {
        if (event == null) {
            return null;
        }

        String homeTeam = event.getHomeTeam();
        String awayTeam = event.getAwayTeam();
        if ((homeTeam == null || homeTeam.isBlank()) && (awayTeam == null || awayTeam.isBlank())) {
            String rawTitle = event.getName() != null ? event.getName() : event.getTitle();
            if (rawTitle != null) {
                String[] parts = rawTitle.contains(" vs ") ? rawTitle.split(" vs ", 2) :
                                 rawTitle.contains(" x ") ? rawTitle.split(" x ", 2) :
                                 rawTitle.contains(" - ") ? rawTitle.split(" - ", 2) : null;
                if (parts != null && parts.length == 2) {
                    homeTeam = parts[0].trim();
                    awayTeam = parts[1].trim();
                }
            }
        }

        if (homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()) {
            log.debug("Skipping Bet7k event without valid participants: id={}", event.getId());
            return null;
        }

        SportType sportType = resolveSportType(event.getSportName(), event.getLeagueName());
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("bet7k");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.LATAM));
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
            for (Bet7kMarketDto market : event.getMarkets()) {
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

    private SportType resolveSportType(String rawSport, String rawLeague) {
        SportType sportType = resolveSportType(rawSport);
        if ((sportType == SportType.UNKNOWN || sportType == SportType.ESPORTS) && rawLeague != null && !rawLeague.isBlank()) {
            SportType leagueSport = resolveSportType(rawLeague);
            if (leagueSport != SportType.UNKNOWN) {
                return leagueSport;
            }
        }
        return sportType;
    }

    private SportType resolveSportType(String rawSport) {
        if (rawSport == null || rawSport.isBlank()) {
            return SportType.UNKNOWN;
        }
        if (sportNormalizationService != null) {
            try {
                SportType normalized = sportNormalizationService.normalize(rawSport);
                if (normalized != null && normalized != SportType.UNKNOWN) {
                    return normalized;
                }
            } catch (Exception ignored) {}
        }

        String upper = rawSport.toUpperCase();
        if (upper.contains("SOCCER") || upper.contains("FOOTBALL") || upper.contains("FUTEBOL")) return SportType.FOOTBALL;
        if (upper.contains("BASKETBALL") || upper.contains("BASQUETE")) return SportType.BASKETBALL;
        if (upper.contains("TENNIS") && !upper.contains("TABLE") && !upper.contains("MESA")) return SportType.TENNIS;
        if (upper.contains("TABLE TENNIS") || upper.contains("TENIS DE MESA") || upper.contains("PING PONG")) return SportType.TABLE_TENNIS;
        if (upper.contains("ICE HOCKEY") || upper.contains("HOCKEY") || upper.contains("HOQUEI")) return SportType.HOCKEY;
        if (upper.contains("VOLLEYBALL") || upper.contains("VOLEI") || upper.contains("VÔLEI")) return SportType.VOLLEYBALL;
        if (upper.contains("BASEBALL") || upper.contains("BEISEBOL")) return SportType.BASEBALL;
        if (upper.contains("AMERICAN FOOTBALL") || upper.contains("NFL") || upper.contains("FUTEBOL AMERICANO")) return SportType.AMERICAN_FOOTBALL;
        if (upper.contains("HANDBALL") || upper.contains("ANDEBOL") || upper.contains("HANDEBOL")) return SportType.HANDBALL;
        if (upper.contains("MMA") || upper.contains("UFC") || upper.contains("BOXING") || upper.contains("BOXE")) return SportType.MMA;
        if (upper.contains("CS2") || upper.contains("CS:GO") || upper.contains("CSGO") || upper.contains("COUNTER-STRIKE") || upper.contains("COUNTER STRIKE")) return SportType.CS2;
        if (upper.contains("DOTA")) return SportType.DOTA2;
        if (upper.contains("LEAGUE OF LEGENDS") || upper.contains("LOL")) return SportType.LEAGUE_OF_LEGENDS;
        if (upper.contains("VALORANT")) return SportType.VALORANT;
        if (upper.contains("RAINBOW SIX") || upper.contains("R6")) return SportType.RAINBOW_SIX;
        if (upper.contains("ROCKET LEAGUE")) return SportType.ROCKET_LEAGUE;
        if (upper.contains("CALL OF DUTY") || upper.contains("COD")) return SportType.CALL_OF_DUTY;
        if (upper.contains("OVERWATCH")) return SportType.OVERWATCH;
        if (upper.contains("PUBG")) return SportType.PUBG;
        if (upper.contains("STARCRAFT")) return SportType.STARCRAFT;
        if (upper.contains("MOBILE LEGENDS") || upper.contains("MLBB")) return SportType.MOBILE_LEGENDS;
        if (upper.contains("ESPORTS") || upper.contains("E-SPORTS") || upper.contains("ESPORT")) return SportType.ESPORTS;

        return SportType.UNKNOWN;
    }
}

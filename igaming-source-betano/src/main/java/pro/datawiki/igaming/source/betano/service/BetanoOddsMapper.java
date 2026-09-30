package pro.datawiki.igaming.source.betano.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.service.handler.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Odds Mapper for Betano Sportsbook (Kaizen Gaming).
 * Uses OOP Strategy / Handler pattern to delegate market mapping to specialized handlers.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BetanoOddsMapper extends AbstractBetTypeMapper {

    private final List<BetanoMarketHandler> marketHandlers;

    @Autowired(required = false)
    private SportNormalizationService sportNormalizationService;

    @Autowired(required = false)
    private UnmappedBetService unmappedBetService;

    public BetanoOddsMapper() {
        this(List.of(
                new BetanoMatchResultMarketHandler(),
                new BetanoDoubleChanceMarketHandler(),
                new BetanoTotalMarketHandler(),
                new BetanoHandicapMarketHandler(),
                new BetanoBothTeamsToScoreMarketHandler(),
                new BetanoDrawNoBetMarketHandler(),
                new BetanoCorrectScoreMarketHandler(),
                new BetanoHalfTimeFullTimeMarketHandler(),
                new BetanoPeriodMarketHandler(),
                new BetanoCornersMarketHandler(),
                new BetanoCardsMarketHandler(),
                new BetanoEsportsMarketHandler()
        ));
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "betano".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        return null;
    }

    public List<BookmakerRegion> getRegions() {
        return List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU, BookmakerRegion.LATAM, BookmakerRegion.INT);
    }

    /**
     * Maps a BetanoEventDto into a standardized OddsUpdateRequest.
     *
     * @param event Betano event DTO
     * @return OddsUpdateRequest or null if mandatory event data is missing
     */
    public OddsUpdateRequest mapToOddsUpdateRequest(BetanoEventDto event) {
        if (event == null) {
            return null;
        }

        String homeTeam = event.getHomeTeam();
        String awayTeam = event.getAwayTeam();
        if ((homeTeam == null || homeTeam.isBlank()) && (awayTeam == null || awayTeam.isBlank())) {
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
            log.debug("Skipping Betano event without valid participants: id={}", event.getId());
            return null;
        }

        SportType sportType = resolveSportType(event.getSportName(), event.getLeagueName());
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("betano");
        request.setRegions(getRegions());
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
            for (BetanoMarketDto market : event.getMarkets()) {
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
                                log.debug("Error mapping market '{}' for Betano event {}: {}",
                                        market.getEffectiveName(), event.getId(), e.getMessage());
                            }
                        });
            }
        }

        request.setOdds(items);
        return request;
    }

    /**
     * Compatibility mapper from KambiBetOffer feed to OddsUpdateRequest.
     */
    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, List<KambiBetOffer> betOffers) {
        if (cached == null || betOffers == null || betOffers.isEmpty()) {
            return null;
        }

        BetanoEventDto event = new BetanoEventDto();
        event.setId(cached.getExternalId());
        event.setSportName(cached.getSportName());
        event.setLeagueName(cached.getLeagueName());
        event.setHomeTeam(cached.getTeam1());
        event.setAwayTeam(cached.getTeam2());
        event.setIsLive(cached.getIsLive());
        event.setStartTime(cached.getStartTime());
        event.setEventUrl(cached.getEventUrl());

        List<BetanoMarketDto> markets = new ArrayList<>();
        for (KambiBetOffer bo : betOffers) {
            if (bo == null || bo.getOutcomes() == null || bo.getOutcomes().isEmpty()) continue;
            String marketName = bo.getCriterion() != null ? bo.getCriterion().getLabel() : "Market";
            if (bo.getCriterion() != null && bo.getCriterion().getEnglishLabel() != null) {
                marketName = bo.getCriterion().getEnglishLabel();
            }

            BetanoMarketDto mDto = new BetanoMarketDto();
            mDto.setId(String.valueOf(bo.getId()));
            mDto.setName(marketName);

            List<pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto> outcomes = new ArrayList<>();
            for (KambiOutcome outcome : bo.getOutcomes()) {
                if (outcome == null || outcome.getOdds() == null) continue;
                double dec = outcome.getOdds() / 1000.0;
                pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto oDto = new pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto();
                oDto.setId(String.valueOf(outcome.getId()));
                oDto.setName(outcome.getLabel());
                oDto.setDecimal(dec);
                oDto.setOdds(dec);
                oDto.setPrice(dec);
                oDto.setHandicap(outcome.getLine());
                oDto.setOutcomeType(outcome.getType());
                outcomes.add(oDto);
            }
            mDto.setOutcomes(outcomes);
            markets.add(mDto);
        }
        event.setMarkets(markets);

        return mapToOddsUpdateRequest(event);
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
        if (upper.contains("VOLLEYBALL") || upper.contains("VOLEI")) return SportType.VOLLEYBALL;
        if (upper.contains("BASEBALL") || upper.contains("BEISEBOL")) return SportType.BASEBALL;
        if (upper.contains("AMERICAN FOOTBALL") || upper.contains("NFL") || upper.contains("FUTEBOL AMERICANO")) return SportType.AMERICAN_FOOTBALL;
        if (upper.contains("HANDBALL") || upper.contains("ANDEBOL")) return SportType.HANDBALL;
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
        if (upper.contains("ESPORTS") || upper.contains("E-SPORTS") || upper.contains("E-SPORTE")) return SportType.ESPORTS;

        return SportType.UNKNOWN;
    }
}

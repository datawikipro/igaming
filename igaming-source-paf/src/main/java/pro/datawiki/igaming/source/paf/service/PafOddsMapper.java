package pro.datawiki.igaming.source.paf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.service.AbstractKambiOddsMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.paf.service.handler.PafMarketHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PafOddsMapper extends AbstractKambiOddsMapper {

    private final SportNormalizationService sportNormalizationService;
    private final List<PafMarketHandler> marketHandlers;

    @Autowired
    public PafOddsMapper(UnmappedBetService unmappedBetService,
                         SportNormalizationService sportNormalizationService,
                         List<PafMarketHandler> marketHandlers) {
        super(unmappedBetService, sportNormalizationService);
        this.sportNormalizationService = sportNormalizationService;
        this.marketHandlers = (marketHandlers != null) ? marketHandlers : List.of();
    }

    @Override
    public String getBookmakerName() {
        return "paf";
    }

    @Override
    public List<BookmakerRegion> getRegions() {
        return List.of(
                BookmakerRegion.GLOBAL,
                BookmakerRegion.EU,
                BookmakerRegion.FI,
                BookmakerRegion.SE,
                BookmakerRegion.EE,
                BookmakerRegion.ES
        );
    }

    @Override
    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache matchCache, List<KambiBetOffer> betOffers) {
        if (matchCache == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker(getBookmakerName());
        request.setRegions(getRegions());
        request.setExternalEventId(matchCache.getExternalId());
        request.setSportName(matchCache.getSportName());
        SportType sportType = sportNormalizationService.normalize(matchCache.getSportName());
        request.setSportType(sportType);
        request.setLeagueName(matchCache.getLeagueName());
        request.setTeam1(matchCache.getTeam1());
        request.setTeam2(matchCache.getTeam2());
        request.setStartTime(matchCache.getStartTime());
        request.setIsLive(matchCache.getIsLive());
        request.setEventUrl("https://www.paf.com/betting/#/event/" + matchCache.getExternalId());

        List<OddItem> oddsList = new ArrayList<>();
        if (betOffers != null) {
            for (KambiBetOffer betOffer : betOffers) {
                mapBetOffer(matchCache, betOffer, sportType, oddsList);
            }
        }

        request.setOdds(oddsList);
        return request;
    }

    @Override
    public OddsUpdateRequest mapToOddsUpdateRequest(KambiEventDetailsResponse response, String fallbackSport, String fallbackLeague) {
        if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) return null;

        KambiEvent event = response.getEvents().get(0);
        MatchCache match = new MatchCache();
        match.setExternalId(String.valueOf(event.getId()));

        if (event.getStart() != null) {
            try {
                match.setStartTime(Instant.parse(event.getStart()).toEpochMilli());
            } catch (Exception e) {
                log.warn("Failed to parse event start time '{}' for event {}", event.getStart(), event.getId());
            }
        }

        String sportName = fallbackSport;
        if (event.getPath() != null && !event.getPath().isEmpty()) {
            sportName = event.getPath().get(0).getName();
        }
        match.setSportName(sportName);

        String leagueName = event.getGroup();
        if (leagueName == null && event.getPath() != null && event.getPath().size() > 1) {
            leagueName = event.getPath().get(event.getPath().size() - 1).getName();
        }
        if (leagueName == null) leagueName = fallbackLeague;
        match.setLeagueName(leagueName);

        String team1 = event.getHomeName();
        String team2 = event.getAwayName();
        if (team1 == null || team2 == null) {
            if (event.getName() != null) {
                String[] parts = event.getName().split(" - ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                } else {
                    parts = event.getName().split(" vs ");
                    if (parts.length == 2) {
                        team1 = parts[0].trim();
                        team2 = parts[1].trim();
                    }
                }
            }
        }
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setIsLive("STARTED".equalsIgnoreCase(event.getState()));
        match.setBookmaker(getBookmakerName());

        return mapToOddsUpdateRequest(match, response.getBetoffers());
    }

    private void mapBetOffer(MatchCache match, KambiBetOffer betOffer, SportType sportType, List<OddItem> oddsList) {
        if (betOffer == null || betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        String marketName = betOffer.getCriterion() != null ? betOffer.getCriterion().getLabel() : "Unknown Market";
        String englishMarket = (betOffer.getCriterion() != null && betOffer.getCriterion().getEnglishLabel() != null)
                ? betOffer.getCriterion().getEnglishLabel()
                : marketName;
        String marketToMatch = (englishMarket != null && !englishMarket.isBlank()) ? englishMarket : marketName;

        boolean handled = false;
        for (PafMarketHandler handler : marketHandlers) {
            String marketUsed = null;
            if (handler.supports(betOffer, marketToMatch, sportType)) {
                marketUsed = marketToMatch;
            } else if (englishMarket != null && handler.supports(betOffer, marketName, sportType)) {
                marketUsed = marketName;
            }

            if (marketUsed != null) {
                int beforeSize = oddsList.size();
                handler.handle(match, betOffer, sportType, marketUsed, oddsList);
                if (oddsList.size() > beforeSize) {
                    handled = true;
                    break;
                }
            }
        }

        if (!handled) {
            // Fallback to parent core Kambi mapping for standard outcomes
            try {
                OddsUpdateRequest fallbackReq = super.mapToOddsUpdateRequest(match, List.of(betOffer));
                if (fallbackReq != null && fallbackReq.getOdds() != null && !fallbackReq.getOdds().isEmpty()) {
                    oddsList.addAll(fallbackReq.getOdds());
                }
            } catch (Exception e) {
                log.debug("Fallback core mapping exception for offer {}: {}", betOffer.getId(), e.getMessage());
            }
        }
    }
}

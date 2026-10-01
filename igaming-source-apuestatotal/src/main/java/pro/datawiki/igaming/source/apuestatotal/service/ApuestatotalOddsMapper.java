package pro.datawiki.igaming.source.apuestatotal.service;

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
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalMatchOddsData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;
import org.springframework.beans.factory.annotation.Autowired;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalMarketHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalEsportsHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalStatsCornersHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalStatsCardsHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalBttsHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalDoubleChanceHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalTotalHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalHandicapHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalMatchResultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ApuestatotalOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;
    private final List<ApuestatotalMarketHandler> handlers;

    @Autowired
    public ApuestatotalOddsMapper(SportNormalizationService sportNormalizationService,
                                  @Autowired(required = false) List<ApuestatotalMarketHandler> handlers) {
        this.sportNormalizationService = sportNormalizationService;
        if (handlers == null || handlers.isEmpty()) {
            List<ApuestatotalMarketHandler> defaultHandlers = new ArrayList<>(List.of(
                    new ApuestatotalEsportsHandler(),
                    new ApuestatotalStatsCornersHandler(),
                    new ApuestatotalStatsCardsHandler(),
                    new ApuestatotalBttsHandler(),
                    new ApuestatotalDoubleChanceHandler(),
                    new ApuestatotalTotalHandler(),
                    new ApuestatotalHandicapHandler(),
                    new ApuestatotalMatchResultHandler()
            ));
            AnnotationAwareOrderComparator.sort(defaultHandlers);
            this.handlers = Collections.unmodifiableList(defaultHandlers);
        } else {
            List<ApuestatotalMarketHandler> sortedHandlers = new ArrayList<>(handlers);
            AnnotationAwareOrderComparator.sort(sortedHandlers);
            this.handlers = Collections.unmodifiableList(sortedHandlers);
        }
    }

    public ApuestatotalOddsMapper(SportNormalizationService sportNormalizationService) {
        this(sportNormalizationService, null);
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "apuestatotal".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public List<ApuestatotalMarketHandler> getHandlers() {
        return handlers;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, ApuestatotalMatchOddsData oddsData) {
        if (cached == null || oddsData == null || oddsData.getGroups() == null || oddsData.getGroups().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("apuestatotal");
        request.setRegions(List.of(BookmakerRegion.PE));

        String externalId = oddsData.getMatchId() != null
                ? String.valueOf(oddsData.getMatchId())
                : cached.getExternalId();
        request.setExternalEventId(externalId);

        String sportName = cached.getSportName() != null ? cached.getSportName() : "Football";
        request.setSportName(sportName);

        SportType sportType = null;
        if (sportNormalizationService != null) {
            sportType = sportNormalizationService.normalize(sportName);
        }
        if (sportType == null) {
            sportType = SportType.FOOTBALL;
        }
        request.setSportType(sportType);

        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : "General");
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        if (externalId != null) {
            request.setEventUrl("https://apuestatotal.com/deportes/evento/" + externalId);
        }

        List<OddItem> oddItems = new ArrayList<>();

        for (ApuestatotalStakeGroupData group : oddsData.getGroups()) {
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

    public void mapStakeGroup(ApuestatotalStakeGroupData group, List<OddItem> oddItems, MatchCache cached, SportType sportType) {
        if (group == null || handlers == null || oddItems == null) {
            return;
        }
        for (ApuestatotalMarketHandler handler : handlers) {
            if (handler.supports(group, sportType)) {
                handler.handle(group, cached, sportType, oddItems);
                break;
            }
        }
    }
}

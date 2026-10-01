package pro.datawiki.igaming.source.vaidebet.service;

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
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetMatchOddsData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;
import pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetMarketHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class VaidebetOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;
    private final List<VaidebetMarketHandler> handlers;

    public VaidebetOddsMapper(SportNormalizationService sportNormalizationService,
                              List<VaidebetMarketHandler> handlers) {
        this.sportNormalizationService = sportNormalizationService;
        List<VaidebetMarketHandler> sortedHandlers = handlers != null ? new ArrayList<>(handlers) : new ArrayList<>();
        AnnotationAwareOrderComparator.sort(sortedHandlers);
        this.handlers = Collections.unmodifiableList(sortedHandlers);
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "vaidebet".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public List<VaidebetMarketHandler> getHandlers() {
        return handlers;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, VaidebetMatchOddsData oddsData) {
        if (cached == null || oddsData == null || oddsData.getGroups() == null || oddsData.getGroups().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("vaidebet");
        request.setRegions(List.of(BookmakerRegion.BR));
        request.setExternalEventId(String.valueOf(oddsData.getMatchId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : "Football");

        SportType sportType = sportNormalizationService.normalize(request.getSportName());
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : "General");
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://vaidebet.com/line/sport/event/" + oddsData.getMatchId());

        List<OddItem> oddItems = new ArrayList<>();

        for (VaidebetStakeGroupData group : oddsData.getGroups()) {
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

    public void mapStakeGroup(VaidebetStakeGroupData group, List<OddItem> oddItems, MatchCache cached, SportType sportType) {
        if (group == null || handlers == null) {
            return;
        }
        for (VaidebetMarketHandler handler : handlers) {
            if (handler.supports(group, sportType)) {
                handler.handle(group, cached, sportType, oddItems);
                break;
            }
        }
    }
}

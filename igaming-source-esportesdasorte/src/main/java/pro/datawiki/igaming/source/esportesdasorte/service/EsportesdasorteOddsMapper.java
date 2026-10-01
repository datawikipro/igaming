package pro.datawiki.igaming.source.esportesdasorte.service;

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
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteMatchOddsData;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeGroupData;
import pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteMarketHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class EsportesdasorteOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;
    private final List<EsportesdasorteMarketHandler> handlers;

    public EsportesdasorteOddsMapper() {
        this(null, null);
    }

    public EsportesdasorteOddsMapper(SportNormalizationService sportNormalizationService) {
        this(sportNormalizationService, null);
    }

    public EsportesdasorteOddsMapper(SportNormalizationService sportNormalizationService,
                                     List<EsportesdasorteMarketHandler> handlers) {
        this.sportNormalizationService = sportNormalizationService;
        List<EsportesdasorteMarketHandler> activeHandlers;
        if (handlers != null && !handlers.isEmpty()) {
            activeHandlers = new ArrayList<>(handlers);
        } else {
            activeHandlers = new ArrayList<>(createDefaultHandlers());
        }
        AnnotationAwareOrderComparator.sort(activeHandlers);
        this.handlers = Collections.unmodifiableList(activeHandlers);
    }

    private static List<EsportesdasorteMarketHandler> createDefaultHandlers() {
        return List.of(
                new pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteEsportsHandler(),
                new pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteStatsCornersHandler(),
                new pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteStatsCardsHandler(),
                new pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteDoubleChanceHandler(),
                new pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteBttsHandler(),
                new pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteTotalHandler(),
                new pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteHandicapHandler(),
                new pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteMatchResultHandler()
        );
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "esportesdasorte".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public List<EsportesdasorteMarketHandler> getHandlers() {
        return handlers;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, EsportesdasorteMatchOddsData oddsData) {
        if (cached == null || oddsData == null || oddsData.getGroups() == null || oddsData.getGroups().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("esportesdasorte");
        request.setRegions(List.of(BookmakerRegion.BR, BookmakerRegion.LATAM));
        request.setExternalEventId(String.valueOf(oddsData.getMatchId()));

        String sportName = cached.getSportName();
        SportType sportType = null;
        if (sportNormalizationService != null && sportName != null) {
            sportType = sportNormalizationService.normalize(sportName);
        }
        if (sportType == null && sportName != null) {
            sportType = resolveFallbackSportType(sportName);
        }
        if (sportType == null) {
            sportType = SportType.FOOTBALL;
        }
        if (sportName == null || sportName.isBlank()) {
            sportName = sportType.name();
        }

        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : "General");
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://esportesdasorte.com/line/sport/event/" + oddsData.getMatchId());

        List<OddItem> oddItems = new ArrayList<>();

        for (EsportesdasorteStakeGroupData group : oddsData.getGroups()) {
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

    private SportType resolveFallbackSportType(String sportName) {
        String lower = sportName.toLowerCase();
        if (lower.contains("cs") || lower.contains("counter-strike") || lower.contains("counter strike")) return SportType.CS2;
        if (lower.contains("dota")) return SportType.DOTA2;
        if (lower.contains("league of legends") || lower.contains("lol")) return SportType.LEAGUE_OF_LEGENDS;
        if (lower.contains("valorant")) return SportType.VALORANT;
        if (lower.contains("esport") || lower.contains("cyber") || lower.contains("кибер")) return SportType.ESPORTS;
        if (lower.contains("basket") || lower.contains("баскет")) return SportType.BASKETBALL;
        if (lower.contains("tennis") || lower.contains("теннис")) return SportType.TENNIS;
        if (lower.contains("hockey") || lower.contains("хоккей")) return SportType.HOCKEY;
        if (lower.contains("volley") || lower.contains("волей")) return SportType.VOLLEYBALL;
        return SportType.FOOTBALL;
    }

    public void mapStakeGroup(EsportesdasorteStakeGroupData group, List<OddItem> oddItems, MatchCache cached, SportType sportType) {
        if (group == null || handlers == null) {
            return;
        }
        for (EsportesdasorteMarketHandler handler : handlers) {
            if (handler.supports(group, sportType)) {
                handler.handle(group, cached, sportType, oddItems);
                break;
            }
        }
    }
}

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

    public VaidebetOddsMapper() {
        this(null, null);
    }

    public VaidebetOddsMapper(SportNormalizationService sportNormalizationService) {
        this(sportNormalizationService, null);
    }

    public VaidebetOddsMapper(SportNormalizationService sportNormalizationService,
                              List<VaidebetMarketHandler> handlers) {
        this.sportNormalizationService = sportNormalizationService;
        List<VaidebetMarketHandler> activeHandlers;
        if (handlers != null && !handlers.isEmpty()) {
            activeHandlers = new ArrayList<>(handlers);
        } else {
            activeHandlers = new ArrayList<>(createDefaultHandlers());
        }
        AnnotationAwareOrderComparator.sort(activeHandlers);
        this.handlers = Collections.unmodifiableList(activeHandlers);
    }

    private static List<VaidebetMarketHandler> createDefaultHandlers() {
        return List.of(
                new pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetEsportsHandler(),
                new pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetStatsCornersHandler(),
                new pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetStatsCardsHandler(),
                new pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetDoubleChanceHandler(),
                new pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetBttsHandler(),
                new pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetTotalHandler(),
                new pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetHandicapHandler(),
                new pro.datawiki.igaming.source.vaidebet.service.handler.VaidebetMatchResultHandler()
        );
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

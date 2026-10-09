package pro.datawiki.igaming.source.betano.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betano.dto.BetanoEvent;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;
import pro.datawiki.igaming.source.betano.strategy.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BetanoOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;
    private final List<BetanoMarketStrategy> strategies;

    @Autowired
    public BetanoOddsMapper(List<BetanoMarketStrategy> strategies,
                            @Autowired(required = false) SportNormalizationService sportNormalizationService) {
        this.strategies = (strategies != null && !strategies.isEmpty()) ? strategies : defaultStrategies();
        this.sportNormalizationService = sportNormalizationService;
    }

    public BetanoOddsMapper(SportNormalizationService sportNormalizationService) {
        this(defaultStrategies(), sportNormalizationService);
    }

    public BetanoOddsMapper() {
        this(defaultStrategies(), null);
    }

    private static List<BetanoMarketStrategy> defaultStrategies() {
        return List.of(
                new BetanoMatchResultStrategy(),
                new BetanoTotalStrategy(),
                new BetanoTeamTotalStrategy(),
                new BetanoHandicapStrategy(),
                new BetanoBttsStrategy(),
                new BetanoDoubleChanceStrategy(),
                new BetanoDrawNoBetStrategy(),
                new BetanoHalfTimeFullTimeStrategy()
        );
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "betano".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, BetanoEvent event) {
        if (cached == null || event == null) {
            return null;
        }

        List<BetanoMarket> markets = event.getMarkets();
        if (markets == null || markets.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("betano");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU, BookmakerRegion.LATAM));
        request.setExternalEventId(event.getResolvedId());
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : event.getResolvedSport());

        SportType sportType = sportNormalizationService != null ? sportNormalizationService.normalize(request.getSportName()) : SportType.FOOTBALL;
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : event.getResolvedLeague());
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(event.getResolvedIsLive() != null ? event.getResolvedIsLive() : Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://www.betano.com/match/" + event.getResolvedId());

        String team1 = cached.getTeam1();
        String team2 = cached.getTeam2();
        List<OddItem> oddItems = new ArrayList<>();

        for (BetanoMarket market : markets) {
            List<BetanoSelection> selections = market.getResolvedSelections();
            if (selections == null || selections.isEmpty()) {
                continue;
            }

            for (BetanoMarketStrategy strategy : strategies) {
                if (strategy.supports(market, team1, team2)) {
                    List<OddItem> mapped = strategy.map(market, sportType, team1, team2, cached);
                    if (mapped != null && !mapped.isEmpty()) {
                        oddItems.addAll(mapped);
                    }
                    break;
                }
            }
        }

        if (oddItems.isEmpty()) {
            return null;
        }

        request.setOdds(oddItems);
        return request;
    }
}

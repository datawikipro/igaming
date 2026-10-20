package pro.datawiki.igaming.source.digitain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.digitain.dto.DigitainMatchOddsData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;
import pro.datawiki.igaming.source.digitain.strategy.*;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DigitainOddsMapper {

    private final SportNormalizationService sportNormalizationService;
    private final List<DigitainMarketStrategy> strategies;

    @Autowired
    public DigitainOddsMapper(List<DigitainMarketStrategy> strategies,
                              @Autowired(required = false) SportNormalizationService sportNormalizationService) {
        this.strategies = (strategies != null && !strategies.isEmpty()) ? strategies : defaultStrategies();
        this.sportNormalizationService = sportNormalizationService;
    }

    public DigitainOddsMapper(SportNormalizationService sportNormalizationService) {
        this(defaultStrategies(), sportNormalizationService);
    }

    public DigitainOddsMapper() {
        this(defaultStrategies(), null);
    }

    private static List<DigitainMarketStrategy> defaultStrategies() {
        return List.of(
                new DigitainMatchResultStrategy(),
                new DigitainTotalStrategy(),
                new DigitainTeamTotalStrategy(),
                new DigitainHandicapStrategy(),
                new DigitainBttsStrategy(),
                new DigitainDoubleChanceStrategy(),
                new DigitainDrawNoBetStrategy(),
                new DigitainHalfTimeFullTimeStrategy()
        );
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, DigitainMatchOddsData oddsData) {
        if (cached == null || oddsData == null || oddsData.getGroups() == null || oddsData.getGroups().isEmpty()) {
            return null;
        }

        String bookmaker = (cached.getBookmaker() != null && !cached.getBookmaker().isEmpty())
                ? cached.getBookmaker()
                : "digitain";

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker(bookmaker);
        request.setRegions(List.of(BookmakerRegion.RU));
        request.setExternalEventId(String.valueOf(oddsData.getMatchId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : "Football");

        SportType sportType = (sportNormalizationService != null)
                ? sportNormalizationService.normalize(request.getSportName())
                : SportType.FOOTBALL;
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : "General");
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl(cached.getEventUrl() != null && !cached.getEventUrl().isEmpty()
                ? cached.getEventUrl()
                : "https://melbet.ru/line/sport/event/" + oddsData.getMatchId());

        String team1 = cached.getTeam1();
        String team2 = cached.getTeam2();
        List<OddItem> oddItems = new ArrayList<>();

        for (DigitainStakeGroupData group : oddsData.getGroups()) {
            if (group.getStakes() == null || group.getStakes().isEmpty()) {
                continue;
            }

            for (DigitainMarketStrategy strategy : strategies) {
                if (strategy.supports(group, team1, team2)) {
                    List<OddItem> mapped = strategy.map(group, sportType, team1, team2, cached);
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

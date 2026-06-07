package pro.datawiki.igaming.source.bet365.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.bet365.service.Bet365ApiClient.Bet365OddsResponse;
import pro.datawiki.igaming.source.bet365.service.Bet365ApiClient.Bet365Odd;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Bet365OddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(Bet365OddsResponse response, MatchCache cached, String fallbackSport, String fallbackLeague) {
        if (response == null || response.getOdds() == null || response.getOdds().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("bet365");
        request.setExternalEventId(response.getEventId());
        request.setStartTime(cached.getStartTime());
        
        String sportName = cached.getSportName() != null ? cached.getSportName() : fallbackSport;
        request.setSportName(sportName);

        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);

        String leagueName = cached.getLeagueName() != null ? cached.getLeagueName() : fallbackLeague;
        request.setLeagueName(leagueName);

        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(cached.getIsLive());
        request.setEventUrl("https://www.bet365.com/#/IP/EV" + response.getEventId());

        List<OddItem> oddsList = new ArrayList<>();
        for (Bet365Odd odd : response.getOdds()) {
            processOdd(response.getEventId(), odd, sportType, sportName, oddsList);
        }
        request.setOdds(oddsList);

        return request;
    }

    private void processOdd(String eventId, Bet365Odd odd, SportType sportType, String sportName, List<OddItem> oddsList) {
        String marketName = odd.getMarketName() != null ? odd.getMarketName() : "Unknown Market";
        String runnerName = odd.getOutcomeName() != null ? odd.getOutcomeName() : "Outcome " + odd.getCode();

        BetType betType = resolveBetType(odd, sportType, marketName, runnerName);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(eventId, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        item.setFactorId(odd.getCode());
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(odd.getValue());
        item.setBetType(betType);

        oddsList.add(item);
    }

    private BetType resolveBetType(Bet365Odd odd, SportType sportType, String marketName, String runnerName) {
        String mUpper = marketName.toUpperCase();
        String codeUpper = odd.getCode().toUpperCase();

        // 1. Result Markets (Winner/1X2) - supports both English and Swedish header names and column codes
        if (mUpper.contains("1X2") || mUpper.contains("MATCH WINNER") || mUpper.contains("RESULT") || mUpper.contains("VINNARE") || mUpper.equals("1") || mUpper.equals("2") || mUpper.equals("X")) {
            if ("1".equals(codeUpper)) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null);
            } else if ("X".equals(codeUpper) || "DRAW".equals(codeUpper) || "OAVGJORT".equals(codeUpper)) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("2".equals(codeUpper)) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null);
            }
        }

        // Fallback to resolver
        return betTypeResolver.resolve("bet365", sportType, mUpper, runnerName.toUpperCase(), 0.0);
    }

    private void logUnmapped(String eventId, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED BET365 MARKET: Event={}, Sport={}, Market={}, Runner={}",
                eventId, sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("bet365", sportName, runnerName, marketName, eventId);
    }
}

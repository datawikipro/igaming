package pro.datawiki.igaming.source.caesars.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.engine.american.AmericanOddsResolver;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.caesars.dto.CaesarsEventGroupResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaesarsOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final AmericanOddsResolver americanOddsResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(CaesarsEventGroupResponse.Event event,
                                                    CaesarsEventGroupResponse response,
                                                    String sportName,
                                                    String leagueName) {
        if (event == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("caesars");
        request.setExternalEventId(event.getId());

        if (event.getStartTime() != null) {
            try {
                request.setStartTime(Instant.parse(event.getStartTime()).toEpochMilli());
            } catch (Exception e) {
                log.warn("Failed to parse start date '{}'", event.getStartTime());
            }
        }

        request.setSportName(sportName);
        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);

        String team1 = null;
        String team2 = null;
        if (event.getName() != null) {
            String fullName = event.getName();
            String[] parts = fullName.split(" @ ");
            if (parts.length == 2) {
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else {
                parts = fullName.split("\\| ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                }
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive(Boolean.TRUE.equals(event.getStarted()));
        request.setEventUrl("https://sportsbook.caesars.com/us/mi/bet/event/" + event.getId());

        List<OddItem> oddsList = new ArrayList<>();
        if (event.getMarkets() != null) {
            for (CaesarsEventGroupResponse.Market market : event.getMarkets()) {
                if (market.getSelections() != null) {
                    for (CaesarsEventGroupResponse.Selection selection : market.getSelections()) {
                        processOutcome(event, market.getName(), market, selection, sportType, sportName, team1, team2, oddsList);
                    }
                }
            }
        }
        
        request.setOdds(oddsList);
        return request;
    }

    private void processOutcome(CaesarsEventGroupResponse.Event event,
                                String marketName,
                                CaesarsEventGroupResponse.Market market,
                                CaesarsEventGroupResponse.Selection selection,
                                SportType sportType,
                                String sportName,
                                String team1,
                                String team2,
                                List<OddItem> oddsList) {
        
        Double decimalOdds = null;
        if (selection.getPrice() != null) {
            decimalOdds = selection.getPrice().getD();
            if (decimalOdds == null && selection.getPrice().getA() != null) {
                int am = selection.getPrice().getA();
                if (am > 0) decimalOdds = (am / 100.0) + 1.0;
                else if (am < 0) decimalOdds = (100.0 / Math.abs(am)) + 1.0;
            }
        }
        if (decimalOdds == null || decimalOdds <= 1.0) return;

        String runnerName = selection.getName();
        if (runnerName == null) runnerName = "Outcome " + selection.hashCode();

        Double line = market.getLine();
        BetType betType = resolveBetType(marketName, runnerName, line, sportType, team1, team2);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(event, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        item.setFactorId(market.getId() + "_" + selection.getId());
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    private BetType resolveBetType(String marketName,
                                   String runnerName,
                                   Double line,
                                   SportType sportType,
                                   String team1,
                                   String team2) {
        return americanOddsResolver.resolve("caesars", marketName, runnerName, line, sportType, team1, team2);
    }

    private void logUnmapped(CaesarsEventGroupResponse.Event event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED CAESARS MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event.getId(), sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("caesars", sportName, runnerName, marketName, event.getId());
    }
}

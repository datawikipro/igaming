package pro.datawiki.igaming.source.betmgm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.betmgm.dto.BetMGMEventGroupResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BetMGMOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(BetMGMEventGroupResponse.Fixture event,
                                                    BetMGMEventGroupResponse response,
                                                    String sportName,
                                                    String leagueName) {
        if (event == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("betmgm");
        request.setExternalEventId(event.getId());

        if (event.getStartDate() != null) {
            try {
                request.setStartTime(Instant.parse(event.getStartDate()).toEpochMilli());
            } catch (Exception e) {
                log.warn("Failed to parse start date '{}'", event.getStartDate());
            }
        }

        request.setSportName(sportName);
        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);

        String team1 = null;
        String team2 = null;
        if (event.getName() != null && event.getName().getValue() != null) {
            String fullName = event.getName().getValue();
            String[] parts = fullName.split(" @ ");
            if (parts.length == 2) {
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else {
                parts = fullName.split(" vs ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                }
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive(false);
        request.setEventUrl("https://sports.betmgm.com/en/sports/events/" + event.getId());

        List<OddItem> oddsList = new ArrayList<>();
        if (event.getOptionMarkets() != null) {
            for (BetMGMEventGroupResponse.OptionMarket market : event.getOptionMarkets()) {
                if (market.getOptions() != null) {
                    for (BetMGMEventGroupResponse.Option option : market.getOptions()) {
                        processOutcome(event, market.getName(), market, option, sportType, sportName, team1, team2, oddsList);
                    }
                }
            }
        }
        
        if (event.getGames() != null) {
            for (BetMGMEventGroupResponse.Game game : event.getGames()) {
                if (game.getResults() != null) {
                    for (BetMGMEventGroupResponse.OptionMarket market : game.getResults()) {
                         if (market.getOptions() != null) {
                             for (BetMGMEventGroupResponse.Option option : market.getOptions()) {
                                 processOutcome(event, market.getName(), market, option, sportType, sportName, team1, team2, oddsList);
                             }
                         }
                    }
                }
            }
        }
        
        request.setOdds(oddsList);
        return request;
    }

    private void processOutcome(BetMGMEventGroupResponse.Fixture event,
                                String marketName,
                                BetMGMEventGroupResponse.OptionMarket market,
                                BetMGMEventGroupResponse.Option selection,
                                SportType sportType,
                                String sportName,
                                String team1,
                                String team2,
                                List<OddItem> oddsList) {
        
        Double decimalOdds = selection.getPrice();
        if (decimalOdds == null && selection.getAmericanPrice() != null) {
            double am = selection.getAmericanPrice();
            if (am > 0) decimalOdds = (am / 100.0) + 1.0;
            else if (am < 0) decimalOdds = (100.0 / Math.abs(am)) + 1.0;
        }
        if (decimalOdds == null || decimalOdds <= 1.0) return;

        String runnerName = selection.getName() != null ? selection.getName().getValue() : null;
        if (runnerName == null) runnerName = "Outcome " + selection.hashCode();

        BetType betType = resolveBetType(marketName, runnerName, sportType, team1, team2);

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
                                   SportType sportType,
                                   String team1,
                                   String team2) {
        if (marketName == null) return null;
        String mUpper = marketName.toUpperCase();
        
        Double line = 0.0;
        if (runnerName.contains(" ") && runnerName.matches(".*[\\+\\-]\\d+\\.?\\d*.*")) {
             String[] parts = runnerName.split(" ");
             try {
                 line = Double.parseDouble(parts[parts.length - 1]);
             } catch (Exception ignored) {}
        }

        // 1. Result Markets (Moneyline)
        if (mUpper.contains("MONEY LINE") || mUpper.contains("MATCH RESULT") || mUpper.contains("3-WAY")) {
            if (team1 != null && runnerName.equalsIgnoreCase(team1)) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if (team2 != null && runnerName.equalsIgnoreCase(team2)) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2_2WAY, null);
            } else if (runnerName.toUpperCase().contains("DRAW") || runnerName.toUpperCase().contains("TIE")) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            }
        }

        // 2. Totals Markets (Over/Under)
        if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("O/U")) {
            if (runnerName.toUpperCase().startsWith("OVER")) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, line, false, null);
            } else if (runnerName.toUpperCase().startsWith("UNDER")) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, line, false, null);
            }
        }

        // 3. Spreads/Handicaps
        if (mUpper.contains("SPREAD") || mUpper.contains("HANDICAP") || mUpper.contains("RUN LINE") || mUpper.contains("PUCK LINE")) {
            if (team1 != null && runnerName.contains(team1)) {
                return new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Team.TEAM1, line, false, null);
            } else if (team2 != null && runnerName.contains(team2)) {
                return new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Team.TEAM2, line, false, null);
            }
        }

        return betTypeResolver.resolve("betmgm", sportType, mUpper, runnerName.toUpperCase(), line);
    }

    private void logUnmapped(BetMGMEventGroupResponse.Fixture event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED BETMGM MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event.getId(), sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("betmgm", sportName, runnerName, marketName, event.getId());
    }
}

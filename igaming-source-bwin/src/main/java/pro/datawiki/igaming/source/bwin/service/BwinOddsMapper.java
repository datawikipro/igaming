package pro.datawiki.igaming.source.bwin.service;

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
import pro.datawiki.igaming.source.bwin.dto.entain.EntainEventDetailsResponse;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainFixture;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainOptionMarket;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainOption;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BwinOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(EntainEventDetailsResponse response, String fallbackSport, String fallbackLeague) {
        if (response == null || response.getFixture() == null) {
            return null;
        }

        EntainFixture fixture = response.getFixture();
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("bwin");
        request.setExternalEventId(String.valueOf(fixture.getSourceId()));

        if (fixture.getStartDate() != null) {
            try {
                request.setStartTime(Instant.parse(fixture.getStartDate()).toEpochMilli());
            } catch (Exception e) {
                log.warn("Failed to parse start time '{}'", fixture.getStartDate());
            }
        }

        String sportName = fallbackSport;
        request.setSportName(sportName);

        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);

        String leagueName = fallbackLeague;
        request.setLeagueName(leagueName);

        String team1 = "Team 1";
        String team2 = "Team 2";
        if (fixture.getName() != null && fixture.getName().getValue() != null) {
            String fixtureName = fixture.getName().getValue();
            String[] parts = fixtureName.split(" - ");
            if (parts.length == 2) {
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else {
                parts = fixtureName.split(" vs ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                }
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive(true); // Assuming Live events if hitting highlights
        request.setEventUrl("https://sports.bwin.com/en/sports/events/" + fixture.getSourceId());

        List<OddItem> oddsList = new ArrayList<>();
        if (fixture.getOptionMarkets() != null) {
            for (EntainOptionMarket market : fixture.getOptionMarkets()) {
                if (market.getOptions() != null) {
                    for (EntainOption option : market.getOptions()) {
                        processOption(fixture, market, option, sportType, sportName, oddsList);
                    }
                }
            }
        }
        request.setOdds(oddsList);

        return request;
    }

    private void processOption(EntainFixture fixture, EntainOptionMarket market, EntainOption option, 
                                SportType sportType, String sportName, List<OddItem> oddsList) {
        if (option.getPrice() == null || option.getPrice().getOdds() == null) return;

        double decimalOdds = option.getPrice().getOdds();
        String marketName = market.getName() != null ? market.getName().getValue() : "Unknown Market";
        String runnerName = option.getName() != null ? option.getName().getValue() : "Option " + option.getId();

        BetType betType = resolveBetType(market, option, sportType, marketName, runnerName);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(fixture, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        item.setFactorId(String.valueOf(option.getId()));
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    private BetType resolveBetType(EntainOptionMarket market, EntainOption option, SportType sportType, 
                                   String marketName, String runnerName) {
        String mUpper = marketName.toUpperCase();
        String rUpper = runnerName.toUpperCase();

        Double line = 0.0;

        // Extract line from runnerName (e.g., "Over 2.5")
        try {
            String[] parts = runnerName.split(" ");
            for (String part : parts) {
                part = part.replace(",", ".");
                if (part.matches("[-+]?[0-9]*\\.?[0-9]+")) {
                    line = Double.parseDouble(part);
                    break;
                }
            }
        } catch (Exception ignored) {}

        // 1. Result Markets (Moneyline, 1X2)
        if (mUpper.contains("MATCH RESULT") || mUpper.contains("1X2")) {
            if ("1".equals(rUpper) || rUpper.contains("1") && !rUpper.contains("X")) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null);
            } else if ("X".equals(rUpper) || rUpper.contains("DRAW")) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("2".equals(rUpper) || rUpper.contains("2") && !rUpper.contains("X")) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null);
            }
        }

        // 2. Totals Markets
        if (mUpper.contains("TOTAL GOALS") || mUpper.contains("OVER/UNDER")) {
            if (rUpper.contains("OVER")) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, line, false, null);
            } else if (rUpper.contains("UNDER")) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, line, false, null);
            }
        }

        // Fallback to strict DB matching
        return betTypeResolver.resolve("bwin", sportType, mUpper, rUpper, line);
    }

    private void logUnmapped(EntainFixture fixture, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED BWIN MARKET: Event={}, Sport={}, Market={}, Runner={}",
                fixture.getSourceId(), sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("bwin", sportName, runnerName, marketName, String.valueOf(fixture.getSourceId()));
    }
}

package pro.datawiki.igaming.source.unibet.service;

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
import pro.datawiki.igaming.source.unibet.dto.kambi.KambiBetOffer;
import pro.datawiki.igaming.source.unibet.dto.kambi.KambiEvent;
import pro.datawiki.igaming.source.unibet.dto.kambi.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.unibet.dto.kambi.KambiOutcome;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UnibetOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(KambiEventDetailsResponse response, String fallbackSport, String fallbackLeague) {
        if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
            return null;
        }

        KambiEvent event = response.getEvents().get(0);
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("unibet");
        request.setExternalEventId(String.valueOf(event.getId()));

        if (event.getStart() != null) {
            try {
                request.setStartTime(Instant.parse(event.getStart()).toEpochMilli());
            } catch (Exception e) {
                log.warn("Failed to parse start time '{}'", event.getStart());
            }
        }

        String sportName = fallbackSport;
        if (event.getPath() != null && !event.getPath().isEmpty()) {
            sportName = event.getPath().get(0).getName();
        }
        request.setSportName(sportName);

        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);

        String leagueName = event.getGroup();
        if (leagueName == null && event.getPath() != null && event.getPath().size() > 1) {
            leagueName = event.getPath().get(event.getPath().size() - 1).getName();
        }
        if (leagueName == null) leagueName = fallbackLeague;
        request.setLeagueName(leagueName);

        String team1 = event.getHomeName();
        String team2 = event.getAwayName();
        if (team1 == null || team2 == null) {
            if (event.getName() != null) {
                String[] parts = event.getName().split(" - ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                } else {
                    parts = event.getName().split(" vs ");
                    if (parts.length == 2) {
                        team1 = parts[0].trim();
                        team2 = parts[1].trim();
                    }
                }
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive("STARTED".equalsIgnoreCase(event.getState()));
        request.setEventUrl("https://www.unibet.com/betting/sports/event/" + event.getId());

        List<OddItem> oddsList = new ArrayList<>();
        if (response.getBetoffers() != null) {
            for (KambiBetOffer betOffer : response.getBetoffers()) {
                if (betOffer.getOutcomes() != null) {
                    for (KambiOutcome outcome : betOffer.getOutcomes()) {
                        processOutcome(event, betOffer, outcome, sportType, sportName, oddsList);
                    }
                }
            }
        }
        request.setOdds(oddsList);

        return request;
    }

    private void processOutcome(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome, 
                                SportType sportType, String sportName, List<OddItem> oddsList) {
        if (outcome.getOdds() == null) return;

        double decimalOdds = outcome.getOdds() / 1000.0;
        String marketName = betOffer.getCriterion() != null ? betOffer.getCriterion().getLabel() : "Unknown Market";
        String englishMarket = betOffer.getCriterion() != null && betOffer.getCriterion().getEnglishLabel() != null 
                ? betOffer.getCriterion().getEnglishLabel() 
                : marketName;
        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        BetType betType = resolveBetType(betOffer, outcome, sportType, englishMarket, runnerName);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            // Log as unmapped to help system mapping quality tracking
            logUnmapped(event, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        item.setFactorId(String.valueOf(outcome.getId()));
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    private BetType resolveBetType(KambiBetOffer betOffer, KambiOutcome outcome, SportType sportType, 
                                   String marketName, String runnerName) {
        String mUpper = marketName.toUpperCase();
        String typeUpper = outcome.getType() != null ? outcome.getType().toUpperCase() : "";

        Double line = outcome.getLine();
        if (line == null) line = 0.0;

        // 1. Result Markets (Moneyline, 1X2)
        if (mUpper.contains("MATCH") || mUpper.contains("RESULT") || mUpper.contains("MONEYLINE") || mUpper.contains("1X2")) {
            boolean hasDraw = betOffer.getOutcomes().stream()
                    .anyMatch(o -> "OT_DRAW".equalsIgnoreCase(o.getType()) || (o.getLabel() != null && o.getLabel().toUpperCase().contains("DRAW")));
            
            if ("OT_ONE".equals(typeUpper)) {
                return new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("OT_DRAW".equals(typeUpper)) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("OT_TWO".equals(typeUpper)) {
                return new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            }
        }

        // 2. Totals Markets
        if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER")) {
            if ("OT_OVER".equals(typeUpper)) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, line, false, null);
            } else if ("OT_UNDER".equals(typeUpper)) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, line, false, null);
            }
        }

        // 3. Spreads/Handicaps
        if (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD")) {
            if ("OT_ONE".equals(typeUpper)) {
                return new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, line, false, null);
            } else if ("OT_TWO".equals(typeUpper)) {
                return new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, line, false, null);
            }
        }

        // Fallback to strict DB matching
        return betTypeResolver.resolve("unibet", sportType, mUpper, runnerName.toUpperCase(), line);
    }

    private void logUnmapped(KambiEvent event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED UNIBET MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event.getId(), sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("unibet", sportName, runnerName, marketName, String.valueOf(event.getId()));
    }
}

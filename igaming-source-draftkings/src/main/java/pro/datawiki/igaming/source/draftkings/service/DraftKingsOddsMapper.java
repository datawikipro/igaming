package pro.datawiki.igaming.source.draftkings.service;

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
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsEventGroupResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DraftKingsOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final AmericanOddsResolver americanOddsResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(DraftKingsEventGroupResponse.DraftKingsEvent event,
                                                    DraftKingsEventGroupResponse response,
                                                    String sportName,
                                                    String leagueName) {
        if (event == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("draftkings");
        request.setExternalEventId(event.getEventId());

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

        String team1 = event.getTeamName1();
        String team2 = event.getTeamName2();
        if (team1 == null || team2 == null) {
            if (event.getName() != null) {
                String[] parts = event.getName().split(" @ ");
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

        request.setIsLive("Started".equalsIgnoreCase(event.getEventStatus()));
        request.setEventUrl("https://sportsbook.draftkings.com/event/" + event.getEventId());

        List<OddItem> oddsList = new ArrayList<>();
        if (response.getEventGroup() != null && response.getEventGroup().getOfferCategories() != null) {
            for (DraftKingsEventGroupResponse.DraftKingsOfferCategory category : response.getEventGroup().getOfferCategories()) {
                if (category.getOfferSubcategoryDescriptors() != null) {
                    for (DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor descriptor : category.getOfferSubcategoryDescriptors()) {
                        String marketName = descriptor.getName();
                        if (descriptor.getOfferSubcategory() != null && descriptor.getOfferSubcategory().getOffers() != null) {
                            for (List<DraftKingsEventGroupResponse.DraftKingsOffer> offerList : descriptor.getOfferSubcategory().getOffers()) {
                                for (DraftKingsEventGroupResponse.DraftKingsOffer offer : offerList) {
                                    if (event.getEventId().equals(offer.getEventId()) && offer.getOutcomes() != null) {
                                        for (DraftKingsEventGroupResponse.DraftKingsOutcome outcome : offer.getOutcomes()) {
                                            processOutcome(event, marketName, offer, outcome, sportType, sportName, team1, team2, oddsList);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        request.setOdds(oddsList);
        return request;
    }

    private void processOutcome(DraftKingsEventGroupResponse.DraftKingsEvent event,
                                String marketName,
                                DraftKingsEventGroupResponse.DraftKingsOffer offer,
                                DraftKingsEventGroupResponse.DraftKingsOutcome outcome,
                                SportType sportType,
                                String sportName,
                                String team1,
                                String team2,
                                List<OddItem> oddsList) {
        double decimalOdds = outcome.getOddsDecimal();
        if (decimalOdds <= 0.0) {
            if (outcome.getOddsAmerican() != null) {
                try {
                    int am = Integer.parseInt(outcome.getOddsAmerican().replace("+", "").trim());
                    if (am > 0) {
                        decimalOdds = (am / 100.0) + 1.0;
                    } else if (am < 0) {
                        decimalOdds = (100.0 / Math.abs(am)) + 1.0;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse American odds: {}", outcome.getOddsAmerican());
                }
            }
        }
        if (decimalOdds <= 1.0) return;

        String runnerName = outcome.getParticipant() != null ? outcome.getParticipant() : outcome.getLabel();
        if (runnerName == null) runnerName = "Outcome " + outcome.hashCode();

        BetType betType = resolveBetType(marketName, outcome, sportType, team1, team2);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(event, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        item.setFactorId(offer.getOfferId() + "_" + outcome.hashCode());
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    private BetType resolveBetType(String marketName,
                                   DraftKingsEventGroupResponse.DraftKingsOutcome outcome,
                                   SportType sportType,
                                   String team1,
                                   String team2) {
        String participant = outcome.getParticipant();
        String label = outcome.getLabel();

        Double line = null;
        if (outcome.getLine() != null) {
            try {
                line = Double.parseDouble(outcome.getLine().replace("+", "").trim());
            } catch (Exception ignored) {}
        }

        String runnerName = participant != null ? participant : label;
        return americanOddsResolver.resolve("draftkings", marketName, runnerName, line, sportType, team1, team2);
    }

    private void logUnmapped(DraftKingsEventGroupResponse.DraftKingsEvent event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED DRAFTKINGS MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event.getEventId(), sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("draftkings", sportName, runnerName, marketName, event.getEventId());
    }
}

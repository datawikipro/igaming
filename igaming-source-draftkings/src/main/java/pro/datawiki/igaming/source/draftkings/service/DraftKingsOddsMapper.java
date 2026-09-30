package pro.datawiki.igaming.source.draftkings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsEventGroupResponse;
import pro.datawiki.igaming.source.draftkings.service.mapper.DraftKingsMarketContext;
import pro.datawiki.igaming.source.draftkings.service.mapper.DraftKingsMarketHandler;
import pro.datawiki.igaming.source.draftkings.service.mapper.DraftKingsScopeResolver;
import pro.datawiki.igaming.source.draftkings.service.mapper.DraftKingsStatTypeResolver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DraftKingsOddsMapper extends AbstractBetTypeMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;
    private final DraftKingsScopeResolver scopeResolver;
    private final DraftKingsStatTypeResolver statTypeResolver;
    private final List<DraftKingsMarketHandler> marketHandlers;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "draftkings".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(DraftKingsEventGroupResponse.DraftKingsEvent event,
                                                    DraftKingsEventGroupResponse response,
                                                    String sportName,
                                                    String leagueName) {
        if (event == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("draftkings");
        request.setRegions(List.of(BookmakerRegion.US, BookmakerRegion.GLOBAL));
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
                    team2 = parts[0].trim();
                    team1 = parts[1].trim();
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
        if (response != null && response.getEventGroup() != null && response.getEventGroup().getOfferCategories() != null) {
            for (DraftKingsEventGroupResponse.DraftKingsOfferCategory category : response.getEventGroup().getOfferCategories()) {
                String categoryName = category.getName();
                if (category.getOfferSubcategoryDescriptors() != null) {
                    for (DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor descriptor : category.getOfferSubcategoryDescriptors()) {
                        String marketName = descriptor.getName();
                        if (descriptor.getOfferSubcategory() != null && descriptor.getOfferSubcategory().getOffers() != null) {
                            for (List<DraftKingsEventGroupResponse.DraftKingsOffer> offerList : descriptor.getOfferSubcategory().getOffers()) {
                                for (DraftKingsEventGroupResponse.DraftKingsOffer offer : offerList) {
                                    if (event.getEventId().equals(offer.getEventId()) && offer.getOutcomes() != null) {
                                        for (DraftKingsEventGroupResponse.DraftKingsOutcome outcome : offer.getOutcomes()) {
                                            processOutcome(event, categoryName, marketName, offer, outcome, sportType, sportName, leagueName, team1, team2, oddsList);
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
                                String categoryName,
                                String marketName,
                                DraftKingsEventGroupResponse.DraftKingsOffer offer,
                                DraftKingsEventGroupResponse.DraftKingsOutcome outcome,
                                SportType sportType,
                                String sportName,
                                String leagueName,
                                String team1,
                                String team2,
                                List<OddItem> oddsList) {
        double decimalOdds = outcome.getOddsDecimal();
        if (decimalOdds <= 0.0 && outcome.getOddsAmerican() != null) {
            decimalOdds = americanToDecimal(outcome.getOddsAmerican());
        }
        if (decimalOdds <= 1.0) return;

        String runnerName = outcome.getParticipant() != null ? outcome.getParticipant() : outcome.getLabel();
        if (runnerName == null) runnerName = "Outcome " + outcome.hashCode();

        Double line = parseLine(outcome.getLine());
        BetScope scope = scopeResolver.resolve(categoryName, marketName);
        StatType statType = statTypeResolver.resolve(categoryName, marketName, sportType);

        DraftKingsMarketContext ctx = DraftKingsMarketContext.builder()
                .event(event)
                .categoryName(categoryName)
                .marketName(marketName)
                .offer(offer)
                .outcome(outcome)
                .sportType(sportType)
                .sportName(sportName)
                .leagueName(leagueName)
                .team1(team1)
                .team2(team2)
                .decimalOdds(decimalOdds)
                .line(line)
                .runnerName(runnerName)
                .scope(scope)
                .statType(statType)
                .build();

        BetType betType = null;
        for (DraftKingsMarketHandler handler : marketHandlers) {
            if (handler.supports(ctx)) {
                betType = handler.map(ctx);
                if (betType != null) {
                    break;
                }
            }
        }

        // Fallback to BetTypeResolverService if not mapped by OOP handlers
        if (betType == null && betTypeResolver != null) {
            String mUpper = marketName != null ? marketName.toUpperCase() : "";
            betType = betTypeResolver.resolve("draftkings", sportType, mUpper, runnerName.toUpperCase(), line != null ? line : 0.0);
        }

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(event, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        String safeRunner = runnerName.replaceAll("[^a-zA-Z0-9_+.-]", "_");
        String safeLine = line != null ? ("_" + line) : "";
        item.setFactorId(offer.getOfferId() + "_" + safeRunner + safeLine);
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    public static double americanToDecimal(String americanStr) {
        if (americanStr == null || americanStr.isBlank()) return 0.0;
        try {
            int am = Integer.parseInt(americanStr.replace("+", "").trim());
            if (am > 0) {
                return Math.round(((am / 100.0) + 1.0) * 1000.0) / 1000.0;
            } else if (am < 0) {
                return Math.round(((100.0 / Math.abs(am)) + 1.0) * 1000.0) / 1000.0;
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    private Double parseLine(String lineStr) {
        if (lineStr == null || lineStr.isBlank()) return null;
        try {
            return Double.parseDouble(lineStr.replace("+", "").trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void logUnmapped(DraftKingsEventGroupResponse.DraftKingsEvent event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED DRAFTKINGS MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event != null ? event.getEventId() : "null", sportName, marketName, runnerName);
        if (unmappedBetService != null && event != null) {
            unmappedBetService.saveAndNotify("draftkings", sportName, runnerName, marketName, event.getEventId());
        }
    }
}

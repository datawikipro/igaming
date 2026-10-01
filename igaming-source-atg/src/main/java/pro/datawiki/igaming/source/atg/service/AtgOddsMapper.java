package pro.datawiki.igaming.source.atg.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.atg.service.handler.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class AtgOddsMapper extends AbstractBetTypeMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;
    private final List<AtgMarketHandler> handlers;

    @Autowired
    public AtgOddsMapper(UnmappedBetService unmappedBetService,
                         SportNormalizationService sportNormalizationService,
                         BetTypeResolverService betTypeResolver,
                         @Autowired(required = false) List<AtgMarketHandler> handlers) {
        this.unmappedBetService = unmappedBetService;
        this.sportNormalizationService = sportNormalizationService;
        this.betTypeResolver = betTypeResolver;
        if (handlers == null || handlers.isEmpty()) {
            List<AtgMarketHandler> defaultHandlers = new ArrayList<>(List.of(
                    new AtgEsportsHandler(),
                    new AtgStatsCornersHandler(),
                    new AtgStatsCardsHandler(),
                    new AtgDoubleChanceHandler(),
                    new AtgBttsHandler(),
                    new AtgDrawNoBetHandler(),
                    new AtgTotalHandler(),
                    new AtgHandicapHandler(),
                    new AtgMoneylineHandler()
            ));
            AnnotationAwareOrderComparator.sort(defaultHandlers);
            this.handlers = Collections.unmodifiableList(defaultHandlers);
        } else {
            List<AtgMarketHandler> sorted = new ArrayList<>(handlers);
            AnnotationAwareOrderComparator.sort(sorted);
            this.handlers = Collections.unmodifiableList(sorted);
        }
    }

    public AtgOddsMapper(UnmappedBetService unmappedBetService,
                         SportNormalizationService sportNormalizationService,
                         BetTypeResolverService betTypeResolver) {
        this(unmappedBetService, sportNormalizationService, betTypeResolver, null);
    }

    public List<AtgMarketHandler> getHandlers() {
        return handlers;
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "atg".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        if (m == null || o == null) return null;
        String mLower = m.toLowerCase(Locale.ROOT);

        StatType statType = StatType.MATCH;
        if (mLower.contains("corner") || mLower.contains("hörn")) {
            statType = StatType.CORNERS;
        } else if (mLower.contains("card") || mLower.contains("booking") || mLower.contains("kort")) {
            statType = StatType.YELLOW_CARDS;
        } else if (mLower.contains("round")) {
            statType = StatType.ROUNDS;
        } else if (mLower.contains("map")) {
            statType = StatType.MAPS;
        }

        BetScope scope = BetScope.FULL_MATCH;
        if (mLower.contains("half1") || mLower.contains("1st_half") || mLower.contains("first_half")) {
            scope = BetScope.HALF_1;
        } else if (mLower.contains("half2") || mLower.contains("2nd_half") || mLower.contains("second_half")) {
            scope = BetScope.HALF_2;
        }

        if (mLower.contains("moneyline") || mLower.contains("1x2") || mLower.contains("result")) {
            return map1X2Record(o, scope, statType);
        } else if (mLower.contains("handicap") || mLower.contains("spread")) {
            return mapHandicapRecord(o, scope, statType, true, param);
        } else if (mLower.contains("total")) {
            return mapTotalRecord(o, scope, BetSubject.MATCH, statType, true, param);
        } else if (mLower.contains("double_chance") || mLower.contains("dc")) {
            return map1X2DCRecord(o, scope, statType);
        } else if (mLower.contains("btts") || mLower.contains("both_teams_to_score")) {
            return new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS,
                    "yes".equalsIgnoreCase(o) ? BinaryMarketBet.Outcome.YES : BinaryMarketBet.Outcome.NO, statType);
        } else if (mLower.contains("dnb") || mLower.contains("draw_no_bet")) {
            return mapHandicapRecord(o, scope, statType, true, 0.0);
        }
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, List<KambiBetOffer> betOffers) {
        if (cached == null || betOffers == null || betOffers.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("atg");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(cached.getExternalId());
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : "General");

        SportType sportType = sportNormalizationService.normalize(request.getSportName());
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : "League");
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(cached.getIsLive());
        request.setStartTime(cached.getStartTime());
        request.setEventUrl(cached.getEventUrl() != null ? cached.getEventUrl() : "https://www.atg.se/event/" + cached.getExternalId());

        KambiEvent mockEvent = new KambiEvent();
        try {
            mockEvent.setId(Long.parseLong(cached.getExternalId()));
        } catch (Exception ignored) {}
        mockEvent.setName(cached.getTeam1() + " vs " + cached.getTeam2());
        mockEvent.setHomeName(cached.getTeam1());
        mockEvent.setAwayName(cached.getTeam2());

        List<OddItem> oddsList = new ArrayList<>();
        for (KambiBetOffer betOffer : betOffers) {
            processBetOffer(mockEvent, betOffer, sportType, request.getSportName(), oddsList);
        }
        request.setOdds(oddsList);
        return request;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(KambiEventDetailsResponse response, String fallbackSport, String fallbackLeague) {
        if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
            return null;
        }

        KambiEvent event = response.getEvents().get(0);
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("atg");
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
        request.setEventUrl("https://www.atg.se/event/" + event.getId());

        List<OddItem> oddsList = new ArrayList<>();
        if (response.getBetoffers() != null) {
            for (KambiBetOffer betOffer : response.getBetoffers()) {
                processBetOffer(event, betOffer, sportType, sportName, oddsList);
            }
        }
        request.setOdds(oddsList);

        return request;
    }

    private void processBetOffer(KambiEvent event, KambiBetOffer betOffer, SportType sportType,
                                 String sportName, List<OddItem> oddsList) {
        if (betOffer == null || betOffer.getOutcomes() == null) return;
        String marketName = betOffer.getCriterion() != null ? betOffer.getCriterion().getLabel() : "Unknown Market";
        String englishMarket = betOffer.getCriterion() != null && betOffer.getCriterion().getEnglishLabel() != null 
                ? betOffer.getCriterion().getEnglishLabel() 
                : marketName;

        AtgMarketHandler matchedHandler = null;
        for (AtgMarketHandler handler : handlers) {
            if (handler.supports(betOffer, englishMarket, sportType)) {
                matchedHandler = handler;
                break;
            }
        }

        if (matchedHandler != null) {
            int beforeSize = oddsList.size();
            matchedHandler.handleOffer(event, betOffer, englishMarket, sportType, oddsList);
            if (oddsList.size() > beforeSize) {
                return;
            }
        }

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            processOutcomeFallback(event, betOffer, outcome, sportType, sportName, englishMarket, marketName, oddsList);
        }
    }

    private void processOutcomeFallback(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome, 
                                        SportType sportType, String sportName, String englishMarket,
                                        String marketName, List<OddItem> oddsList) {
        if (outcome.getOdds() == null) return;

        double decimalOdds = outcome.getOdds() / 1000.0;
        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        BetType betType = resolveBetType(betOffer, outcome, sportType, englishMarket, runnerName);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
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
        String mUpper = marketName.toUpperCase(Locale.ROOT);
        String typeUpper = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";

        Double line = outcome.getLine();
        if (line == null) line = 0.0;

        // 1. Result Markets (Moneyline, 1X2)
        if (mUpper.contains("MATCH") || mUpper.contains("RESULT") || mUpper.contains("MONEYLINE") || mUpper.contains("1X2")) {
            boolean hasDraw = betOffer.getOutcomes().stream()
                    .anyMatch(o -> "OT_DRAW".equalsIgnoreCase(o.getType()) || (o.getLabel() != null && o.getLabel().toUpperCase(Locale.ROOT).contains("DRAW")));
            
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

        return betTypeResolver.resolve("atg", sportType, mUpper, runnerName.toUpperCase(Locale.ROOT), line);
    }

    private void logUnmapped(KambiEvent event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED atg MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event != null ? event.getId() : "null", sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("atg", sportName, runnerName, marketName, event != null ? String.valueOf(event.getId()) : "0");
    }
}
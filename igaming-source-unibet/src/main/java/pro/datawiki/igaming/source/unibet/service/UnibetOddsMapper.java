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
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
@RequiredArgsConstructor
public class UnibetOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, List<KambiBetOffer> betOffers) {
        if (cached == null || betOffers == null || betOffers.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("unibet");
        request.setRegions(List.of(pro.datawiki.igaming.dto.BookmakerRegion.GLOBAL, pro.datawiki.igaming.dto.BookmakerRegion.EU));
        request.setExternalEventId(cached.getExternalId());
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : "General");

        SportType sportType = sportNormalizationService.normalize(request.getSportName());
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : "League");
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(cached.getIsLive());
        request.setStartTime(cached.getStartTime());
        request.setEventUrl(cached.getEventUrl() != null ? cached.getEventUrl() : "https://www.unibet.com/betting/sports/event/" + cached.getExternalId());

        KambiEvent mockEvent = new KambiEvent();
        try {
            mockEvent.setId(Long.parseLong(cached.getExternalId()));
        } catch (Exception ignored) {}
        mockEvent.setName(cached.getTeam1() + " vs " + cached.getTeam2());
        mockEvent.setHomeName(cached.getTeam1());
        mockEvent.setAwayName(cached.getTeam2());

        List<OddItem> oddsList = new ArrayList<>();
        for (KambiBetOffer betOffer : betOffers) {
            if (betOffer.getOutcomes() != null) {
                for (KambiOutcome outcome : betOffer.getOutcomes()) {
                    processOutcome(mockEvent, betOffer, outcome, sportType, request.getSportName(), oddsList);
                }
            }
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

        BetType betType = resolveBetType(event, betOffer, outcome, sportType, englishMarket, runnerName);
        if (betType == null && !marketName.equals(englishMarket)) {
            betType = resolveBetType(event, betOffer, outcome, sportType, marketName, runnerName);
        }

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

    public BetType resolveBetType(KambiBetOffer betOffer, KambiOutcome outcome, SportType sportType, 
                                  String marketName, String runnerName) {
        return resolveBetType(null, betOffer, outcome, sportType, marketName, runnerName);
    }

    public BetType resolveBetType(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome, SportType sportType, 
                                  String marketName, String runnerName) {
        if (marketName == null) marketName = "";
        String mUpper = marketName.toUpperCase(Locale.ROOT);
        String typeUpper = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String labelUpper = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";
        String participant = outcome.getParticipant() != null ? outcome.getParticipant().toUpperCase(Locale.ROOT) : "";

        Double line = normalizeLine(outcome.getLine());

        String home = event != null && event.getHomeName() != null ? event.getHomeName().trim().toUpperCase(Locale.ROOT) : null;
        String away = event != null && event.getAwayName() != null ? event.getAwayName().trim().toUpperCase(Locale.ROOT) : null;

        BetScope scope = resolveScope(marketName);

        // 1. Double Chance
        if (mUpper.contains("DOUBLE CHANCE") || "OT_ONE_DRAW".equals(typeUpper) || "OT_DRAW_TWO".equals(typeUpper) || "OT_ONE_TWO".equals(typeUpper)) {
            if ("OT_ONE_DRAW".equals(typeUpper) || labelUpper.contains("1X") || labelUpper.contains("1 OR X")) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, null);
            } else if ("OT_ONE_TWO".equals(typeUpper) || labelUpper.contains("12") || labelUpper.contains("1 OR 2")) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, null);
            } else if ("OT_DRAW_TWO".equals(typeUpper) || labelUpper.contains("X2") || labelUpper.contains("X OR 2") || labelUpper.contains("2X")) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, null);
            }
        }

        // 2. Draw No Bet
        if (mUpper.contains("DRAW NO BET") || mUpper.contains("DNB")) {
            if ("OT_ONE".equals(typeUpper) || "HOME".equals(participant) || labelUpper.startsWith("1") || (home != null && labelUpper.contains(home))) {
                return new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, null);
            } else if ("OT_TWO".equals(typeUpper) || "AWAY".equals(participant) || labelUpper.startsWith("2") || (away != null && labelUpper.contains(away))) {
                return new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, null);
            }
        }

        // 3. Both Teams to Score (BTTS)
        if (mUpper.contains("BOTH TEAMS TO SCORE") || mUpper.contains("BTTS") || (mUpper.contains("BOTH TEAMS") && mUpper.contains("SCORE"))) {
            if ("OT_YES".equals(typeUpper) || labelUpper.startsWith("YES") || labelUpper.equals("1")) {
                return new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if ("OT_NO".equals(typeUpper) || labelUpper.startsWith("NO") || labelUpper.equals("2")) {
                return new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }
        }

        // 4. Spreads / Handicaps (MUST be resolved before Moneyline/1X2 to prevent "Match Handicap" from being treated as 1X2!)
        if (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("ASIAN")) {
            if ("OT_ONE".equals(typeUpper) || "HOME".equals(participant) || labelUpper.startsWith("1") || (home != null && labelUpper.contains(home))) {
                return new HandicapBet(scope, HandicapBet.Outcome.TEAM1, line, false, null);
            } else if ("OT_TWO".equals(typeUpper) || "AWAY".equals(participant) || labelUpper.startsWith("2") || (away != null && labelUpper.contains(away))) {
                return new HandicapBet(scope, HandicapBet.Outcome.TEAM2, line, false, null);
            }
        }

        // 5. Totals Markets (MUST be resolved before Moneyline/1X2 to prevent "Match Total" from false matching!)
        if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("OVER / UNDER")
                || "OT_OVER".equals(typeUpper) || "OT_UNDER".equals(typeUpper)) {
            BetSubject subject = determineTotalSubject(marketName, home, away);
            if ("OT_OVER".equals(typeUpper) || labelUpper.startsWith("OVER") || labelUpper.startsWith(">")) {
                return new TotalBet(scope, subject, TotalBet.Direction.OVER, line, false, null);
            } else if ("OT_UNDER".equals(typeUpper) || labelUpper.startsWith("UNDER") || labelUpper.startsWith("<")) {
                return new TotalBet(scope, subject, TotalBet.Direction.UNDER, line, false, null);
            }
        }

        // 6. Result Markets (Moneyline, 1X2) - Strictly exclude other markets
        boolean isExcludedFromMoneyline = mUpper.contains("CORNER") || mUpper.contains("CARD") || mUpper.contains("BOOKING")
                || mUpper.contains("DOUBLE CHANCE") || mUpper.contains("DRAW NO BET") || mUpper.contains("DNB")
                || mUpper.contains("BOTH TEAMS") || mUpper.contains("BTTS")
                || mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("OVER / UNDER")
                || mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("ASIAN")
                || mUpper.contains("HALF TIME/FULL TIME") || mUpper.contains("HT/FT")
                || mUpper.contains("ROUND") || mUpper.contains("MAP");

        if (!isExcludedFromMoneyline && (mUpper.contains("MATCH") || mUpper.contains("RESULT") || mUpper.contains("MONEYLINE")
                || mUpper.contains("1X2") || mUpper.contains("WINNER") || mUpper.contains("WHO WILL WIN") || mUpper.endsWith("FULL TIME"))) {
            boolean hasDraw = betOffer != null && betOffer.getOutcomes() != null && betOffer.getOutcomes().stream()
                    .anyMatch(o -> "OT_DRAW".equalsIgnoreCase(o.getType())
                            || "OT_CROSS".equalsIgnoreCase(o.getType())
                            || (o.getLabel() != null && (o.getLabel().equalsIgnoreCase("Draw") || o.getLabel().equalsIgnoreCase("X"))));

            if ("OT_ONE".equals(typeUpper) || "1".equals(labelUpper) || (home != null && labelUpper.equalsIgnoreCase(home))) {
                return new MatchResultBet(scope, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("OT_DRAW".equals(typeUpper) || "OT_CROSS".equals(typeUpper) || "DRAW".equals(labelUpper) || "X".equals(labelUpper)) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, null);
            } else if ("OT_TWO".equals(typeUpper) || "2".equals(labelUpper) || (away != null && labelUpper.equalsIgnoreCase(away))) {
                return new MatchResultBet(scope, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            }
        }

        // Fallback to strict DB matching
        return betTypeResolver.resolve("unibet", sportType, mUpper, runnerName.toUpperCase(Locale.ROOT), line);
    }

    public static Double normalizeLine(Double rawLine) {
        if (rawLine == null) {
            return 0.0;
        }
        if (Math.abs(rawLine) >= 50.0) {
            return rawLine / 1000.0;
        }
        return rawLine;
    }

    public static BetScope resolveScope(String marketName) {
        if (marketName == null || marketName.isBlank()) {
            return BetScope.FULL_MATCH;
        }
        String m = marketName.toLowerCase(Locale.ROOT);

        // Esports maps
        if (m.contains("map 1") || m.contains("1st map")) return BetScope.MAP_1;
        if (m.contains("map 2") || m.contains("2nd map")) return BetScope.MAP_2;
        if (m.contains("map 3") || m.contains("3rd map")) return BetScope.MAP_3;
        if (m.contains("map 4") || m.contains("4th map")) return BetScope.MAP_4;
        if (m.contains("map 5") || m.contains("5th map")) return BetScope.MAP_5;

        // Halves
        if (m.contains("1st half") || m.contains("first half") || m.contains("half 1") || m.contains("ht1") || m.contains("1h")) return BetScope.HALF_1;
        if (m.contains("2nd half") || m.contains("second half") || m.contains("half 2") || m.contains("ht2") || m.contains("2h")) return BetScope.HALF_2;

        // Quarters
        if (m.contains("1st quarter") || m.contains("quarter 1") || m.contains("1q")) return BetScope.QUARTER_1;
        if (m.contains("2nd quarter") || m.contains("quarter 2") || m.contains("2q")) return BetScope.QUARTER_2;
        if (m.contains("3rd quarter") || m.contains("quarter 3") || m.contains("3q")) return BetScope.QUARTER_3;
        if (m.contains("4th quarter") || m.contains("quarter 4") || m.contains("4q")) return BetScope.QUARTER_4;

        // Periods (Hockey)
        if (m.contains("1st period") || m.contains("period 1") || m.contains("1p")) return BetScope.PERIOD_1;
        if (m.contains("2nd period") || m.contains("period 2") || m.contains("2p")) return BetScope.PERIOD_2;
        if (m.contains("3rd period") || m.contains("period 3") || m.contains("3p")) return BetScope.PERIOD_3;

        // Sets (Tennis / Volleyball)
        if (m.contains("1st set") || m.contains("set 1")) return BetScope.SET_1;
        if (m.contains("2nd set") || m.contains("set 2")) return BetScope.SET_2;
        if (m.contains("3rd set") || m.contains("set 3")) return BetScope.SET_3;
        if (m.contains("4th set") || m.contains("set 4")) return BetScope.SET_4;
        if (m.contains("5th set") || m.contains("set 5")) return BetScope.SET_5;

        return BetScope.FULL_MATCH;
    }

    public static BetSubject determineTotalSubject(String marketName, String team1, String team2) {
        if (marketName == null || marketName.isBlank()) {
            return BetSubject.MATCH;
        }
        String lower = marketName.toLowerCase(Locale.ROOT);
        boolean matchesTeam1 = team1 != null && !team1.isBlank() && lower.contains(team1.toLowerCase(Locale.ROOT));
        boolean matchesTeam2 = team2 != null && !team2.isBlank() && lower.contains(team2.toLowerCase(Locale.ROOT));

        if (matchesTeam1 && !matchesTeam2) return BetSubject.TEAM1;
        if (matchesTeam2 && !matchesTeam1) return BetSubject.TEAM2;

        if (lower.contains("home") || lower.contains("team 1") || lower.contains("team1")) {
            return BetSubject.TEAM1;
        }
        if (lower.contains("away") || lower.contains("team 2") || lower.contains("team2")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }

    private void logUnmapped(KambiEvent event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED UNIBET MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event.getId(), sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("unibet", sportName, runnerName, marketName, String.valueOf(event.getId()));
    }
}

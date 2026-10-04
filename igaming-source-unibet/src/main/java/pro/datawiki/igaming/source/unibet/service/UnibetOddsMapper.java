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

        StatType statType = resolveStatType(marketName);
        BetScope scope = resolveScope(marketName);

        // 1. Double Chance
        boolean isDoubleChance = mUpper.contains("DOUBLE CHANCE") || mUpper.contains("DUBBELCHANS") || mUpper.contains("ДВОЙНОЙ ШАНС")
                || "OT_ONE_DRAW".equals(typeUpper) || "OT_ONE_CROSS".equals(typeUpper)
                || "OT_DRAW_TWO".equals(typeUpper) || "OT_CROSS_TWO".equals(typeUpper)
                || "OT_ONE_TWO".equals(typeUpper);

        if (isDoubleChance) {
            if ("OT_ONE_DRAW".equals(typeUpper) || "OT_ONE_CROSS".equals(typeUpper)
                    || labelUpper.startsWith("1X") || labelUpper.equals("1X") || labelUpper.contains("1 OR X") || labelUpper.contains("1 ELLER X") || labelUpper.contains("1 ИЛИ Х")
                    || (home != null && labelUpper.contains(home) && (labelUpper.contains("DRAW") || labelUpper.contains("OAVGJORT") || labelUpper.contains("X") || labelUpper.contains("LIKA") || labelUpper.contains("НИЧЬЯ")))) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, statType);
            } else if ("OT_DRAW_TWO".equals(typeUpper) || "OT_CROSS_TWO".equals(typeUpper)
                    || labelUpper.startsWith("X2") || labelUpper.equals("X2") || labelUpper.startsWith("2X") || labelUpper.equals("2X")
                    || labelUpper.contains("X OR 2") || labelUpper.contains("2 OR X") || labelUpper.contains("X ELLER 2") || labelUpper.contains("2 ELLER X") || labelUpper.contains("Х ИЛИ 2")
                    || (away != null && labelUpper.contains(away) && (labelUpper.contains("DRAW") || labelUpper.contains("OAVGJORT") || labelUpper.contains("X") || labelUpper.contains("LIKA") || labelUpper.contains("НИЧЬЯ")))) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, statType);
            } else if ("OT_ONE_TWO".equals(typeUpper) || labelUpper.equals("12") || labelUpper.contains("1 OR 2") || labelUpper.contains("1 ELLER 2") || labelUpper.contains("1 ИЛИ 2")
                    || (home != null && away != null && labelUpper.contains(home) && labelUpper.contains(away))) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, statType);
            } else if (betOffer != null && betOffer.getOutcomes() != null && betOffer.getOutcomes().size() == 3) {
                int idx = betOffer.getOutcomes().indexOf(outcome);
                if (idx == 0) return new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, statType);
                if (idx == 1) return new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, statType);
                if (idx == 2) return new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, statType);
            }
        }

        // 2. Draw No Bet
        if (mUpper.contains("DRAW NO BET") || mUpper.contains("DNB") || mUpper.contains("OAVGJORT INGET SPEL")
                || mUpper.contains("INSATSEN TILLBAKA VID OAVGJORT") || mUpper.contains("TIE NO BET") || mUpper.contains("НИЧЬЯ ИСКЛЮЧЕНА")) {
            if ("OT_ONE".equals(typeUpper) || "HOME".equals(participant) || labelUpper.startsWith("1") || (home != null && labelUpper.contains(home))) {
                return new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, statType);
            } else if ("OT_TWO".equals(typeUpper) || "AWAY".equals(participant) || labelUpper.startsWith("2") || (away != null && labelUpper.contains(away))) {
                return new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, statType);
            }
        }

        // 3. Both Teams to Score (BTTS)
        if (mUpper.contains("BOTH TEAMS TO SCORE") || mUpper.contains("BTTS") || (mUpper.contains("BOTH TEAMS") && mUpper.contains("SCORE"))
                || mUpper.contains("BÅDA LAGEN GÖR MÅL") || mUpper.contains("ОБЕ ЗАБЬЮТ")) {
            if ("OT_YES".equals(typeUpper) || labelUpper.startsWith("YES") || labelUpper.startsWith("JA") || labelUpper.equals("1")) {
                return new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, statType);
            } else if ("OT_NO".equals(typeUpper) || labelUpper.startsWith("NO") || labelUpper.startsWith("NEJ") || labelUpper.equals("2")) {
                return new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, statType);
            }
        }

        // 4. Spreads / Handicaps (MUST be resolved before Moneyline/1X2 to prevent "Match Handicap" from being treated as 1X2!)
        if (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("ASIAN") || mUpper.contains("HANDIKAPP") || mUpper.contains("ФОРА")) {
            boolean is3WayHandicap = betOffer != null && betOffer.getOutcomes() != null && (
                    betOffer.getOutcomes().size() == 3
                    || betOffer.getOutcomes().stream().anyMatch(UnibetOddsMapper::isDraw)
            );
            if (isDraw(outcome)) {
                return new HandicapBet(scope, HandicapBet.Outcome.DRAW, line, !is3WayHandicap, statType);
            } else if ("OT_ONE".equals(typeUpper) || "HOME".equals(participant) || labelUpper.startsWith("1") || (home != null && labelUpper.contains(home))) {
                return new HandicapBet(scope, HandicapBet.Outcome.TEAM1, line, !is3WayHandicap, statType);
            } else if ("OT_TWO".equals(typeUpper) || "AWAY".equals(participant) || labelUpper.startsWith("2") || (away != null && labelUpper.contains(away))) {
                return new HandicapBet(scope, HandicapBet.Outcome.TEAM2, line, !is3WayHandicap, statType);
            }
        }

        // 5. Totals Markets (MUST be resolved before Moneyline/1X2 to prevent "Match Total" from false matching!)
        if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("OVER / UNDER")
                || mUpper.contains("ÖVER/UNDER") || mUpper.contains("ANTAL") || mUpper.contains("ТОТАЛ")
                || "OT_OVER".equals(typeUpper) || "OT_UNDER".equals(typeUpper)) {
            BetSubject subject = determineTotalSubject(marketName, home, away);
            if ("OT_OVER".equals(typeUpper) || labelUpper.startsWith("OVER") || labelUpper.startsWith("ÖVER") || labelUpper.startsWith(">")) {
                return new TotalBet(scope, subject, TotalBet.Direction.OVER, line, false, statType);
            } else if ("OT_UNDER".equals(typeUpper) || labelUpper.startsWith("UNDER") || labelUpper.startsWith("UNDER") || labelUpper.startsWith("<")) {
                return new TotalBet(scope, subject, TotalBet.Direction.UNDER, line, false, statType);
            }
        }

        // 6. Result Markets (Moneyline, 1X2) - Strictly exclude other markets
        boolean isExcludedFromMoneyline = mUpper.contains("CORNER") || mUpper.contains("HÖRN")
                || mUpper.contains("CARD") || mUpper.contains("BOOKING") || mUpper.contains("KORT") || mUpper.contains("VARNING")
                || mUpper.contains("DOUBLE CHANCE") || mUpper.contains("DUBBELCHANS") || mUpper.contains("ДВОЙНОЙ ШАНС")
                || mUpper.contains("DRAW NO BET") || mUpper.contains("DNB") || mUpper.contains("OAVGJORT INGET SPEL")
                || mUpper.contains("BOTH TEAMS") || mUpper.contains("BTTS") || mUpper.contains("BÅDA LAGEN")
                || mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("OVER / UNDER") || mUpper.contains("ÖVER/UNDER") || mUpper.contains("ANTAL")
                || mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("ASIAN") || mUpper.contains("HANDIKAPP")
                || mUpper.contains("HALF TIME/FULL TIME") || mUpper.contains("HT/FT") || mUpper.contains("HALVTID/FULLTID")
                || mUpper.contains("ROUND") || mUpper.contains("MAP");

        if (!isExcludedFromMoneyline && (mUpper.contains("MATCH") || mUpper.contains("RESULT") || mUpper.contains("MONEYLINE")
                || mUpper.contains("1X2") || mUpper.contains("WINNER") || mUpper.contains("WHO WILL WIN")
                || mUpper.contains("REGULAR TIME") || mUpper.contains("ORDINARIE TID") || mUpper.contains("FULL TIME") || mUpper.contains("FULLTIME")
                || mUpper.contains("ОСНОВНОЕ ВРЕМЯ") || mUpper.endsWith("FULL TIME"))) {
            boolean hasDraw = betOffer != null && betOffer.getOutcomes() != null && (
                    betOffer.getOutcomes().size() == 3
                    || betOffer.getOutcomes().stream().anyMatch(UnibetOddsMapper::isDraw)
            );

            if (isDraw(outcome)) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType);
            } else if ("OT_ONE".equals(typeUpper) || "1".equals(labelUpper) || (home != null && labelUpper.equalsIgnoreCase(home))) {
                return new MatchResultBet(scope, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, statType);
            } else if ("OT_TWO".equals(typeUpper) || "2".equals(labelUpper) || (away != null && labelUpper.equalsIgnoreCase(away))) {
                return new MatchResultBet(scope, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, statType);
            } else if (hasDraw && betOffer != null && betOffer.getOutcomes() != null && betOffer.getOutcomes().size() == 3) {
                int idx = betOffer.getOutcomes().indexOf(outcome);
                if (idx == 1) {
                    return new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType);
                } else if (idx == 0) {
                    return new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, statType);
                } else if (idx == 2) {
                    return new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, statType);
                }
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

    public static boolean isDraw(KambiOutcome outcome) {
        if (outcome == null) return false;
        String t = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String l = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";
        return "OT_DRAW".equals(t) || "OT_CROSS".equals(t) || "OT_TIE".equals(t) || "OT_X".equals(t) || "OT_EQUAL".equals(t)
                || "DRAW".equals(l) || "TIE".equals(l) || "X".equals(l)
                || "OAVGJORT".equals(l) || "LIKA".equals(l) || "UNENTSCHIEDEN".equals(l)
                || "NUL".equals(l) || "EMPATE".equals(l) || "НИЧЬЯ".equals(l)
                || l.contains("HANDICAP TIE") || l.contains("HANDICAP DRAW") || l.contains("HANDIKAPP OAVGJORT")
                || l.startsWith("TIE ") || l.startsWith("DRAW ") || l.startsWith("X ");
    }

    public static StatType resolveStatType(String marketName) {
        if (marketName == null || marketName.isBlank()) return StatType.MATCH;
        String m = marketName.toLowerCase(Locale.ROOT);
        if (m.contains("yellow") || m.contains("жк") || m.contains("желт")) {
            return StatType.YELLOW_CARDS;
        }
        if (m.contains("card") || m.contains("booking") || m.contains("kort") || m.contains("varning") || m.contains("карточ")) {
            return StatType.CARDS;
        }
        if (m.contains("corner") || m.contains("hörn") || m.contains("углов")) {
            return StatType.CORNERS;
        }
        if (m.contains("foul") || m.contains("фол")) {
            return StatType.FOULS;
        }
        if (m.contains("offside") || m.contains("офсайд")) {
            return StatType.OFFSIDES;
        }
        if (m.contains("shot on goal") || m.contains("shots on goal")) {
            return StatType.SHOTS_ON_GOAL;
        }
        if (m.contains("shot on target") || m.contains("shots on target") || m.contains("в створ")) {
            return StatType.SHOTS_ON_TARGET;
        }
        if (m.contains("ace") || m.contains("эйс")) {
            return StatType.ACES;
        }
        if (m.contains("double fault") || m.contains("двойн")) {
            return StatType.DOUBLE_FAULTS;
        }
        return StatType.MATCH;
    }

    private void logUnmapped(KambiEvent event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED UNIBET MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event.getId(), sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("unibet", sportName, runnerName, marketName, String.valueOf(event.getId()));
    }
}

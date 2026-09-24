package pro.datawiki.igaming.source.betfair.service;

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
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.betfair.service.BetfairApiClient.BetfairOddsResponse;
import pro.datawiki.igaming.source.betfair.service.BetfairApiClient.BetfairOdd;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class BetfairOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;
    private final BetfairConfig betfairConfig;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    public OddsUpdateRequest mapToOddsUpdateRequest(BetfairOddsResponse response, MatchCache cached, String fallbackSport, String fallbackLeague) {
        if (response == null || response.getOdds() == null || response.getOdds().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("betfair");
        request.setExternalEventId(response.getEventId());
        request.setStartTime(cached != null ? cached.getStartTime() : System.currentTimeMillis());

        String sportName = cached != null && cached.getSportName() != null ? cached.getSportName() : fallbackSport;
        request.setSportName(sportName != null ? sportName : "Soccer");

        SportType sportType = sportNormalizationService.normalize(request.getSportName());
        request.setSportType(sportType);

        String leagueName = cached != null && cached.getLeagueName() != null ? cached.getLeagueName() : fallbackLeague;
        request.setLeagueName(leagueName != null ? leagueName : "General");

        request.setTeam1(cached != null ? cached.getTeam1() : "Home");
        request.setTeam2(cached != null ? cached.getTeam2() : "Away");
        request.setIsLive(cached != null && Boolean.TRUE.equals(cached.getIsLive()));
        request.setEventUrl("https://www.betfair.com/sport/event?id=" + response.getEventId());

        double commission = (betfairConfig != null && betfairConfig.getExchange() != null && betfairConfig.getExchange().isDeductCommission())
                ? betfairConfig.getExchange().getCommissionRate() : 0.0;

        List<OddItem> oddsList = new ArrayList<>();
        for (BetfairOdd odd : response.getOdds()) {
            processOdd(response.getEventId(), odd, sportType, request.getSportName(), request.getTeam1(), request.getTeam2(), commission, oddsList);
        }
        request.setOdds(oddsList);

        return request;
    }

    private void processOdd(String eventId, BetfairOdd odd, SportType sportType, String sportName,
                            String team1, String team2, double commission, List<OddItem> oddsList) {
        String marketName = odd.getMarketName() != null ? odd.getMarketName() : "Unknown Market";
        String runnerName = odd.getOutcomeName() != null ? odd.getOutcomeName() : "Outcome " + odd.getCode();

        BetType betType = resolveBetType(odd, sportType, marketName, runnerName, team1, team2);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(eventId, sportName, marketName, runnerName);
            return;
        }

        double netValue = calculateNetBackOdd(odd.getValue(), commission);

        OddItem item = new OddItem();
        item.setFactorId(odd.getCode());
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(netValue);
        item.setBetType(betType);

        oddsList.add(item);
    }

    public double calculateNetBackOdd(double grossOdd, double commissionRate) {
        if (grossOdd <= 1.0 || commissionRate <= 0.0) return grossOdd;
        double net = 1.0 + (grossOdd - 1.0) * (1.0 - commissionRate);
        return BigDecimal.valueOf(net).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private BetType resolveBetType(BetfairOdd odd, SportType sportType, String marketName, String runnerName, String team1, String team2) {
        String mUpper = marketName.toUpperCase();
        String rUpper = runnerName.toUpperCase();
        String codeUpper = odd.getCode().toUpperCase();

        // 1. Result Markets
        if (mUpper.contains("MATCH ODDS") || mUpper.contains("MATCH WINNER") || mUpper.contains("MATCH BETTING") || mUpper.contains("1X2")) {
            if ("1".equals(codeUpper) || rUpper.equalsIgnoreCase(team1) || rUpper.contains("HOME")) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null);
            } else if ("X".equals(codeUpper) || "DRAW".equals(codeUpper) || "THE DRAW".equals(rUpper)) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("2".equals(codeUpper) || rUpper.equalsIgnoreCase(team2) || rUpper.contains("AWAY")) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null);
            }
        }

        // 2. Over / Under Goals
        if (mUpper.contains("OVER/UNDER") || mUpper.contains("TOTAL GOALS")) {
            Double param = extractParam(mUpper);
            if (param == null) param = 2.5;

            if (rUpper.contains("OVER")) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, null);
            } else if (rUpper.contains("UNDER")) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, null);
            }
        }

        // 3. Both Teams to Score
        if (mUpper.contains("BOTH TEAMS TO SCORE") || mUpper.contains("BTTS")) {
            if (rUpper.contains("YES")) {
                return new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if (rUpper.contains("NO")) {
                return new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }
        }

        // Fallback to resolver
        return betTypeResolver.resolve("betfair", sportType, mUpper, runnerName.toUpperCase(), 0.0);
    }

    private Double extractParam(String str) {
        try {
            Matcher m = NUMBER_PATTERN.matcher(str);
            if (m.find()) {
                return Double.parseDouble(m.group());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void logUnmapped(String eventId, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED BETFAIR MARKET: Event={}, Sport={}, Market={}, Runner={}",
                eventId, sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("betfair", sportName, runnerName, marketName, eventId);
    }
}

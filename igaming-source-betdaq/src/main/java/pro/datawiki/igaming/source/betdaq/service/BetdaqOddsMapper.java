package pro.datawiki.igaming.source.betdaq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.betdaq.config.BetdaqConfig;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqEventDto;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqMarketDto;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqSelectionDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class BetdaqOddsMapper extends AbstractBetTypeMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetdaqConfig betdaqConfig;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "betdaq".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cache, BetdaqEventDto event) {
        if (cache == null || event == null || event.getMarkets() == null || event.getMarkets().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("betdaq");
        request.setRegions(List.of(BookmakerRegion.GB, BookmakerRegion.EU));
        request.setExternalEventId(event.getId() != null ? event.getId() : cache.getExternalId());

        String sportName = cache.getSportName() != null ? cache.getSportName() : (event.getSportName() != null ? event.getSportName() : "Football");
        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);
        request.setSportName(sportName);

        request.setLeagueName(cache.getLeagueName() != null ? cache.getLeagueName() : (event.getCompetitionName() != null ? event.getCompetitionName() : "General"));
        request.setTeam1(cache.getTeam1());
        request.setTeam2(cache.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cache.getIsLive()) || Boolean.TRUE.equals(event.getIsLive()));
        request.setStartTime(cache.getStartTime());

        double commission = (betdaqConfig != null && betdaqConfig.isDeductCommission())
                ? betdaqConfig.getCommissionRate() : 0.0;

        List<OddItem> items = new ArrayList<>();

        for (BetdaqMarketDto market : event.getMarkets()) {
            if (market == null || market.getSelections() == null || market.getSelections().isEmpty()) {
                continue;
            }
            mapMarket(items, market, cache.getTeam1(), cache.getTeam2(), commission);
        }

        request.setOdds(items);
        return request;
    }

    private void mapMarket(List<OddItem> items, BetdaqMarketDto market, String team1, String team2, double commission) {
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";

        if (mName.contains("match odds") || mName.contains("1x2") || mName.contains("winner") || mName.contains("match betting")) {
            map1X2(items, market, team1, team2, commission);
        } else if (mName.contains("over/under") || mName.contains("total goals")) {
            mapTotal(items, market, commission);
        } else if (mName.contains("both teams to score") || mName.contains("btts")) {
            mapBtts(items, market, commission);
        } else if (mName.contains("double chance")) {
            mapDoubleChance(items, market, commission);
        }
    }

    private void map1X2(List<OddItem> items, BetdaqMarketDto market, String team1, String team2, double commission) {
        boolean hasDraw = market.getSelections().stream().anyMatch(s -> "draw".equalsIgnoreCase(s.getName()) || "the draw".equalsIgnoreCase(s.getName()));

        for (BetdaqSelectionDto sel : market.getSelections()) {
            Double gross = sel.getBestBackPrice();
            if (gross == null || gross <= 1.0) continue;
            double net = calculateNetBackOdd(gross, commission);

            String name = sel.getName() != null ? sel.getName().trim() : "";
            BetType betType = null;

            if ("1".equalsIgnoreCase(name) || (team1 != null && name.equalsIgnoreCase(team1)) || name.toLowerCase().contains("home")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("draw".equalsIgnoreCase(name) || "the draw".equalsIgnoreCase(name) || "x".equalsIgnoreCase(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("2".equalsIgnoreCase(name) || (team2 != null && name.equalsIgnoreCase(team2)) || name.toLowerCase().contains("away")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel.getId(), market.getName(), name, net, betType));
            }
        }
    }

    private void mapTotal(List<OddItem> items, BetdaqMarketDto market, double commission) {
        Double param = extractParam(market.getName());
        if (param == null) param = 2.5;

        for (BetdaqSelectionDto sel : market.getSelections()) {
            Double gross = sel.getBestBackPrice();
            if (gross == null || gross <= 1.0) continue;
            double net = calculateNetBackOdd(gross, commission);

            String name = sel.getName() != null ? sel.getName().trim().toLowerCase() : "";
            BetType betType = null;

            if (name.contains("over")) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, null);
            } else if (name.contains("under")) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel.getId(), market.getName(), sel.getName(), net, betType));
            }
        }
    }

    private void mapBtts(List<OddItem> items, BetdaqMarketDto market, double commission) {
        for (BetdaqSelectionDto sel : market.getSelections()) {
            Double gross = sel.getBestBackPrice();
            if (gross == null || gross <= 1.0) continue;
            double net = calculateNetBackOdd(gross, commission);

            String name = sel.getName() != null ? sel.getName().trim().toLowerCase() : "";
            BetType betType = null;

            if ("yes".equals(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if ("no".equals(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel.getId(), market.getName(), sel.getName(), net, betType));
            }
        }
    }

    private void mapDoubleChance(List<OddItem> items, BetdaqMarketDto market, double commission) {
        for (BetdaqSelectionDto sel : market.getSelections()) {
            Double gross = sel.getBestBackPrice();
            if (gross == null || gross <= 1.0) continue;
            double net = calculateNetBackOdd(gross, commission);

            String name = sel.getName() != null ? sel.getName().trim().toUpperCase() : "";
            BetType betType = null;

            if ("1X".equals(name) || "HOME OR DRAW".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null);
            } else if ("12".equals(name) || "HOME OR AWAY".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null);
            } else if ("X2".equals(name) || "DRAW OR AWAY".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel.getId(), market.getName(), sel.getName(), net, betType));
            }
        }
    }

    public double calculateNetBackOdd(double grossOdd, double commissionRate) {
        if (grossOdd <= 1.0 || commissionRate <= 0.0) return grossOdd;
        double net = 1.0 + (grossOdd - 1.0) * (1.0 - commissionRate);
        return BigDecimal.valueOf(net).setScale(3, RoundingMode.HALF_UP).doubleValue();
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

    private OddItem createOddItem(String id, String groupName, String name, Double price, BetType betType) {
        OddItem item = new OddItem();
        item.setFactorId(id);
        item.setGroupName(groupName);
        item.setName(name);
        item.setValue(price);
        item.setBetType(betType);
        return item;
    }
}

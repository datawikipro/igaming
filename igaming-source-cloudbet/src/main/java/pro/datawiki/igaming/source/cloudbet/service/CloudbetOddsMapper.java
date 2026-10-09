package pro.datawiki.igaming.source.cloudbet.service;

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
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetFixtureDto;
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetMarketDto;
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetSelectionDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CloudbetOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "cloudbet".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cache, CloudbetFixtureDto fixture) {
        if (cache == null || fixture == null || fixture.getMarkets() == null || fixture.getMarkets().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("cloudbet");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(fixture.getId() != null ? fixture.getId() : cache.getExternalId());

        String sportName = cache.getSportName() != null ? cache.getSportName() : (fixture.getSport() != null ? fixture.getSport().getName() : "General");
        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);
        request.setSportName(sportName);

        request.setLeagueName(cache.getLeagueName() != null ? cache.getLeagueName() : (fixture.getCompetition() != null ? fixture.getCompetition().getName() : "General"));
        request.setTeam1(cache.getTeam1());
        request.setTeam2(cache.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cache.getIsLive()) || "live".equalsIgnoreCase(fixture.getStatus()));
        request.setStartTime(cache.getStartTime());

        List<OddItem> items = new ArrayList<>();

        for (Map.Entry<String, CloudbetMarketDto> entry : fixture.getMarkets().entrySet()) {
            String marketKey = entry.getKey();
            CloudbetMarketDto market = entry.getValue();
            if (market == null || market.getSelections() == null || market.getSelections().isEmpty()) {
                continue;
            }
            mapMarket(items, marketKey, market, cache.getTeam1(), cache.getTeam2());
        }

        request.setOdds(items);
        return request;
    }

    private void mapMarket(List<OddItem> items, String marketKey, CloudbetMarketDto market, String team1, String team2) {
        String key = (marketKey != null ? marketKey : "").toLowerCase();

        if (key.contains("match_odds") || key.contains("winner") || key.contains("moneyline") || key.contains("1x2")) {
            map1X2(items, marketKey, market);
        } else if (key.contains("total") || key.contains("over_under")) {
            mapTotal(items, marketKey, market);
        } else if (key.contains("handicap") || key.contains("spread")) {
            mapHandicap(items, marketKey, market);
        } else if (key.contains("both_teams_to_score") || key.contains("btts")) {
            mapBtts(items, marketKey, market);
        } else if (key.contains("double_chance")) {
            mapDoubleChance(items, marketKey, market);
        }
    }

    private void map1X2(List<OddItem> items, String marketKey, CloudbetMarketDto market) {
        boolean hasDraw = market.getSelections().stream().anyMatch(s -> "draw".equalsIgnoreCase(s.getOutcome()) || "x".equalsIgnoreCase(s.getOutcome()));

        for (CloudbetSelectionDto sel : market.getSelections()) {
            Double price = sel.getEffectivePrice();
            if (price == null || price <= 1.0) continue;
            String outcome = sel.getOutcome() != null ? sel.getOutcome().trim().toLowerCase() : "";
            BetType betType = null;

            if ("home".equals(outcome) || "1".equals(outcome)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("draw".equals(outcome) || "x".equals(outcome)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("away".equals(outcome) || "2".equals(outcome)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel, marketKey, betType, price));
            }
        }
    }

    private void mapTotal(List<OddItem> items, String marketKey, CloudbetMarketDto market) {
        for (CloudbetSelectionDto sel : market.getSelections()) {
            Double price = sel.getEffectivePrice();
            if (price == null || price <= 1.0) continue;
            String outcome = sel.getOutcome() != null ? sel.getOutcome().trim().toLowerCase() : "";
            Double param = sel.getEffectiveParam();
            if (param == null) param = 2.5;

            BetType betType = null;
            if (outcome.startsWith("over") || "o".equals(outcome)) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, null);
            } else if (outcome.startsWith("under") || "u".equals(outcome)) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel, marketKey, betType, price));
            }
        }
    }

    private void mapHandicap(List<OddItem> items, String marketKey, CloudbetMarketDto market) {
        for (CloudbetSelectionDto sel : market.getSelections()) {
            Double price = sel.getEffectivePrice();
            if (price == null || price <= 1.0) continue;
            String outcome = sel.getOutcome() != null ? sel.getOutcome().trim().toLowerCase() : "";
            Double param = sel.getEffectiveParam();
            if (param == null) param = 0.0;

            BetType betType = null;
            if ("home".equals(outcome) || "1".equals(outcome)) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, param, false, null);
            } else if ("away".equals(outcome) || "2".equals(outcome)) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, param, false, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel, marketKey, betType, price));
            }
        }
    }

    private void mapBtts(List<OddItem> items, String marketKey, CloudbetMarketDto market) {
        for (CloudbetSelectionDto sel : market.getSelections()) {
            Double price = sel.getEffectivePrice();
            if (price == null || price <= 1.0) continue;
            String outcome = sel.getOutcome() != null ? sel.getOutcome().trim().toLowerCase() : "";
            BetType betType = null;

            if ("yes".equals(outcome)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if ("no".equals(outcome)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel, marketKey, betType, price));
            }
        }
    }

    private void mapDoubleChance(List<OddItem> items, String marketKey, CloudbetMarketDto market) {
        for (CloudbetSelectionDto sel : market.getSelections()) {
            Double price = sel.getEffectivePrice();
            if (price == null || price <= 1.0) continue;
            String outcome = sel.getOutcome() != null ? sel.getOutcome().trim().toLowerCase() : "";
            BetType betType = null;

            if ("home_draw".equals(outcome) || "1x".equals(outcome)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null);
            } else if ("home_away".equals(outcome) || "12".equals(outcome)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null);
            } else if ("draw_away".equals(outcome) || "x2".equals(outcome)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null);
            }

            if (betType != null) {
                items.add(createOddItem(sel, marketKey, betType, price));
            }
        }
    }

    private OddItem createOddItem(CloudbetSelectionDto sel, String groupName, BetType betType, Double price) {
        OddItem item = new OddItem();
        item.setFactorId(sel.getOutcome());
        item.setGroupName(groupName);
        item.setName(sel.getOutcome());
        item.setValue(price);
        item.setBetType(betType);
        return item;
    }
}

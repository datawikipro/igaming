package pro.datawiki.igaming.source.pinnacle.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.sport.SportRegistry;
import pro.datawiki.igaming.source.pinnacle.service.handler.AbstractPinnacleMarketHandler;
import pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleMarketHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PinnacleOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;
    private final SportRegistry sportRegistry;
    private final List<PinnacleMarketHandler> marketHandlers;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "pinnacle".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public static double americanToDecimal(double american) {
        return AbstractPinnacleMarketHandler.americanToDecimal(american);
    }

    public OddsUpdateRequest mapArcadiaToOddsUpdateRequest(JsonNode matchup, List<JsonNode> markets, String sportName, SportType sportType) {
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("pinnacle");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.EU));

        String externalEventId = matchup.path("id").asText();
        request.setExternalEventId(externalEventId);
        request.setSportName(sportName);
        request.setSportType(sportType);

        String leagueName = matchup.path("league").path("name").asText("Unknown League");
        request.setLeagueName(leagueName);

        String homeTeam = null;
        String awayTeam = null;
        JsonNode participants = matchup.path("participants");
        if (participants.isArray()) {
            for (JsonNode p : participants) {
                String alignment = p.path("alignment").asText();
                String name = p.path("name").asText();
                if ("home".equalsIgnoreCase(alignment)) {
                    homeTeam = name;
                } else if ("away".equalsIgnoreCase(alignment)) {
                    awayTeam = name;
                }
            }
            if (homeTeam == null && participants.size() >= 2) {
                homeTeam = participants.get(0).path("name").asText();
                awayTeam = participants.get(1).path("name").asText();
            }
        }

        if (homeTeam == null || awayTeam == null) {
            return null;
        }

        request.setTeam1(homeTeam);
        request.setTeam2(awayTeam);
        request.setIsLive(matchup.path("isLive").asBoolean(false));
        request.setEventUrl("https://www.pinnacle.com/en/" + sportName.toLowerCase().replace(" ", "-") + "/" +
                leagueName.toLowerCase().replace(" ", "-") + "/match/" + externalEventId);

        String startTimeStr = matchup.path("startTime").asText();
        if (!startTimeStr.isEmpty()) {
            try {
                request.setStartTime(Instant.parse(startTimeStr).toEpochMilli());
            } catch (Exception e) {
                log.debug("Failed to parse start time '{}' for Pinnacle event {}: {}", startTimeStr, externalEventId, e.getMessage());
            }
        }

        List<OddItem> items = new ArrayList<>();
        if (markets != null) {
            for (JsonNode market : markets) {
                int period = market.path("period").asInt(0);
                BetScope scope = sportRegistry.getSport(sportType).resolvePeriodScope(period);
                String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
                String type = market.path("type").asText();

                marketHandlers.stream()
                        .filter(h -> h.supports(type))
                        .findFirst()
                        .ifPresent(h -> h.handle(market, scope, scopeSuffix, items));
            }
        }

        request.setOdds(items);
        return request;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(JsonNode fixture, JsonNode oddsNode, String sportName, SportType sportType, String leagueName) {
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("pinnacle");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.EU));
        
        String externalEventId = fixture.path("id").asText();
        request.setExternalEventId(externalEventId);
        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);
        
        request.setTeam1(fixture.path("home").asText());
        request.setTeam2(fixture.path("away").asText());
        request.setIsLive(fixture.path("status").asText().equalsIgnoreCase("H"));
        request.setEventUrl("https://www.pinnacle.com/en/" + sportName.toLowerCase().replace(" ", "-") + "/" + leagueName.toLowerCase().replace(" ", "-") + "/match/" + externalEventId);

        String startsStr = fixture.path("starts").asText();
        if (!startsStr.isEmpty()) {
            try {
                request.setStartTime(Instant.parse(startsStr).toEpochMilli());
            } catch (Exception e) {
                log.debug("Failed to parse start time '{}' for event {}: {}", startsStr, externalEventId, e.getMessage());
            }
        }

        List<OddItem> items = new ArrayList<>();
        if (oddsNode != null && oddsNode.has("periods")) {
            for (JsonNode period : oddsNode.path("periods")) {
                int periodNum = period.path("number").asInt();
                BetScope scope = sportRegistry.getSport(sportType).resolvePeriodScope(periodNum);
                String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());

                // 1. Moneyline
                if (period.has("moneyline")) {
                    JsonNode moneyline = period.get("moneyline");
                    addOddItem(items, "moneyline" + scopeSuffix, "HOME", moneyline.path("home").asDouble(), map1X2Record("1", scope, StatType.MATCH));
                    addOddItem(items, "moneyline" + scopeSuffix, "AWAY", moneyline.path("away").asDouble(), map1X2Record("2", scope, StatType.MATCH));
                    if (moneyline.has("draw")) {
                        addOddItem(items, "moneyline" + scopeSuffix, "DRAW", moneyline.path("draw").asDouble(), map1X2Record("X", scope, StatType.MATCH));
                    }
                }

                // 2. Spreads
                if (period.has("spreads")) {
                    for (JsonNode spread : period.get("spreads")) {
                        double hdp = spread.path("hdp").asDouble();
                        addOddItem(items, "spread" + scopeSuffix, "HOME (" + hdp + ")", spread.path("home").asDouble(), 
                                mapHandicapRecord("1", scope, StatType.MATCH, false, hdp));
                        addOddItem(items, "spread" + scopeSuffix, "AWAY (" + (-hdp) + ")", spread.path("away").asDouble(), 
                                mapHandicapRecord("2", scope, StatType.MATCH, false, -hdp));
                    }
                }

                // 3. Totals
                if (period.has("totals")) {
                    for (JsonNode total : period.get("totals")) {
                        double points = total.path("points").asDouble();
                        addOddItem(items, "total" + scopeSuffix, "OVER (" + points + ")", total.path("over").asDouble(), 
                                mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.MATCH, false, points));
                        addOddItem(items, "total" + scopeSuffix, "UNDER (" + points + ")", total.path("under").asDouble(), 
                                mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.MATCH, false, points));
                    }
                }

                // 4. Team Totals
                if (period.has("teamTotal")) {
                    JsonNode tt = period.get("teamTotal");
                    if (tt.has("home")) {
                        for (JsonNode tth : tt.get("home")) {
                            double points = tth.path("points").asDouble();
                            addOddItem(items, "team_total" + scopeSuffix + "_home", "OVER (" + points + ")", tth.path("over").asDouble(),
                                    mapTotalRecord("OVER", scope, BetSubject.TEAM1, StatType.MATCH, false, points));
                            addOddItem(items, "team_total" + scopeSuffix + "_home", "UNDER (" + points + ")", tth.path("under").asDouble(),
                                    mapTotalRecord("UNDER", scope, BetSubject.TEAM1, StatType.MATCH, false, points));
                        }
                    }
                    if (tt.has("away")) {
                        for (JsonNode tta : tt.get("away")) {
                            double points = tta.path("points").asDouble();
                            addOddItem(items, "team_total" + scopeSuffix + "_away", "OVER (" + points + ")", tta.path("over").asDouble(),
                                    mapTotalRecord("OVER", scope, BetSubject.TEAM2, StatType.MATCH, false, points));
                            addOddItem(items, "team_total" + scopeSuffix + "_away", "UNDER (" + points + ")", tta.path("under").asDouble(),
                                    mapTotalRecord("UNDER", scope, BetSubject.TEAM2, StatType.MATCH, false, points));
                        }
                    }
                }

                // 5. Odd/Even (Чёт/Нечёт) — legacy Pinnacle API field "oddEven" or "odd_even"
                JsonNode oeNode = period.has("oddEven") ? period.get("oddEven")
                        : (period.has("odd_even") ? period.get("odd_even") : null);
                if (oeNode != null) {
                    String oeGroup = "odd_even" + scopeSuffix;
                    double oddPrice = oeNode.path("odd").asDouble();
                    double evenPrice = oeNode.path("even").asDouble();
                    if (oddPrice > 1.0) {
                        addOddItem(items, oeGroup, "ODD", oddPrice,
                                new BinaryMarketBet(scope, BetSubject.MATCH,
                                        BinaryMarketBet.MarketType.ODD_EVEN,
                                        BinaryMarketBet.Outcome.ODD, StatType.MATCH));
                    }
                    if (evenPrice > 1.0) {
                        addOddItem(items, oeGroup, "EVEN", evenPrice,
                                new BinaryMarketBet(scope, BetSubject.MATCH,
                                        BinaryMarketBet.MarketType.ODD_EVEN,
                                        BinaryMarketBet.Outcome.EVEN, StatType.MATCH));
                    }
                }

            }
        }

        request.setOdds(items);
        return request;
    }

    private void addOddItem(List<OddItem> items, String groupName, String rawOutcomeName, double value, BetType betType) {
        if (value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(groupName + "_" + rawOutcomeName.replace(" ", "_"));
        item.setGroupName(groupName);
        item.setName(rawOutcomeName);
        item.setValue(value);
        item.setBetType(betType);
        items.add(item);
    }
}

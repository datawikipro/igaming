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
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PinnacleOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "pinnacle".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
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
                if (periodNum != 0) {
                    continue; 
                }

                // 1. Moneyline
                if (period.has("moneyline")) {
                    JsonNode moneyline = period.get("moneyline");
                    addOddItem(items, "moneyline", "HOME", moneyline.path("home").asDouble(), map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH));
                    addOddItem(items, "moneyline", "AWAY", moneyline.path("away").asDouble(), map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH));
                    if (moneyline.has("draw")) {
                        addOddItem(items, "moneyline", "DRAW", moneyline.path("draw").asDouble(), map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH));
                    }
                }

                // 2. Spreads
                if (period.has("spreads")) {
                    for (JsonNode spread : period.get("spreads")) {
                        double hdp = spread.path("hdp").asDouble();
                        addOddItem(items, "spread", "HOME (" + hdp + ")", spread.path("home").asDouble(), 
                                mapHandicapRecord("1", BetScope.FULL_MATCH, StatType.MATCH, false, hdp));
                        addOddItem(items, "spread", "AWAY (" + (-hdp) + ")", spread.path("away").asDouble(), 
                                mapHandicapRecord("2", BetScope.FULL_MATCH, StatType.MATCH, false, -hdp));
                    }
                }

                // 3. Totals
                if (period.has("totals")) {
                    for (JsonNode total : period.get("totals")) {
                        double points = total.path("points").asDouble();
                        addOddItem(items, "total", "OVER (" + points + ")", total.path("over").asDouble(), 
                                mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, points));
                        addOddItem(items, "total", "UNDER (" + points + ")", total.path("under").asDouble(), 
                                mapTotalRecord("UNDER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, points));
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

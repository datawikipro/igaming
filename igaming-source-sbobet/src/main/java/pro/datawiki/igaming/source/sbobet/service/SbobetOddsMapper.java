package pro.datawiki.igaming.source.sbobet.service;

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
public class SbobetOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "sbobet".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(JsonNode event, String sportName, SportType sportType, String leagueName) {
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("sbobet");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.GLOBAL));
        
        String externalEventId = event.path("id").asText();
        request.setExternalEventId(externalEventId);
        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);
        
        request.setTeam1(event.path("home").asText());
        request.setTeam2(event.path("away").asText());
        request.setIsLive(event.path("isLive").asBoolean(false));
        request.setEventUrl("https://www.sbobet.com/euro/football/match/" + externalEventId);

        long startTime = event.path("startTime").asLong(0);
        if (startTime > 0) {
            request.setStartTime(startTime);
        } else {
            request.setStartTime(Instant.now().toEpochMilli() + 3600000); // fallback to 1 hour from now
        }

        List<OddItem> items = new ArrayList<>();
        
        // 1. 1X2 (Moneyline)
        if (event.has("moneyline")) {
            JsonNode ml = event.get("moneyline");
            addOddItem(items, "moneyline", "HOME", ml.path("home").asDouble(), map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH));
            addOddItem(items, "moneyline", "AWAY", ml.path("away").asDouble(), map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH));
            if (ml.has("draw")) {
                addOddItem(items, "moneyline", "DRAW", ml.path("draw").asDouble(), map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH));
            }
        }

        // 2. Handicaps (Asian spreads)
        if (event.has("handicaps")) {
            for (JsonNode hdpNode : event.get("handicaps")) {
                double hdp = hdpNode.path("hdp").asDouble();
                addOddItem(items, "handicap", "HOME (" + hdp + ")", hdpNode.path("home").asDouble(), 
                        mapHandicapRecord("1", BetScope.FULL_MATCH, StatType.MATCH, true, hdp));
                addOddItem(items, "handicap", "AWAY (" + (-hdp) + ")", hdpNode.path("away").asDouble(), 
                        mapHandicapRecord("2", BetScope.FULL_MATCH, StatType.MATCH, true, -hdp));
            }
        }

        // 3. Totals (Asian Over/Under)
        if (event.has("totals")) {
            for (JsonNode totalNode : event.get("totals")) {
                double limit = totalNode.path("limit").asDouble();
                addOddItem(items, "total", "OVER (" + limit + ")", totalNode.path("over").asDouble(), 
                        mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, true, limit));
                addOddItem(items, "total", "UNDER (" + limit + ")", totalNode.path("under").asDouble(), 
                        mapTotalRecord("UNDER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, true, limit));
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

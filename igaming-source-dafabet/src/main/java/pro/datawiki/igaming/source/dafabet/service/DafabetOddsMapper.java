package pro.datawiki.igaming.source.dafabet.service;

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
public class DafabetOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "dafabet".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(JsonNode event, String sportName, SportType sportType, String leagueName) {
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("dafabet");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.GLOBAL));

        // Get Event ID (various field options depending on API version)
        String externalEventId = getFieldAsString(event, "id", "eventId", "EventId", "MatchId");
        if (externalEventId.isEmpty()) {
            return null;
        }
        request.setExternalEventId(externalEventId);
        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);

        // Team names
        String team1 = getFieldAsString(event, "home", "homeName", "HomeName", "Team1", "Home");
        String team2 = getFieldAsString(event, "away", "awayName", "AwayName", "Team2", "Away");
        request.setTeam1(team1.isEmpty() ? "Home Team" : team1);
        request.setTeam2(team2.isEmpty() ? "Away Team" : team2);

        // Live status
        boolean isLive = getFieldAsBoolean(event, "isLive", "live", "IsLive", "IsLiveMatch");
        request.setIsLive(isLive);

        // Event URL
        request.setEventUrl("https://www.dafabet.com/en/sports/match/" + externalEventId);

        // Start time
        long startTime = getFieldAsLong(event, "startTime", "kickOffTime", "KickOffTime", "MatchTime");
        if (startTime > 0) {
            if (startTime < 10000000000L) startTime *= 1000; // convert seconds to ms
            request.setStartTime(startTime);
        } else {
            request.setStartTime(Instant.now().toEpochMilli() + 3600000);
        }

        List<OddItem> items = new ArrayList<>();

        // Map odds - Moneyline / 1X2
        JsonNode mlNode = getNestedNode(event, "moneyline", "moneyLine", "odds1x2", "Odds1X2");
        if (mlNode != null && !mlNode.isMissingNode()) {
            double homeOdds = getFieldAsDouble(mlNode, "home", "h", "1", "HomeOdds");
            double awayOdds = getFieldAsDouble(mlNode, "away", "a", "2", "AwayOdds");
            double drawOdds = getFieldAsDouble(mlNode, "draw", "d", "x", "X", "DrawOdds");

            addOddItem(items, "moneyline", "HOME", homeOdds, map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH));
            addOddItem(items, "moneyline", "AWAY", awayOdds, map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH));
            if (drawOdds > 1.0) {
                addOddItem(items, "moneyline", "DRAW", drawOdds, map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH));
            }
        }

        // Map odds - Handicaps
        JsonNode hdps = getNestedNode(event, "handicaps", "handicap", "hdp", "HdpOdds");
        if (hdps != null && hdps.isArray()) {
            for (JsonNode hdpNode : hdps) {
                double limit = getFieldAsDouble(hdpNode, "limit", "hdp", "point", "Point");
                double homeOdds = getFieldAsDouble(hdpNode, "home", "h", "HomeOdds");
                double awayOdds = getFieldAsDouble(hdpNode, "away", "a", "AwayOdds");

                addOddItem(items, "handicap", "HOME (" + limit + ")", homeOdds,
                        mapHandicapRecord("1", BetScope.FULL_MATCH, StatType.MATCH, true, limit));
                addOddItem(items, "handicap", "AWAY (" + (-limit) + ")", awayOdds,
                        mapHandicapRecord("2", BetScope.FULL_MATCH, StatType.MATCH, true, -limit));
            }
        }

        // Map odds - Totals
        JsonNode totals = getNestedNode(event, "totals", "total", "ou", "OuOdds");
        if (totals != null && totals.isArray()) {
            for (JsonNode totalNode : totals) {
                double limit = getFieldAsDouble(totalNode, "limit", "ou", "point", "Point");
                double overOdds = getFieldAsDouble(totalNode, "over", "o", "OverOdds");
                double underOdds = getFieldAsDouble(totalNode, "under", "u", "UnderOdds");

                addOddItem(items, "total", "OVER (" + limit + ")", overOdds,
                        mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, true, limit));
                addOddItem(items, "total", "UNDER (" + limit + ")", underOdds,
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

    // --- Helper extraction methods for robust parsing ---

    private String getFieldAsString(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) {
                return node.path(key).asText("");
            }
        }
        return "";
    }

    private boolean getFieldAsBoolean(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) {
                return node.path(key).asBoolean(false);
            }
        }
        return false;
    }

    private long getFieldAsLong(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) {
                return node.path(key).asLong(0);
            }
        }
        return 0;
    }

    private double getFieldAsDouble(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) {
                return node.path(key).asDouble(0.0);
            }
        }
        return 0.0;
    }

    private JsonNode getNestedNode(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) {
                return node.get(key);
            }
        }
        return null;
    }
}

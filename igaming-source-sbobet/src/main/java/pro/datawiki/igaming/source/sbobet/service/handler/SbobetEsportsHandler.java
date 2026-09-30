package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.List;

@Component
public class SbobetEsportsHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String lower = marketKey.toLowerCase();
        return lower.startsWith("esport") || lower.startsWith("map") || lower.startsWith("round")
                || lower.contains("map_winner") || lower.contains("maps_total") || lower.contains("maps_handicap");
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        if (isEsports(sportType)) {
            return true;
        }
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        handle(marketNode, SportType.ESPORTS, items);
    }

    public static boolean isEsports(SportType sportType) {
        if (sportType == null) return false;
        return sportType == SportType.ESPORTS
                || sportType == SportType.CS2
                || sportType == SportType.DOTA2
                || sportType == SportType.LEAGUE_OF_LEGENDS
                || sportType == SportType.VALORANT
                || sportType == SportType.STARCRAFT
                || sportType == SportType.RAINBOW_SIX
                || sportType == SportType.OVERWATCH
                || sportType == SportType.CALL_OF_DUTY;
    }

    @Override
    public void handle(JsonNode marketNode, SportType sportType, List<OddItem> items) {
        if (marketNode == null) return;

        // 1. Map winners (map1, map2, map3, map4, map5)
        for (int mapIdx = 1; mapIdx <= 5; mapIdx++) {
            String mapKey = "map" + mapIdx;
            BetScope mapScope = resolveMapScope(mapIdx);
            if (mapScope == null) continue;

            JsonNode mapNode = marketNode.has(mapKey) ? marketNode.get(mapKey) : null;
            if (mapNode != null) {
                handleMapWinner(mapNode, mapKey, mapScope, items);
                handleMapTotals(mapNode, mapKey, mapScope, items);
                handleMapHandicaps(mapNode, mapKey, mapScope, items);
            }
        }

        // 2. Maps Total (StatType.MAPS, BetScope.FULL_MATCH)
        JsonNode mapsTotalNode = marketNode.has("maps_total") ? marketNode.get("maps_total") : null;
        if (mapsTotalNode != null && mapsTotalNode.isArray()) {
            for (JsonNode tNode : mapsTotalNode) {
                double limit = tNode.path("limit").asDouble();
                if (tNode.has("over")) {
                    addTotal(items, "maps_total", "OVER (" + limit + ")", tNode.path("over").asDouble(),
                            BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, limit, true, StatType.MAPS);
                }
                if (tNode.has("under")) {
                    addTotal(items, "maps_total", "UNDER (" + limit + ")", tNode.path("under").asDouble(),
                            BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, true, StatType.MAPS);
                }
            }
        }

        // 3. Maps Handicap (StatType.MAPS, BetScope.FULL_MATCH)
        JsonNode mapsHdpNode = marketNode.has("maps_handicap") ? marketNode.get("maps_handicap") : null;
        if (mapsHdpNode != null && mapsHdpNode.isArray()) {
            for (JsonNode hNode : mapsHdpNode) {
                double hdp = hNode.path("hdp").asDouble();
                if (hNode.has("home")) {
                    addHandicap(items, "maps_handicap", "HOME (" + hdp + ")", hNode.path("home").asDouble(),
                            BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, hdp, true, StatType.MAPS);
                }
                if (hNode.has("away")) {
                    addHandicap(items, "maps_handicap", "AWAY (" + (-hdp) + ")", hNode.path("away").asDouble(),
                            BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, -hdp, true, StatType.MAPS);
                }
            }
        }
    }

    private void handleMapWinner(JsonNode mapNode, String groupPrefix, BetScope mapScope, List<OddItem> items) {
        JsonNode winnerNode = mapNode.has("winner") ? mapNode.get("winner") : mapNode;
        if (winnerNode.has("home")) {
            addMatchResult(items, groupPrefix + "_winner", "HOME",
                    winnerNode.path("home").asDouble(), mapScope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH);
        }
        if (winnerNode.has("away")) {
            addMatchResult(items, groupPrefix + "_winner", "AWAY",
                    winnerNode.path("away").asDouble(), mapScope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH);
        }
    }

    private void handleMapTotals(JsonNode mapNode, String groupPrefix, BetScope mapScope, List<OddItem> items) {
        JsonNode totalsNode = mapNode.has("totals") ? mapNode.get("totals") : null;
        if (totalsNode != null && totalsNode.isArray()) {
            for (JsonNode tNode : totalsNode) {
                double limit = tNode.path("limit").asDouble();
                if (tNode.has("over")) {
                    addTotal(items, groupPrefix + "_rounds_total", "OVER (" + limit + ")", tNode.path("over").asDouble(),
                            mapScope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, true, StatType.ROUNDS);
                }
                if (tNode.has("under")) {
                    addTotal(items, groupPrefix + "_rounds_total", "UNDER (" + limit + ")", tNode.path("under").asDouble(),
                            mapScope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, true, StatType.ROUNDS);
                }
            }
        }
    }

    private void handleMapHandicaps(JsonNode mapNode, String groupPrefix, BetScope mapScope, List<OddItem> items) {
        JsonNode handicapsNode = mapNode.has("handicaps") ? mapNode.get("handicaps") : null;
        if (handicapsNode != null && handicapsNode.isArray()) {
            for (JsonNode hNode : handicapsNode) {
                double hdp = hNode.path("hdp").asDouble();
                if (hNode.has("home")) {
                    addHandicap(items, groupPrefix + "_rounds_handicap", "HOME (" + hdp + ")", hNode.path("home").asDouble(),
                            mapScope, HandicapBet.Outcome.TEAM1, hdp, true, StatType.ROUNDS);
                }
                if (hNode.has("away")) {
                    addHandicap(items, groupPrefix + "_rounds_handicap", "AWAY (" + (-hdp) + ")", hNode.path("away").asDouble(),
                            mapScope, HandicapBet.Outcome.TEAM2, -hdp, true, StatType.ROUNDS);
                }
            }
        }
    }

    private BetScope resolveMapScope(int mapIdx) {
        return switch (mapIdx) {
            case 1 -> BetScope.MAP_1;
            case 2 -> BetScope.MAP_2;
            case 3 -> BetScope.MAP_3;
            case 4 -> BetScope.MAP_4;
            case 5 -> BetScope.MAP_5;
            default -> null;
        };
    }
}

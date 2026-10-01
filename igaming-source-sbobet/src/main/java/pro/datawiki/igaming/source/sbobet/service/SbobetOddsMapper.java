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
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetEsportsHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetMarketHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class SbobetOddsMapper extends AbstractBetTypeMapper {

    private static final Pattern MAP_INDEX_PATTERN = Pattern.compile("map[_-]?([1-5])(?![0-9])");

    private final SportNormalizationService sportNormalizationService;
    private final List<SbobetMarketHandler> marketHandlers;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "sbobet".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        if (m == null || o == null) return null;
        String mLower = m.toLowerCase();

        StatType statType = StatType.MATCH;
        if (mLower.contains("corner")) {
            statType = StatType.CORNERS;
        } else if (mLower.contains("card") || mLower.contains("booking")) {
            statType = StatType.YELLOW_CARDS;
        } else if (mLower.contains("round")) {
            statType = StatType.ROUNDS;
        } else if (mLower.contains("maps_total") || mLower.contains("maps_handicap")
                || mLower.contains("map_total") || mLower.contains("map_handicap")
                || mLower.contains("maps_totals") || mLower.contains("maps_handicaps")
                || mLower.contains("maps")) {
            statType = StatType.MAPS;
        }

        BetScope scope = BetScope.FULL_MATCH;
        if (mLower.contains("half1") || mLower.contains("half_1") || mLower.contains("1st_half")) {
            scope = BetScope.HALF_1;
        } else if (mLower.contains("half2") || mLower.contains("half_2") || mLower.contains("2nd_half")) {
            scope = BetScope.HALF_2;
        } else {
            Matcher mapMatcher = MAP_INDEX_PATTERN.matcher(mLower);
            if (mapMatcher.find()) {
                int mapIdx = Integer.parseInt(mapMatcher.group(1));
                scope = resolveMapScope(mapIdx);
            }
        }

        if (mLower.contains("moneyline") || mLower.contains("1x2") || mLower.contains("winner")
                || (scope != BetScope.FULL_MATCH && scope != BetScope.HALF_1 && scope != BetScope.HALF_2
                && !mLower.contains("total") && !mLower.contains("handicap") && !mLower.contains("spread") && !mLower.contains("round"))) {
            if ("win1_2way".equalsIgnoreCase(o) || "1_2way".equalsIgnoreCase(o)) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType);
            } else if ("win2_2way".equalsIgnoreCase(o) || "2_2way".equalsIgnoreCase(o)) {
                return new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType);
            }
            return map1X2Record(o, scope, statType);
        } else if (mLower.contains("handicap") || mLower.contains("spread") || mLower.contains("hdp")) {
            return mapHandicapRecord(o, scope, statType, true, param);
        } else if (mLower.contains("total")) {
            BetSubject subject = BetSubject.MATCH;
            if (mLower.contains("home") || mLower.contains("team1")) {
                subject = BetSubject.TEAM1;
            } else if (mLower.contains("away") || mLower.contains("team2")) {
                subject = BetSubject.TEAM2;
            }
            return mapTotalRecord(o, scope, subject, statType, true, param);
        } else if (mLower.contains("dc") || mLower.contains("double_chance")) {
            return map1X2DCRecord(o, scope, statType);
        } else if (mLower.contains("btts") || mLower.contains("both_teams_to_score")) {
            return new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS,
                    "yes".equalsIgnoreCase(o) ? BinaryMarketBet.Outcome.YES : BinaryMarketBet.Outcome.NO, statType);
        } else if (mLower.contains("dnb") || mLower.contains("draw_no_bet")) {
            return mapHandicapRecord(o, scope, statType, true, 0.0);
        }
        return null;
    }

    private BetScope resolveMapScope(int mapIdx) {
        return switch (mapIdx) {
            case 1 -> BetScope.MAP_1;
            case 2 -> BetScope.MAP_2;
            case 3 -> BetScope.MAP_3;
            case 4 -> BetScope.MAP_4;
            case 5 -> BetScope.MAP_5;
            default -> BetScope.FULL_MATCH;
        };
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

        String sportPath = "football";
        if (sportName != null && !sportName.isBlank()) {
            sportPath = sportName.toLowerCase().replace(" ", "-");
        } else if (SbobetEsportsHandler.isEsports(sportType)) {
            sportPath = "esports";
        }
        request.setEventUrl("https://www.sbobet.com/euro/" + sportPath + "/match/" + externalEventId);

        long startTime = event.path("startTime").asLong(0);
        if (startTime > 0) {
            request.setStartTime(startTime);
        } else {
            request.setStartTime(Instant.now().toEpochMilli() + 3600000);
        }

        List<OddItem> items = new ArrayList<>();
        event.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode node = entry.getValue();
            marketHandlers.stream()
                    .filter(h -> h.supports(key, sportType))
                    .findFirst()
                    .ifPresent(h -> h.handle(key, node, sportType, items));
        });

        request.setOdds(items);
        return request;
    }
}

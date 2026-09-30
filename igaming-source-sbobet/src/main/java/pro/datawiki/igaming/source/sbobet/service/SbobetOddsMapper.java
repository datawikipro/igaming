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
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetMarketHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SbobetOddsMapper extends AbstractBetTypeMapper {

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
        }

        BetScope scope = BetScope.FULL_MATCH;
        if (mLower.contains("half1") || mLower.contains("half_1") || mLower.contains("1st_half")) {
            scope = BetScope.HALF_1;
        } else if (mLower.contains("half2") || mLower.contains("half_2") || mLower.contains("2nd_half")) {
            scope = BetScope.HALF_2;
        }

        if (mLower.contains("moneyline") || mLower.contains("1x2") || mLower.contains("winner")) {
            return map1X2Record(o, scope, statType);
        } else if (mLower.contains("handicap") || mLower.contains("spread")) {
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

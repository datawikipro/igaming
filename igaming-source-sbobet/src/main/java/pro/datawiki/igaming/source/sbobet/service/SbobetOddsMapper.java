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
                    .filter(h -> h.supports(key))
                    .findFirst()
                    .ifPresent(h -> h.handle(node, items));
        });

        request.setOdds(items);
        return request;
    }
}

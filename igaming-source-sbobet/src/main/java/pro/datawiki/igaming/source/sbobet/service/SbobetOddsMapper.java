package pro.datawiki.igaming.source.sbobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.sbobet.service.handler.AbstractSbobetMarketHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetMarketContext;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetMarketHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class SbobetOddsMapper extends AbstractBetTypeMapper {

    private final SportNormalizationService sportNormalizationService;
    private final List<SbobetMarketHandler> marketHandlers;

    public SbobetOddsMapper(SportNormalizationService sportNormalizationService,
                            List<SbobetMarketHandler> marketHandlers) {
        this.sportNormalizationService = sportNormalizationService;
        List<SbobetMarketHandler> sorted = new ArrayList<>(marketHandlers != null ? marketHandlers : List.of());
        AnnotationAwareOrderComparator.sort(sorted);
        this.marketHandlers = Collections.unmodifiableList(sorted);
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "sbobet".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public List<SbobetMarketHandler> getMarketHandlers() {
        return marketHandlers;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(JsonNode event, String sportName, SportType sportType, String leagueName) {
        if (event == null || !event.isObject()) {
            return null;
        }

        if (sportType == null && sportName != null && sportNormalizationService != null) {
            sportType = sportNormalizationService.normalize(sportName);
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("sbobet");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.GLOBAL));

        String externalEventId = event.path("id").asText();
        request.setExternalEventId(externalEventId);
        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);

        String team1 = event.has("home") ? event.path("home").asText() : event.path("team1").asText("");
        String team2 = event.has("away") ? event.path("away").asText() : event.path("team2").asText("");
        request.setTeam1(team1);
        request.setTeam2(team2);
        request.setIsLive(event.path("isLive").asBoolean(event.path("is_live").asBoolean(false)));

        String sportSegment = (sportName != null && !sportName.isBlank())
                ? sportName.toLowerCase().replace(" ", "-")
                : "football";
        request.setEventUrl("https://www.sbobet.com/euro/" + sportSegment + "/match/" + externalEventId);

        long startTime = event.path("startTime").asLong(event.path("start_time").asLong(0));
        if (startTime > 0) {
            request.setStartTime(startTime);
        } else {
            request.setStartTime(Instant.now().toEpochMilli() + 3600000);
        }

        SbobetMarketContext context = SbobetMarketContext.builder()
                .sportName(sportName)
                .sportType(sportType)
                .leagueName(leagueName)
                .team1(team1)
                .team2(team2)
                .isLive(request.getIsLive())
                .externalEventId(externalEventId)
                .eventNode(event)
                .build();

        List<OddItem> items = new ArrayList<>();
        processEventNode(event, context, items);

        request.setOdds(items);
        return request;
    }

    private void processEventNode(JsonNode event, SbobetMarketContext context, List<OddItem> items) {
        event.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode node = entry.getValue();

            if (AbstractSbobetMarketHandler.isNonMarketKey(key)) {
                return;
            }

            if ("markets".equalsIgnoreCase(key) || "odds".equalsIgnoreCase(key)) {
                processContainerNode(node, context, items);
                return;
            }

            dispatchMarket(key, node, context, items);
        });
    }

    private void processContainerNode(JsonNode containerNode, SbobetMarketContext context, List<OddItem> items) {
        if (containerNode == null || containerNode.isNull()) {
            return;
        }

        if (containerNode.isObject()) {
            containerNode.fields().forEachRemaining(entry -> {
                String subKey = entry.getKey();
                JsonNode subNode = entry.getValue();
                if (!AbstractSbobetMarketHandler.isNonMarketKey(subKey)) {
                    dispatchMarket(subKey, subNode, context, items);
                }
            });
        } else if (containerNode.isArray()) {
            for (JsonNode itemNode : containerNode) {
                String marketKey = extractMarketKey(itemNode);
                if (marketKey != null && !marketKey.isBlank()) {
                    dispatchMarket(marketKey, itemNode, context, items);
                }
            }
        }
    }

    private String extractMarketKey(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        for (String field : List.of("key", "marketKey", "market_key", "name", "marketName", "market_name", "type")) {
            if (node.has(field)) {
                String val = node.path(field).asText();
                if (!val.isBlank()) {
                    return val;
                }
            }
        }
        return null;
    }

    private void dispatchMarket(String key, JsonNode node, SbobetMarketContext context, List<OddItem> items) {
        if (node == null || node.isNull()) {
            return;
        }
        for (SbobetMarketHandler handler : marketHandlers) {
            if (handler.supports(key, context)) {
                handler.handle(key, node, context, items);
                break;
            }
        }
    }
}

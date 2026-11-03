package pro.datawiki.igaming.analyst.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;
import pro.datawiki.igaming.dto.CanonicalRefereeDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Reactive Kafka consumer for referee strictness and card distribution metrics.
 * Listens to topic 'sports.canonical.referees' and updates match tactical dossier in real time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveRefereeKafkaConsumer {

    private final TacticalDossierService tacticalDossierService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.referees:sports.canonical.referees}",
            groupId = "${spring.kafka.consumer.group-id:igaming-ai-analyst-group}"
    )
    public void consumeReferee(Object rawMessage) {
        if (rawMessage == null) {
            log.warn("[Reactive Referee Consumer] Received null message from Kafka");
            return;
        }

        try {
            CanonicalRefereeDto dto = parsePayload(rawMessage);
            if (dto != null && dto.getMatchId() != null) {
                tacticalDossierService.enrichWithReferee(dto);
                log.info("[Reactive Referee Consumer] Successfully enriched dossier for match [{}] with referee={}, strictnessIndex={}, tier={}",
                        dto.getMatchId(), dto.getRefereeName(), dto.getStrictnessIndex(), dto.getStrictnessTier());
            }
        } catch (Exception e) {
            log.error("[Reactive Referee Consumer] Error processing referee message: {}", e.getMessage(), e);
        }
    }

    private CanonicalRefereeDto parsePayload(Object raw) throws Exception {
        if (raw instanceof CanonicalRefereeDto dto) {
            return dto;
        }

        String jsonStr = (raw instanceof String str) ? str : objectMapper.writeValueAsString(raw);
        JsonNode root = objectMapper.readTree(jsonStr);

        String matchId = root.has("matchId") ? root.get("matchId").asText() :
                         root.has("match_id") ? root.get("match_id").asText() : "unknown-match";

        String refName = root.has("refereeName") ? root.get("refereeName").asText() :
                         root.has("referee_name") ? root.get("referee_name").asText() : "Unknown Referee";

        double strictness = root.has("strictnessIndex") ? root.get("strictnessIndex").asDouble() :
                            root.has("strictness_index") ? root.get("strictness_index").asDouble() : 5.0;

        String tier = root.has("strictnessTier") ? root.get("strictnessTier").asText() :
                      root.has("strictness_tier") ? root.get("strictness_tier").asText() : "BALANCED";

        double yc = root.has("yellowCardsPerMatch") ? root.get("yellowCardsPerMatch").asDouble() :
                    root.has("yellow_cards_per_match") ? root.get("yellow_cards_per_match").asDouble() : 4.10;

        double rc = root.has("redCardsPerMatch") ? root.get("redCardsPerMatch").asDouble() :
                    root.has("red_cards_per_match") ? root.get("red_cards_per_match").asDouble() : 0.16;

        double fouls = root.has("foulsPerMatch") ? root.get("foulsPerMatch").asDouble() :
                       root.has("fouls_per_match") ? root.get("fouls_per_match").asDouble() : 22.5;

        double pens = root.has("penaltiesPerMatch") ? root.get("penaltiesPerMatch").asDouble() :
                      root.has("penalties_per_match") ? root.get("penalties_per_match").asDouble() : 0.28;

        List<String> recs = new ArrayList<>();
        JsonNode recArr = root.has("recommendations") ? root.get("recommendations") : null;
        if (recArr != null && recArr.isArray()) {
            for (JsonNode r : recArr) {
                recs.add(r.isTextual() ? r.asText() : r.toString());
            }
        }

        return CanonicalRefereeDto.builder()
                .matchId(matchId)
                .refereeName(refName)
                .country(root.has("country") ? root.get("country").asText() : "International")
                .matchesAnalyzed(root.has("matchesAnalyzed") ? root.get("matchesAnalyzed").asInt() : 15)
                .yellowCardsPerMatch(yc)
                .redCardsPerMatch(rc)
                .foulsPerMatch(fouls)
                .penaltiesPerMatch(pens)
                .strictnessIndex(strictness)
                .strictnessTier(tier)
                .recommendations(recs)
                .dataSource(root.has("dataSource") ? root.get("dataSource").asText() : "KAFKA")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

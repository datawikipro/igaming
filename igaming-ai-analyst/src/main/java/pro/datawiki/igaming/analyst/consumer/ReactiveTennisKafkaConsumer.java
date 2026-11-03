package pro.datawiki.igaming.analyst.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;
import pro.datawiki.igaming.dto.CanonicalTennisPointDto;

/**
 * Reactive Kafka consumer for live tennis point-by-point momentum streams.
 * Listens to topic 'sports.tennis.point-by-point' and adjusts tactical tennis in-play models.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveTennisKafkaConsumer {

    private final TacticalDossierService tacticalDossierService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.tennis-points:sports.tennis.point-by-point}",
            groupId = "${spring.kafka.consumer.group-id:igaming-ai-analyst-group}"
    )
    public void consumeTennisPoints(Object rawMessage) {
        if (rawMessage == null) {
            log.warn("[Reactive Tennis Consumer] Received null message from Kafka");
            return;
        }

        try {
            CanonicalTennisPointDto dto = parsePayload(rawMessage);
            if (dto != null) {
                tacticalDossierService.enrichWithTennisPoints(dto);
                log.info("[Reactive Tennis Consumer] Successfully enriched dossier for player [{}] momentumScore={}",
                        dto.getPlayerName(), dto.getMomentumScore());
            }
        } catch (Exception e) {
            log.error("[Reactive Tennis Consumer] Error processing tennis point message: {}", e.getMessage(), e);
        }
    }

    private CanonicalTennisPointDto parsePayload(Object raw) throws Exception {
        if (raw instanceof CanonicalTennisPointDto dto) {
            return dto;
        }

        String jsonStr = (raw instanceof String str) ? str : objectMapper.writeValueAsString(raw);
        JsonNode root = objectMapper.readTree(jsonStr);

        String matchId = root.has("matchId") ? root.get("matchId").asText() :
                         root.has("match_id") ? root.get("match_id").asText() : null;

        String player = root.has("playerName") ? root.get("playerName").asText() :
                        root.has("player_name") ? root.get("player_name").asText() : "Player 1";

        double firstServe = root.has("firstServePct") ? root.get("firstServePct").asDouble() :
                            root.has("first_serve_pct") ? root.get("first_serve_pct").asDouble() : 0.65;

        double secondServeWin = root.has("secondServeWinPct") ? root.get("secondServeWinPct").asDouble() :
                                root.has("second_serve_win_pct") ? root.get("second_serve_win_pct").asDouble() : 0.52;

        double momentum = root.has("momentumScore") ? root.get("momentumScore").asDouble() :
                          root.has("momentum_score") ? root.get("momentum_score").asDouble() : 0.72;

        return CanonicalTennisPointDto.builder()
                .matchId(matchId != null ? matchId : player)
                .playerName(player)
                .opponentName(root.has("opponentName") ? root.get("opponentName").asText() : "Opponent")
                .surface(root.has("surface") ? root.get("surface").asText() : "Hard")
                .firstServePct(firstServe)
                .secondServeWinPct(secondServeWin)
                .momentumScore(momentum)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

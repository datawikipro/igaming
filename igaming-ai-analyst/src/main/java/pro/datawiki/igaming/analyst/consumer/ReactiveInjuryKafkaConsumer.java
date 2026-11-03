package pro.datawiki.igaming.analyst.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;
import pro.datawiki.igaming.dto.CanonicalInjuryReportDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Reactive Kafka consumer for canonical absent player and injury reports.
 * Listens to topic 'sports.canonical.injuries' and immediately updates the tactical dossier.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveInjuryKafkaConsumer {

    private final TacticalDossierService tacticalDossierService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.injuries:sports.canonical.injuries}",
            groupId = "${spring.kafka.consumer.group-id:igaming-ai-analyst-group}"
    )
    public void consumeInjuryReport(Object rawMessage) {
        if (rawMessage == null) {
            log.warn("[Reactive Injury Consumer] Received null message from Kafka");
            return;
        }

        try {
            CanonicalInjuryReportDto dto = parsePayload(rawMessage);
            if (dto != null && dto.getMatchId() != null) {
                tacticalDossierService.enrichWithInjuries(dto);
                log.info("[Reactive Injury Consumer] Successfully enriched dossier for match [{}] with netAdvantage={}",
                        dto.getMatchId(), dto.getNetAdvantageHome());
            }
        } catch (Exception e) {
            log.error("[Reactive Injury Consumer] Error processing injury message: {}", e.getMessage(), e);
        }
    }

    private CanonicalInjuryReportDto parsePayload(Object raw) throws Exception {
        if (raw instanceof CanonicalInjuryReportDto dto) {
            return dto;
        }

        String jsonStr = (raw instanceof String str) ? str : objectMapper.writeValueAsString(raw);
        JsonNode root = objectMapper.readTree(jsonStr);

        String matchId = root.has("matchExternalId") ? root.get("matchExternalId").asText() :
                         root.has("matchId") ? root.get("matchId").asText() :
                         root.has("match_id") ? root.get("match_id").asText() : "unknown-match";

        double homeImpact = root.has("homeAbsenceImpact") ? root.get("homeAbsenceImpact").asDouble() :
                            root.has("home_absence_impact") ? root.get("home_absence_impact").asDouble() : 0.0;

        double awayImpact = root.has("awayAbsenceImpact") ? root.get("awayAbsenceImpact").asDouble() :
                            root.has("away_absence_impact") ? root.get("away_absence_impact").asDouble() : 0.0;

        double netAdvantage = root.has("netAdvantageHome") ? root.get("netAdvantageHome").asDouble() :
                              root.has("net_advantage_home") ? root.get("net_advantage_home").asDouble() : (homeImpact - awayImpact);

        double conf = root.has("confidenceScore") ? root.get("confidenceScore").asDouble() :
                      root.has("confidence_score") ? root.get("confidence_score").asDouble() : 0.95;

        List<String> homePlayers = extractPlayerNames(root, "homeInjuries", "home_absent_players");
        List<String> awayPlayers = extractPlayerNames(root, "awayInjuries", "away_absent_players");

        return CanonicalInjuryReportDto.builder()
                .matchId(matchId)
                .sport(root.has("sport") ? root.get("sport").asText() : "football")
                .homeTeam(root.has("homeTeam") ? root.get("homeTeam").asText() : "Home")
                .awayTeam(root.has("awayTeam") ? root.get("awayTeam").asText() : "Away")
                .homeAbsenceImpact(homeImpact)
                .awayAbsenceImpact(awayImpact)
                .netAdvantageHome(netAdvantage)
                .confidenceScore(conf)
                .homeAbsentCount(homePlayers.size())
                .awayAbsentCount(awayPlayers.size())
                .homeAbsentPlayers(homePlayers)
                .awayAbsentPlayers(awayPlayers)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private List<String> extractPlayerNames(JsonNode root, String key1, String key2) {
        List<String> result = new ArrayList<>();
        JsonNode arr = root.has(key1) ? root.get(key1) : root.has(key2) ? root.get(key2) : null;
        if (arr != null && arr.isArray()) {
            for (JsonNode item : arr) {
                if (item.isTextual()) {
                    result.add(item.asText());
                } else if (item.has("playerName")) {
                    result.add(item.get("playerName").asText());
                } else if (item.has("player_name")) {
                    result.add(item.get("player_name").asText());
                }
            }
        }
        return result;
    }
}

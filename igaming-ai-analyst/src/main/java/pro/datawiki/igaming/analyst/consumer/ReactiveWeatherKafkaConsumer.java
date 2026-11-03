package pro.datawiki.igaming.analyst.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;
import pro.datawiki.igaming.dto.CanonicalWeatherDto;

/**
 * Reactive Kafka consumer for stadium weather forecasts and totals bias.
 * Listens to topic 'sports.canonical.weather' and enriches the match dossier.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveWeatherKafkaConsumer {

    private final TacticalDossierService tacticalDossierService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.weather:sports.canonical.weather}",
            groupId = "${spring.kafka.consumer.group-id:igaming-ai-analyst-group}"
    )
    public void consumeWeather(Object rawMessage) {
        if (rawMessage == null) {
            log.warn("[Reactive Weather Consumer] Received null message from Kafka");
            return;
        }

        try {
            CanonicalWeatherDto dto = parsePayload(rawMessage);
            if (dto != null && dto.getMatchId() != null) {
                tacticalDossierService.enrichWithWeather(dto);
                log.info("[Reactive Weather Consumer] Successfully enriched dossier for match [{}] with condition={}, underBias={}",
                        dto.getMatchId(), dto.getCondition(), dto.getUnderBias());
            }
        } catch (Exception e) {
            log.error("[Reactive Weather Consumer] Error processing weather message: {}", e.getMessage(), e);
        }
    }

    private CanonicalWeatherDto parsePayload(Object raw) throws Exception {
        if (raw instanceof CanonicalWeatherDto dto) {
            return dto;
        }

        String jsonStr = (raw instanceof String str) ? str : objectMapper.writeValueAsString(raw);
        JsonNode root = objectMapper.readTree(jsonStr);

        String matchId = root.has("matchId") ? root.get("matchId").asText() :
                         root.has("match_id") ? root.get("match_id").asText() : "unknown-match";

        String condition = root.has("condition") ? root.get("condition").asText() : "CLEAR";
        double temp = root.has("temperatureC") ? root.get("temperatureC").asDouble() :
                      root.has("temperature_c") ? root.get("temperature_c").asDouble() : 18.0;

        double wind = root.has("windSpeedKmh") ? root.get("windSpeedKmh").asDouble() :
                      root.has("wind_speed_kmh") ? root.get("wind_speed_kmh").asDouble() : 10.0;

        double precip = root.has("precipitationProbability") ? root.get("precipitationProbability").asDouble() :
                        root.has("precipitation_probability") ? root.get("precipitation_probability").asDouble() : 0.0;

        double underBias = root.has("underBias") ? root.get("underBias").asDouble() :
                           root.has("under_bias") ? root.get("under_bias").asDouble() : 0.0;

        boolean outdoor = root.has("outdoor") ? root.get("outdoor").asBoolean() : true;

        return CanonicalWeatherDto.builder()
                .matchId(matchId)
                .sportName(root.has("sportName") ? root.get("sportName").asText() : "football")
                .homeTeam(root.has("team1") ? root.get("team1").asText() : "Home")
                .awayTeam(root.has("team2") ? root.get("team2").asText() : "Away")
                .condition(condition)
                .temperatureC(temp)
                .windSpeedKmh(wind)
                .precipitationProbability(precip)
                .underBias(underBias)
                .outdoor(outdoor)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

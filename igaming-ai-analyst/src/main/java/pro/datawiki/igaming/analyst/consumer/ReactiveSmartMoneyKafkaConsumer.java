package pro.datawiki.igaming.analyst.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;
import pro.datawiki.igaming.dto.CanonicalSmartMoneyAlertDto;

/**
 * Reactive Kafka consumer for Betfair Smart Money and exchange liquidity spikes.
 * Listens to topic 'odds.smart-money' and projects institutional liquidity into the tactical tensor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveSmartMoneyKafkaConsumer {

    private final TacticalDossierService tacticalDossierService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.smart-money:odds.smart-money}",
            groupId = "${spring.kafka.consumer.group-id:igaming-ai-analyst-group}"
    )
    public void consumeSmartMoney(Object rawMessage) {
        if (rawMessage == null) {
            log.warn("[Reactive Smart Money Consumer] Received null message from Kafka");
            return;
        }

        try {
            CanonicalSmartMoneyAlertDto dto = parsePayload(rawMessage);
            if (dto != null) {
                tacticalDossierService.enrichWithSmartMoney(dto);
                log.info("[Reactive Smart Money Consumer] Successfully enriched dossier for alert [{}] side={}, volumeEur={}",
                        dto.getAlertId(), dto.getSharpSide(), dto.getMatchedVolumeEur());
            }
        } catch (Exception e) {
            log.error("[Reactive Smart Money Consumer] Error processing smart money message: {}", e.getMessage(), e);
        }
    }

    private CanonicalSmartMoneyAlertDto parsePayload(Object raw) throws Exception {
        if (raw instanceof CanonicalSmartMoneyAlertDto dto) {
            return dto;
        }

        String jsonStr = (raw instanceof String str) ? str : objectMapper.writeValueAsString(raw);
        JsonNode root = objectMapper.readTree(jsonStr);

        String alertId = root.has("alertId") ? root.get("alertId").asText() :
                         root.has("alert_id") ? root.get("alert_id").asText() : "alert-" + System.currentTimeMillis();

        String eventId = root.has("eventId") ? root.get("eventId").asText() :
                         root.has("event_id") ? root.get("event_id").asText() :
                         root.has("matchId") ? root.get("matchId").asText() : null;

        String marketId = root.has("marketId") ? root.get("marketId").asText() :
                          root.has("market_id") ? root.get("market_id").asText() : null;

        String sharpSide = root.has("sharpSide") ? root.get("sharpSide").asText() :
                           root.has("sharp_side") ? root.get("sharp_side").asText() :
                           root.has("selectionName") ? root.get("selectionName").asText() : "HOME";

        double volume = root.has("matchedVolumeEur") ? root.get("matchedVolumeEur").asDouble() :
                        root.has("matched_volume_eur") ? root.get("matched_volume_eur").asDouble() :
                        root.has("volume") ? root.get("volume").asDouble() : 50000.0;

        double disparity = root.has("priceDisparity") ? root.get("priceDisparity").asDouble() :
                           root.has("price_disparity") ? root.get("price_disparity").asDouble() : 0.08;

        double signal = root.has("signalStrength") ? root.get("signalStrength").asDouble() :
                        root.has("signal_strength") ? root.get("signal_strength").asDouble() : 0.85;

        String desc = root.has("description") ? root.get("description").asText() : "Smart money pressure detected";

        return CanonicalSmartMoneyAlertDto.builder()
                .alertId(alertId)
                .marketId(marketId)
                .eventId(eventId)
                .sport(root.has("sport") ? root.get("sport").asText() : "football")
                .matchedVolumeEur(volume)
                .sharpSide(sharpSide)
                .priceDisparity(disparity)
                .signalStrength(signal)
                .description(desc)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

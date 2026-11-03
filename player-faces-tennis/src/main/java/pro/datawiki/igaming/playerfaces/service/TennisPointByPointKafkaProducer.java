package pro.datawiki.igaming.playerfaces.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.playerfaces.dto.TennisPointByPointEvent;

/**
 * Publishes deep tennis point-by-point events to Kafka topic 'sports.tennis.point-by-point'.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TennisPointByPointKafkaProducer {

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.tennis-point-by-point:sports.tennis.point-by-point}")
    private String topicName;

    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;

    /**
     * Publishes a tennis point-by-point statistical event to Kafka.
     *
     * @param event point-by-point event with MDM mapping
     * @return true if successfully dispatched, false otherwise
     */
    public boolean publish(TennisPointByPointEvent event) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            log.debug("[Tennis Kafka] Kafka publishing is disabled or template not configured. Skipping event for '{}'",
                    event.getPlayerName());
            return false;
        }

        try {
            String key = event.getNormalizedName() != null ? event.getNormalizedName() : event.getPlayerName();
            String payload = objectMapper.writeValueAsString(event);

            log.info("[Tennis Kafka] Streaming point-by-point event to '{}' [key: '{}', surfaces: {}]",
                    topicName, key, event.getSurfaceStats() != null ? event.getSurfaceStats().keySet() : "none");

            kafkaTemplate.send(topicName, key, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[Tennis Kafka] Failed to deliver event to topic '{}' for key '{}': {}",
                            topicName, key, ex.getMessage());
                } else {
                    log.debug("[Tennis Kafka] Successfully delivered event to '{}' partition {} offset {}",
                            topicName,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

            return true;
        } catch (Exception e) {
            log.error("[Tennis Kafka] Error serializing/sending tennis point-by-point event: {}", e.getMessage(), e);
            return false;
        }
    }
}

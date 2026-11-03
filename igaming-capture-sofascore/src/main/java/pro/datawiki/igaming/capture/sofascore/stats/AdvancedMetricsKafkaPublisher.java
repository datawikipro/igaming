package pro.datawiki.igaming.capture.sofascore.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Publisher for emitting verified institutional sports metrics to the central Kafka stream.
 * Target topic: {@code sports.canonical.advanced-metrics}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdvancedMetricsKafkaPublisher {

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topics.advanced-metrics:sports.canonical.advanced-metrics}")
    private String topicName = CanonicalAdvancedMetricsEvent.TOPIC_NAME;

    private final AtomicLong publishedCount = new AtomicLong(0);

    /**
     * Publishes a canonical advanced metrics event to Kafka topic {@code sports.canonical.advanced-metrics}.
     *
     * @param event payload containing computed institutional stats
     * @return true if successfully dispatched or handled
     */
    public boolean publish(CanonicalAdvancedMetricsEvent event) {
        if (event == null) {
            log.warn("[AdvancedMetricsPublisher] Cannot publish null event");
            return false;
        }

        String key = (event.getSport() != null ? event.getSport() : "UNKNOWN") + ":" +
                (event.getMatchExternalId() != null ? event.getMatchExternalId() : event.getEventId());

        try {
            String jsonPayload = objectMapper.writeValueAsString(event);

            if (kafkaTemplate != null) {
                kafkaTemplate.send(topicName, key, jsonPayload).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[AdvancedMetricsPublisher] Failed sending event key='{}' to topic '{}': {}",
                                key, topicName, ex.getMessage());
                    } else {
                        log.info("[AdvancedMetricsPublisher] Dispatched event to topic '{}' (key='{}', sport='{}', league='{}')",
                                topicName, key, event.getSport(), event.getLeagueName());
                    }
                });
            } else {
                log.info("[AdvancedMetricsPublisher] [Local/DryRun] Published event to topic '{}' (key='{}', sport='{}'): {}",
                        topicName, key, event.getSport(), jsonPayload);
            }

            publishedCount.incrementAndGet();
            return true;

        } catch (Exception e) {
            log.error("[AdvancedMetricsPublisher] Serialization/dispatch error for key '{}': {}", key, e.getMessage(), e);
            return false;
        }
    }

    public long getPublishedCount() {
        return publishedCount.get();
    }

    public String getTopicName() {
        return topicName;
    }
}

package pro.datawiki.igaming.source.betfair.service.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.betfair.dto.exchange.SmartMoneyAlert;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes Smart Money alerts to the Kafka topic `odds.smart-money`.
 */
@Service
@Slf4j
public class SmartMoneyKafkaPublisher {

    private final BetfairConfig betfairConfig;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public SmartMoneyKafkaPublisher(
            BetfairConfig betfairConfig,
            ObjectMapper objectMapper,
            @Autowired(required = false) @Qualifier("smartMoneyKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        this.betfairConfig = betfairConfig;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a SmartMoneyAlert as JSON to Kafka topic `odds.smart-money`.
     */
    public boolean publishAlert(SmartMoneyAlert alert) {
        if (alert == null) {
            return false;
        }

        if (!betfairConfig.getSmartMoney().isEnabled()) {
            log.debug("Smart money publishing is disabled by configuration");
            return false;
        }

        String rawTopic = betfairConfig.getSmartMoney().getTopic();
        final String topic = (rawTopic != null && !rawTopic.isBlank()) ? rawTopic : "odds.smart-money";

        try {
            String jsonPayload = objectMapper.writeValueAsString(alert);
            String partitionKey = alert.getEventId() != null ? alert.getEventId() : alert.getSelectionId();

            if (kafkaTemplate != null) {
                CompletableFuture<?> future = kafkaTemplate.send(topic, partitionKey, jsonPayload);
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to deliver smart money alert {} to Kafka topic '{}': {}",
                                alert.getAlertId(), topic, ex.getMessage());
                    } else {
                        log.info("Successfully delivered smart money alert {} to topic '{}' (key: {})",
                                alert.getAlertId(), topic, partitionKey);
                    }
                });
            } else {
                log.info("[MOCK/STANDALONE KAFKA] Smart money alert published to topic '{}': key={}, alertId={}, description={}",
                        topic, partitionKey, alert.getAlertId(), alert.getDescription());
            }

            return true;
        } catch (Exception e) {
            log.error("Error serializing or sending smart money alert {}: {}", alert.getAlertId(), e.getMessage());
            return false;
        }
    }
}

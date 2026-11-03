package pro.datawiki.igaming.capture.fbref.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.CanonicalMatchXgDto;
import pro.datawiki.igaming.dto.CanonicalShotMapDto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for FBref match xG statistics and Understat shot maps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FbrefKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.xg:sports.canonical.xg}")
    private String xgTopic;

    @Value("${app.kafka.topics.shots:sports.canonical.shots}")
    private String shotsTopic;

    @Value("${app.kafka.topics.anomalies:mdm.anomaly.errors}")
    private String anomalyTopic;

    /**
     * Publishes canonical match xG statistics to sports.canonical.xg.
     */
    public CompletableFuture<SendResult<String, Object>> publishCanonicalXg(CanonicalMatchXgDto xgDto) {
        String key = xgDto.getMatchId() != null ? xgDto.getMatchId() : xgDto.getExternalMatchId();
        log.info("Publishing CanonicalMatchXgDto to topic [{}] with key [{}]", xgTopic, key);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(xgTopic, key, xgDto);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published CanonicalMatchXgDto for matchId [{}], partition [{}], offset [{}]",
                        key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish CanonicalMatchXgDto for matchId [{}] to topic [{}]: {}",
                        key, xgTopic, ex.getMessage(), ex);
                publishAnomaly(key, "KAFKA_PUBLISH_XG_ERROR", ex.getMessage());
            }
        });
        return future;
    }

    /**
     * Publishes canonical shot map data to sports.canonical.shots.
     */
    public CompletableFuture<SendResult<String, Object>> publishCanonicalShotMap(CanonicalShotMapDto shotMapDto) {
        String key = shotMapDto.getMatchId() != null ? shotMapDto.getMatchId() : shotMapDto.getExternalMatchId();
        log.info("Publishing CanonicalShotMapDto to topic [{}] with key [{}]", shotsTopic, key);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(shotsTopic, key, shotMapDto);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published CanonicalShotMapDto for matchId [{}], partition [{}], offset [{}]",
                        key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish CanonicalShotMapDto for matchId [{}] to topic [{}]: {}",
                        key, shotsTopic, ex.getMessage(), ex);
                publishAnomaly(key, "KAFKA_PUBLISH_SHOTS_ERROR", ex.getMessage());
            }
        });
        return future;
    }

    /**
     * Publishes error anomaly details to mdm.anomaly.errors.
     */
    public CompletableFuture<SendResult<String, Object>> publishAnomaly(String matchId, String errorType, String details) {
        log.warn("Publishing anomaly to topic [{}] for matchId [{}]: type={}, details={}",
                anomalyTopic, matchId, errorType, details);

        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("match_id", matchId);
        errorPayload.put("error_type", errorType);
        errorPayload.put("details", details);
        errorPayload.put("source", "igaming-capture-fbref");
        errorPayload.put("timestamp_epoch_ms", System.currentTimeMillis());

        return kafkaTemplate.send(anomalyTopic, matchId, errorPayload);
    }
}

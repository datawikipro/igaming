package pro.datawiki.igaming.agent.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.agent.dto.AnomalyEventDto;
import pro.datawiki.igaming.agent.service.AnomalyResolutionService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnomalyKafkaConsumer {

    private final AnomalyResolutionService resolutionService;
    private final ObjectMapper objectMapper;

    private final ExecutorService anomalyExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "anomaly-worker");
        t.setDaemon(true);
        return t;
    });

    @KafkaListener(
            topics = "${app.kafka.topics.anomaly-errors:mdm.anomaly.errors}",
            groupId = "${spring.kafka.consumer.group-id:igaming-anomaly-resolver-agent}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null || record.value().isBlank()) {
            return;
        }

        try {
            AnomalyEventDto event = objectMapper.readValue(record.value(), AnomalyEventDto.class);
            anomalyExecutor.submit(() -> {
                try {
                    resolutionService.processAnomaly(event);
                } catch (Exception ex) {
                    log.error("Error processing anomaly key='{}' async: {}",
                            event.getRawKey(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Failed to parse anomaly event from Kafka record key='{}': {}",
                    record.key(), e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down anomaly worker executor...");
        anomalyExecutor.shutdown();
        try {
            if (!anomalyExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                anomalyExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            anomalyExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

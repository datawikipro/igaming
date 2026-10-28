package pro.datawiki.igaming.agent.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.agent.dto.AnomalyEventDto;
import pro.datawiki.igaming.agent.service.AnomalyResolutionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnomalyKafkaConsumer {

    private final AnomalyResolutionService resolutionService;
    private final ObjectMapper objectMapper;

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
            resolutionService.processAnomaly(event);
        } catch (Exception e) {
            log.error("Failed to parse anomaly event from Kafka record key='{}': {}",
                    record.key(), e.getMessage());
        }
    }
}

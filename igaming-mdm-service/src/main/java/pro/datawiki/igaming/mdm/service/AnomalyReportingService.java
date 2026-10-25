package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.mdm.dto.UnmappedAnomalyEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnomalyReportingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.anomaly-errors:mdm.anomaly.errors}")
    private String anomalyTopic;

    public void reportAnomaly(UnmappedAnomalyEvent event) {
        log.warn("🚨 [MDM Stage-1 Anomaly] Unmapped entity detected: system={}, type={}, rawKey='{}', rawName='{}'",
                event.getExternalSystemCode(), event.getEntityType(), event.getRawKey(), event.getRawName());
        try {
            String key = event.getExternalSystemCode() + ":" + event.getEntityType() + ":" + event.getRawKey();
            kafkaTemplate.send(anomalyTopic, key, event);
        } catch (Exception e) {
            log.error("Failed to publish anomaly to Kafka topic '{}': {}", anomalyTopic, e.getMessage(), e);
        }
    }

    public void reportLeagueMismatch(
            String externalSystemCode,
            String violationType,
            Long tournamentId,
            String tournamentName,
            Long teamId,
            String teamName,
            String details
    ) {
        log.warn("🚨 [MDM League Anomaly] [{}]: {}: tournament='{}' (#{}) vs team='{}' (#{}) - {}",
                externalSystemCode, violationType, tournamentName, tournamentId, teamName, teamId, details);
        UnmappedAnomalyEvent event = UnmappedAnomalyEvent.builder()
                .externalSystemCode(externalSystemCode)
                .entityType(violationType)
                .rawKey(teamId != null ? teamId.toString() : null)
                .rawName(teamName)
                .rawParentKey(tournamentId != null ? tournamentId.toString() : null)
                .rawCategory(tournamentName)
                .details(details)
                .build();
        reportAnomaly(event);
    }
}

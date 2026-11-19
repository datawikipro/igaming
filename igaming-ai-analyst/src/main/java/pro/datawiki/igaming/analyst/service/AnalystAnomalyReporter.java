package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.UnmappedAnomalyEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Anomaly and Error Reporting Service for AI Quantitative Analyst.
 * Publishes unmapped teams, zero-history newcomer violations, and schedule collisions
 * to logging (ERROR), Kafka topic 'mdm.anomaly.errors', and MDM REST API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalystAnomalyReporter {

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.anomaly-errors:mdm.anomaly.errors}")
    private String anomalyTopic;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    private String mdmUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    /**
     * Reports an unmapped team or newcomer with 0 historical matches.
     * Logs ERROR, emits to Kafka topic 'mdm.anomaly.errors', and notifies MDM.
     */
    public void reportUnmappedTeam(Long matchId, String teamName, String leagueName, String sportName, String sportSlug) {
        log.error("🚨 [UNMAPPED_TEAM_ERROR] Команда '{}' (лига: '{}', спорт: '{}') не мапится! В истории 0 матчей (99% ошибка маппинга / новичок). Срочно поправить! (matchId={})",
                teamName, leagueName, sportName, matchId);

        String details = String.format("Команда '%s' (лига: '%s', спорт: '%s') не мапится (0 матчей в истории, 99%% ошибка маппинга / новичок). Срочно поправить! Match ID: %s",
                teamName, leagueName, sportName, matchId != null ? matchId : "N/A");

        UnmappedAnomalyEvent event = UnmappedAnomalyEvent.builder()
                .externalSystemCode("AI_ANALYST")
                .entityType("UNMAPPED_TEAM")
                .rawKey(teamName)
                .rawName(teamName)
                .rawCategory(leagueName)
                .rawParentKey(sportSlug != null ? sportSlug : sportName)
                .detectedAt(OffsetDateTime.now())
                .details(details)
                .build();

        dispatchAnomaly(event, "AI_ANALYST:UNMAPPED_TEAM:" + teamName);
    }

    /**
     * Reports a physical reality schedule collision to Kafka and MDM.
     */
    public void reportScheduleCollision(
            Long matchId1, Long matchId2, String conflictTeam, String sportSlug,
            String t1, String t2, String league1, String t1rec2, String t2rec2, String league2
    ) {
        try {
            Map<String, Object> detailsMap = new HashMap<>();
            detailsMap.put("match1_id", matchId1);
            detailsMap.put("match1_teams", t1 + " vs " + t2);
            detailsMap.put("match1_league", league1);
            detailsMap.put("match2_id", matchId2);
            detailsMap.put("match2_teams", t1rec2 + " vs " + t2rec2);
            detailsMap.put("match2_league", league2);
            detailsMap.put("conflict_team", conflictTeam);
            detailsMap.put("sport", sportSlug);

            UnmappedAnomalyEvent event = UnmappedAnomalyEvent.builder()
                    .externalSystemCode("AI_ANALYST")
                    .entityType("SCHEDULE_COLLISION")
                    .rawKey(matchId1 + ":" + matchId2)
                    .rawName(conflictTeam)
                    .rawCategory(league1 + " vs " + league2)
                    .rawParentKey(sportSlug)
                    .detectedAt(OffsetDateTime.now())
                    .details(objectMapper.writeValueAsString(detailsMap))
                    .build();

            dispatchAnomaly(event, "AI_ANALYST:SCHEDULE_COLLISION:" + matchId1 + ":" + matchId2);
        } catch (Exception e) {
            log.warn("Failed to format schedule collision anomaly: {}", e.getMessage());
        }
    }

    private void dispatchAnomaly(UnmappedAnomalyEvent event, String messageKey) {
        // 1. Direct Kafka publish to mdm.anomaly.errors
        if (kafkaTemplate != null) {
            try {
                String json = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(anomalyTopic, messageKey, json);
                log.info("📡 [Kafka-Anomaly] Published {} anomaly to topic '{}': key='{}'",
                        event.getEntityType(), anomalyTopic, messageKey);
            } catch (Exception e) {
                log.warn("Failed to publish anomaly to Kafka topic '{}': {}", anomalyTopic, e.getMessage());
            }
        } else {
            log.debug("KafkaTemplate is not available. Skipping direct Kafka publish for {}", messageKey);
        }

        // 2. MDM REST endpoint dual-dispatch
        try {
            String url = mdmUrl + "/api/v1/mdm/report-unmapped";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event)))
                    .timeout(Duration.ofSeconds(3))
                    .build();

            httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
            log.info("📢 [MDM-REST] Dispatched anomaly to MDM endpoint: {} for '{}'", url, event.getRawName());
        } catch (Exception e) {
            log.warn("Failed to dispatch anomaly to MDM REST endpoint: {}", e.getMessage());
        }
    }
}

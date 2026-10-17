package pro.datawiki.igaming.capture.sofascore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.capture.sofascore.domain.SofaRawEventEntity;
import pro.datawiki.igaming.capture.sofascore.repository.SofaRawEventRepository;
import pro.datawiki.igaming.dto.raw.SofaRawEventDto;

import java.time.Instant;
import java.util.Optional;

/**
 * Ingestion service responsible for dual-storage of raw SofaScore events:
 * 1. Persistent relational storage in PostgreSQL (sofascore_raw_events) for audit and backfills.
 * 2. Streaming publication to Kafka (sports.raw.sofascore) for reactive downstream consumers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SofaRawEventService {

    private final SofaRawEventRepository repository;
    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    @Value("${app.kafka.topics.sofa-raw:igaming.sofascore.raw-events}")
    private String sofaRawTopic;

    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;

    /**
     * Persists the raw event payload into PostgreSQL and broadcasts to Kafka.
     */
    @Transactional
    public void recordRawEvent(SofaRawEventDto dto) {
        if (dto == null || dto.getEventId() == null) {
            return;
        }

        try {
            // 1. Persistent DB Storage (Upsert)
            Optional<SofaRawEventEntity> existing = repository.findById(dto.getEventId());
            SofaRawEventEntity entity = existing.orElseGet(() -> SofaRawEventEntity.builder()
                    .eventId(dto.getEventId())
                    .build());

            String sport = dto.getSport();
            if (sport == null || sport.isBlank()) {
                sport = entity.getSport() != null ? entity.getSport() : "football";
            }
            entity.setSport(sport);
            if (dto.getTournamentName() != null) entity.setTournament(dto.getTournamentName());
            if (dto.getSeason() != null) entity.setSeason(dto.getSeason());
            if (dto.getMatchDate() != null) entity.setMatchDate(dto.getMatchDate());
            if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
            if (dto.getHomeTeam() != null) entity.setHomeTeam(dto.getHomeTeam());
            if (dto.getAwayTeam() != null) entity.setAwayTeam(dto.getAwayTeam());
            if (dto.getHomeScore() != null) entity.setHomeScore(dto.getHomeScore());
            if (dto.getAwayScore() != null) entity.setAwayScore(dto.getAwayScore());

            if (dto.getEventJson() != null && !dto.getEventJson().isBlank()) {
                entity.setEventJson(dto.getEventJson());
            }
            if (dto.getLineupsJson() != null && !dto.getLineupsJson().isBlank()) {
                entity.setLineupsJson(dto.getLineupsJson());
            }
            if (dto.getH2hJson() != null && !dto.getH2hJson().isBlank()) {
                entity.setH2hJson(dto.getH2hJson());
            }
            if (dto.getStreaksJson() != null && !dto.getStreaksJson().isBlank()) {
                entity.setStreaksJson(dto.getStreaksJson());
            }

            entity.setCapturedAt(Instant.now());
            repository.save(entity);
            log.info("[Raw Store] Persisted raw Sofa event {} ({} vs {}, sport: {}) to DB",
                    dto.getEventId(), dto.getHomeTeam(), dto.getAwayTeam(), dto.getSport());
        } catch (Exception e) {
            log.error("[Raw Store] Failed to save raw Sofa event {} to DB: {}", dto.getEventId(), e.getMessage());
        }

        // 2. Kafka Broadcast
        if (kafkaEnabled && kafkaTemplate.isPresent()) {
            try {
                String key = String.valueOf(dto.getEventId());
                kafkaTemplate.get().send(sofaRawTopic, key, dto);
                log.debug("[Raw Kafka] Emitted event {} to topic {}", dto.getEventId(), sofaRawTopic);
            } catch (Exception e) {
                log.warn("[Raw Kafka] Failed to emit event {} to Kafka topic {}: {}",
                        dto.getEventId(), sofaRawTopic, e.getMessage());
            }
        }
    }
}

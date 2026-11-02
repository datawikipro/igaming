package pro.datawiki.igaming.capture.esports.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.capture.esports.crawler.HltvCrawlerService;
import pro.datawiki.igaming.capture.esports.crawler.OpenDotaCrawlerService;
import pro.datawiki.igaming.capture.esports.crawler.VlrCrawlerService;
import pro.datawiki.igaming.capture.esports.domain.EsportsRawEventEntity;
import pro.datawiki.igaming.capture.esports.domain.HltvRawMatchEntity;
import pro.datawiki.igaming.capture.esports.domain.OpenDotaRawMatchEntity;
import pro.datawiki.igaming.capture.esports.repository.EsportsRawEventRepository;
import pro.datawiki.igaming.capture.esports.repository.HltvRawMatchRepository;
import pro.datawiki.igaming.capture.esports.repository.OpenDotaRawMatchRepository;

import java.time.Instant;
import java.util.Optional;

/**
 * Core ingestion service for raw esports events.
 * Manages dual storage into PostgreSQL:
 * 1. Unified 'esports_raw_events' table with MDM mapping state ('MAPPED' / 'UNMAPPED').
 * 2. Dedicated 'hltv_raw_matches' and 'opendota_raw_matches' tables for deep retroactive ML analysis.
 * Broadcasts events to Kafka topic 'igaming.esports.raw-events'.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EsportsRawEventService {

    private final EsportsRawEventRepository esportsRawEventRepository;
    private final HltvRawMatchRepository hltvRawMatchRepository;
    private final OpenDotaRawMatchRepository openDotaRawMatchRepository;
    private final EsportsMdmService esportsMdmService;
    private final ObjectMapper objectMapper;
    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    @Value("${app.kafka.topics.esports-raw:igaming.esports.raw-events}")
    private String esportsRawTopic;

    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;

    /**
     * Ingests and persists a CS2 match from HLTV crawler.
     */
    @Transactional
    public EsportsRawEventEntity ingestHltvMatch(HltvCrawlerService.ParsedCs2Match parsed) {
        String eventId = "hltv-" + parsed.matchId();
        log.info("📥 [INGEST-CS2] Processing HLTV match: {} ({} vs {})", eventId, parsed.team1(), parsed.team2());

        try {
            String payloadJson = objectMapper.writeValueAsString(parsed.payload());
            Long matchDate = Instant.now().toEpochMilli();

            // 1. Resolve MDM mapping
            EsportsMdmService.MatchResolutionResult mdmResult = esportsMdmService.resolveMatch(
                    "CS2", parsed.tournamentName(), parsed.team1(), parsed.team2(), matchDate);

            // 2. Upsert unified EsportsRawEventEntity
            Optional<EsportsRawEventEntity> existing = esportsRawEventRepository.findById(eventId);
            EsportsRawEventEntity entity = existing.orElseGet(() -> EsportsRawEventEntity.builder()
                    .eventId(eventId)
                    .createdAt(Instant.now())
                    .build());

            entity.setGame("CS2");
            entity.setTournamentRaw(parsed.tournamentName());
            entity.setTeam1Raw(parsed.team1());
            entity.setTeam2Raw(parsed.team2());
            entity.setMatchDate(matchDate);
            entity.setStatus(parsed.payload().getMatchStatus());
            entity.setRawPayloadJson(payloadJson);
            entity.setUpdatedAt(Instant.now());

            if (mdmResult.isMapped()) {
                entity.setMdmStatus("MAPPED");
                entity.setMdmMatchId(mdmResult.getMatchId());
                entity.setMdmHomeTeamId(mdmResult.getHomeTeamId());
                entity.setMdmAwayTeamId(mdmResult.getAwayTeamId());
                if (parsed.payload() != null && parsed.payload().getTeam1Score() != null && parsed.payload().getTeam2Score() != null) {
                    esportsMdmService.syncMatchScore(
                            mdmResult.getMatchId(),
                            parsed.payload().getMatchStatus(),
                            parsed.payload().getTeam1Score(),
                            parsed.payload().getTeam2Score()
                    );
                }
            } else {
                entity.setMdmStatus("UNMAPPED");
                entity.setMdmMatchId(null);
                entity.setMdmHomeTeamId(mdmResult.getHomeTeamId());
                entity.setMdmAwayTeamId(mdmResult.getAwayTeamId());
            }

            EsportsRawEventEntity saved = esportsRawEventRepository.save(entity);

            // 3. Persist dedicated HltvRawMatchEntity for historical retro-analysis
            HltvRawMatchEntity hltvEntity = HltvRawMatchEntity.builder()
                    .matchId(parsed.matchId())
                    .tournamentTier(parsed.tournamentTier())
                    .tournamentName(parsed.tournamentName())
                    .team1(parsed.team1())
                    .team2(parsed.team2())
                    .matchDate(matchDate)
                    .vetoJson(objectMapper.writeValueAsString(parsed.payload().getVetoActions()))
                    .mapsJson(objectMapper.writeValueAsString(parsed.payload().getMaps()))
                    .rostersJson(objectMapper.writeValueAsString(parsed.payload().getPlayers()))
                    .rawJson(payloadJson)
                    .build();
            hltvRawMatchRepository.save(hltvEntity);

            // 4. Kafka publication
            emitToKafka(eventId, saved);
            return saved;
        } catch (Exception e) {
            log.error("❌ [INGEST-CS2] Error ingesting HLTV match {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to ingest HLTV match", e);
        }
    }

    /**
     * Ingests and persists a Dota 2 match from OpenDota crawler.
     */
    @Transactional
    public EsportsRawEventEntity ingestOpenDotaMatch(OpenDotaCrawlerService.ParsedDota2Match parsed) {
        String eventId = "opendota-" + parsed.matchId();
        log.info("📥 [INGEST-DOTA2] Processing OpenDota match: {} ({} vs {})", eventId, parsed.radiantTeam(), parsed.direTeam());

        try {
            String payloadJson = objectMapper.writeValueAsString(parsed.payload());
            Long matchDate = Instant.now().toEpochMilli();

            // 1. Resolve MDM mapping
            EsportsMdmService.MatchResolutionResult mdmResult = esportsMdmService.resolveMatch(
                    "DOTA2", parsed.leagueName(), parsed.radiantTeam(), parsed.direTeam(), matchDate);

            // 2. Upsert unified EsportsRawEventEntity
            Optional<EsportsRawEventEntity> existing = esportsRawEventRepository.findById(eventId);
            EsportsRawEventEntity entity = existing.orElseGet(() -> EsportsRawEventEntity.builder()
                    .eventId(eventId)
                    .createdAt(Instant.now())
                    .build());

            entity.setGame("DOTA2");
            entity.setTournamentRaw(parsed.leagueName());
            entity.setTeam1Raw(parsed.radiantTeam());
            entity.setTeam2Raw(parsed.direTeam());
            entity.setMatchDate(matchDate);
            entity.setStatus("FINISHED");
            entity.setRawPayloadJson(payloadJson);
            entity.setUpdatedAt(Instant.now());

            if (mdmResult.isMapped()) {
                entity.setMdmStatus("MAPPED");
                entity.setMdmMatchId(mdmResult.getMatchId());
                entity.setMdmHomeTeamId(mdmResult.getHomeTeamId());
                entity.setMdmAwayTeamId(mdmResult.getAwayTeamId());
                if (parsed.payload() != null && parsed.payload().getRadiantScore() != null && parsed.payload().getDireScore() != null) {
                    esportsMdmService.syncMatchScore(
                            mdmResult.getMatchId(),
                            "FINISHED",
                            parsed.payload().getRadiantScore(),
                            parsed.payload().getDireScore()
                    );
                }
            } else {
                entity.setMdmStatus("UNMAPPED");
                entity.setMdmMatchId(null);
                entity.setMdmHomeTeamId(mdmResult.getHomeTeamId());
                entity.setMdmAwayTeamId(mdmResult.getAwayTeamId());
            }

            EsportsRawEventEntity saved = esportsRawEventRepository.save(entity);

            // 3. Persist dedicated OpenDotaRawMatchEntity for historical retro-analysis
            OpenDotaRawMatchEntity dotaEntity = OpenDotaRawMatchEntity.builder()
                    .matchId(parsed.matchId())
                    .leagueId(parsed.payload().getLeagueId())
                    .leagueName(parsed.leagueName())
                    .radiantTeam(parsed.radiantTeam())
                    .direTeam(parsed.direTeam())
                    .radiantWin(parsed.payload().getRadiantWin())
                    .durationSeconds(parsed.payload().getDurationSeconds())
                    .draftJson(objectMapper.writeValueAsString(parsed.payload().getDrafts()))
                    .networthAdvJson(objectMapper.writeValueAsString(parsed.payload().getRadiantGoldAdvTimeline()))
                    .xpAdvJson(objectMapper.writeValueAsString(parsed.payload().getRadiantXpAdvTimeline()))
                    .itemTimingsJson(objectMapper.writeValueAsString(parsed.payload().getItemTimings()))
                    .rawJson(payloadJson)
                    .matchDate(matchDate)
                    .build();
            openDotaRawMatchRepository.save(dotaEntity);

            // 4. Kafka publication
            emitToKafka(eventId, saved);
            return saved;
        } catch (Exception e) {
            log.error("❌ [INGEST-DOTA2] Error ingesting OpenDota match {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to ingest OpenDota match", e);
        }
    }

    /**
     * Ingests and persists a Valorant match from VLR crawler.
     */
    @Transactional
    public EsportsRawEventEntity ingestValorantMatch(VlrCrawlerService.ParsedValorantMatch parsed) {
        String eventId = parsed.matchId();
        log.info("📥 [INGEST-VALORANT] Processing Valorant match: {} ({} vs {})", eventId, parsed.team1(), parsed.team2());

        try {
            String payloadJson = objectMapper.writeValueAsString(parsed.payload());
            Long matchDate = Instant.now().toEpochMilli();

            // 1. Resolve MDM mapping
            EsportsMdmService.MatchResolutionResult mdmResult = esportsMdmService.resolveMatch(
                    "VALORANT", parsed.tournamentName(), parsed.team1(), parsed.team2(), matchDate);

            // 2. Upsert unified EsportsRawEventEntity
            Optional<EsportsRawEventEntity> existing = esportsRawEventRepository.findById(eventId);
            EsportsRawEventEntity entity = existing.orElseGet(() -> EsportsRawEventEntity.builder()
                    .eventId(eventId)
                    .createdAt(Instant.now())
                    .build());

            entity.setGame("VALORANT");
            entity.setTournamentRaw(parsed.tournamentName());
            entity.setTeam1Raw(parsed.team1());
            entity.setTeam2Raw(parsed.team2());
            entity.setMatchDate(matchDate);
            entity.setStatus(parsed.payload().getStatus());
            entity.setRawPayloadJson(payloadJson);
            entity.setUpdatedAt(Instant.now());

            if (mdmResult.isMapped()) {
                entity.setMdmStatus("MAPPED");
                entity.setMdmMatchId(mdmResult.getMatchId());
                entity.setMdmHomeTeamId(mdmResult.getHomeTeamId());
                entity.setMdmAwayTeamId(mdmResult.getAwayTeamId());
            } else {
                entity.setMdmStatus("UNMAPPED");
                entity.setMdmMatchId(null);
                entity.setMdmHomeTeamId(mdmResult.getHomeTeamId());
                entity.setMdmAwayTeamId(mdmResult.getAwayTeamId());
            }

            EsportsRawEventEntity saved = esportsRawEventRepository.save(entity);

            // 3. Kafka publication
            emitToKafka(eventId, saved);
            return saved;
        } catch (Exception e) {
            log.error("❌ [INGEST-VALORANT] Error ingesting Valorant match {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to ingest Valorant match", e);
        }
    }

    private void emitToKafka(String key, Object payload) {
        if (kafkaEnabled && kafkaTemplate.isPresent()) {
            try {
                kafkaTemplate.get().send(esportsRawTopic, key, payload);
                log.debug("📡 [Raw Kafka] Published esports raw event to topic {}: {}", esportsRawTopic, key);
            } catch (Exception e) {
                log.warn("Failed to publish esports raw event to Kafka: {}", e.getMessage());
            }
        }
    }
}

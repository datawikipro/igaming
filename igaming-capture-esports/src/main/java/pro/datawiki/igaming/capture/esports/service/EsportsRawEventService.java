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
import pro.datawiki.igaming.dto.lineup.CanonicalLineupEvent;
import pro.datawiki.igaming.dto.lineup.Cs2MatchLineupDto;
import pro.datawiki.igaming.dto.lineup.Dota2MatchLineupDto;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.dto.live.MatchStatus;
import pro.datawiki.igaming.dto.sport.Sports;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core ingestion service for raw esports events.
 * Manages dual storage into PostgreSQL:
 * 1. Unified 'esports_raw_events' table with MDM mapping state ('MAPPED' / 'UNMAPPED').
 * 2. Dedicated 'hltv_raw_matches' and 'opendota_raw_matches' tables for deep retroactive ML analysis.
 * Broadcasts canonical scores to 'sports.canonical.scores' and sport-specific lineups:
 * - CS2 to 'sports.cs2.lineups'
 * - Dota 2 to 'sports.dota2.lineups'
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

    @Value("${app.kafka.topics.canonical-scores:sports.canonical.scores}")
    private String canonicalScoresTopic = "sports.canonical.scores";

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
            entity.setStatus(parsed.payload() != null ? parsed.payload().getMatchStatus() : "FINISHED");
            entity.setRawPayloadJson(payloadJson);
            entity.setUpdatedAt(Instant.now());

            Long mdmMatchId = null;
            if (mdmResult.isMapped()) {
                mdmMatchId = mdmResult.getMatchId();
                entity.setMdmStatus("MAPPED");
                entity.setMdmMatchId(mdmMatchId);
                entity.setMdmHomeTeamId(mdmResult.getHomeTeamId());
                entity.setMdmAwayTeamId(mdmResult.getAwayTeamId());
                if (parsed.payload() != null && parsed.payload().getTeam1Score() != null && parsed.payload().getTeam2Score() != null) {
                    esportsMdmService.syncMatchScore(
                            mdmMatchId,
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

            // 4. Kafka publication: raw, canonical score, CS2 map veto & lineup
            emitToKafka(eventId, saved);

            if (parsed.payload() != null) {
                if (parsed.payload().getTeam1Score() != null && parsed.payload().getTeam2Score() != null) {
                    MatchStatus status = "FINISHED".equalsIgnoreCase(parsed.payload().getMatchStatus())
                            ? MatchStatus.FINISHED
                            : MatchStatus.LIVE;
                    CanonicalLiveScoreDto scoreDto = CanonicalLiveScoreDto.builder()
                            .matchId(mdmMatchId)
                            .sourceCode("hltv")
                            .externalEventId(String.valueOf(parsed.matchId()))
                            .sport(Sports.CS2.getSlug())
                            .status(status)
                            .score1(parsed.payload().getTeam1Score())
                            .score2(parsed.payload().getTeam2Score())
                            .regularScore1(parsed.payload().getTeam1Score())
                            .regularScore2(parsed.payload().getTeam2Score())
                            .displayTimer(status == MatchStatus.FINISHED ? "FT" : "")
                            .timestamp(matchDate)
                            .build();
                    emitCanonicalScore(scoreDto);
                }

                emitCs2LineupAndVeto(parsed, mdmMatchId, matchDate);
            }

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

            Long mdmMatchId = null;
            if (mdmResult.isMapped()) {
                mdmMatchId = mdmResult.getMatchId();
                entity.setMdmStatus("MAPPED");
                entity.setMdmMatchId(mdmMatchId);
                entity.setMdmHomeTeamId(mdmResult.getHomeTeamId());
                entity.setMdmAwayTeamId(mdmResult.getAwayTeamId());
                if (parsed.payload() != null && parsed.payload().getRadiantScore() != null && parsed.payload().getDireScore() != null) {
                    esportsMdmService.syncMatchScore(
                            mdmMatchId,
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

            // 4. Kafka publication: raw, canonical score, Dota 2 draft & lineup
            emitToKafka(eventId, saved);

            if (parsed.payload() != null) {
                Integer s1 = (parsed.payload().getRadiantWin() != null && parsed.payload().getRadiantWin()) ? 1 : 0;
                Integer s2 = (parsed.payload().getRadiantWin() != null && !parsed.payload().getRadiantWin()) ? 1 : 0;
                Integer radiantScore = parsed.payload().getRadiantScore() != null ? parsed.payload().getRadiantScore() : s1;
                Integer direScore = parsed.payload().getDireScore() != null ? parsed.payload().getDireScore() : s2;

                CanonicalLiveScoreDto scoreDto = CanonicalLiveScoreDto.builder()
                        .matchId(mdmMatchId)
                        .sourceCode("opendota")
                        .externalEventId(String.valueOf(parsed.matchId()))
                        .sport(Sports.DOTA2.getSlug())
                        .status(MatchStatus.FINISHED)
                        .score1(radiantScore)
                        .score2(direScore)
                        .regularScore1(radiantScore)
                        .regularScore2(direScore)
                        .elapsedSeconds(parsed.payload().getDurationSeconds())
                        .displayTimer("FT")
                        .timestamp(matchDate)
                        .build();
                emitCanonicalScore(scoreDto);

                emitDota2DraftAndLineup(parsed, mdmMatchId, matchDate);
            }

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
            entity.setStatus(parsed.payload() != null ? parsed.payload().getStatus() : "FINISHED");
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

    private void emitCanonicalScore(CanonicalLiveScoreDto score) {
        if (kafkaEnabled && kafkaTemplate.isPresent() && score != null) {
            try {
                String key = score.getMatchId() != null ? String.valueOf(score.getMatchId()) : score.getExternalEventId();
                kafkaTemplate.get().send(canonicalScoresTopic, key, score);
                log.info("📡 [Score-Kafka] Published score to topic {}: match #{} ({}:{})",
                        canonicalScoresTopic, score.getMatchId(), score.getScore1(), score.getScore2());
            } catch (Exception e) {
                log.warn("Failed to publish score to Kafka topic {}: {}", canonicalScoresTopic, e.getMessage());
            }
        }
    }

    private void emitCanonicalLineup(CanonicalLineupEvent lineup) {
        if (kafkaEnabled && kafkaTemplate.isPresent() && lineup != null) {
            try {
                String topic = lineup.getKafkaTopic();
                String key = lineup.getMatchId() != null ? String.valueOf(lineup.getMatchId()) : lineup.getExternalEventId();
                kafkaTemplate.get().send(topic, key, lineup);
                log.info("📡 [Lineup-Kafka] Published {} to topic {}: match #{} (ext: {})",
                        lineup.getClass().getSimpleName(), topic, lineup.getMatchId(), lineup.getExternalEventId());
            } catch (Exception e) {
                log.warn("Failed to publish lineup to Kafka topic: {}", e.getMessage());
            }
        }
    }

    private void emitCs2LineupAndVeto(HltvCrawlerService.ParsedCs2Match parsed, Long matchId, Long timestamp) {
        try {
            List<Cs2MatchLineupDto.Cs2MapVetoDto> vetos = new ArrayList<>();
            if (parsed.payload().getVetoActions() != null) {
                for (var v : parsed.payload().getVetoActions()) {
                    vetos.add(Cs2MatchLineupDto.Cs2MapVetoDto.builder()
                            .team(v.getTeam())
                            .action(v.getAction())
                            .mapName(v.getMapName())
                            .order(v.getOrder())
                            .build());
                }
            }

            List<Cs2MatchLineupDto.Cs2PlayerSlotDto> team1Players = new ArrayList<>();
            List<Cs2MatchLineupDto.Cs2PlayerSlotDto> team2Players = new ArrayList<>();
            if (parsed.payload().getPlayers() != null) {
                for (var p : parsed.payload().getPlayers()) {
                    Cs2MatchLineupDto.Cs2PlayerSlotDto slot = Cs2MatchLineupDto.Cs2PlayerSlotDto.builder()
                            .nickname(p.getNickname())
                            .realName(p.getRealName())
                            .rating(p.getRating20())
                            .adr(p.getAdr())
                            .kast(p.getKastPct())
                            .kills(p.getKills() != null ? p.getKills() : 0)
                            .deaths(p.getDeaths() != null ? p.getDeaths() : 0)
                            .assists(p.getAssists() != null ? p.getAssists() : 0)
                            .build();
                    if ("team1".equalsIgnoreCase(p.getTeam()) || (parsed.team1() != null && parsed.team1().equalsIgnoreCase(p.getTeam()))) {
                        team1Players.add(slot);
                    } else {
                        team2Players.add(slot);
                    }
                }
            }

            Cs2MatchLineupDto cs2Lineup = Cs2MatchLineupDto.builder()
                    .matchId(matchId)
                    .sourceOrigin("hltv")
                    .externalEventId(String.valueOf(parsed.matchId()))
                    .confirmed(true)
                    .timestamp(timestamp)
                    .vetoActions(vetos)
                    .team1Players(team1Players)
                    .team2Players(team2Players)
                    .build();

            emitCanonicalLineup(cs2Lineup);
        } catch (Exception e) {
            log.warn("Failed emitting CS2 lineup for match {}: {}", parsed.matchId(), e.getMessage());
        }
    }

    private void emitDota2DraftAndLineup(OpenDotaCrawlerService.ParsedDota2Match parsed, Long matchId, Long timestamp) {
        try {
            List<Dota2MatchLineupDto.Dota2DraftDto> drafts = new ArrayList<>();
            if (parsed.payload().getDrafts() != null) {
                for (var d : parsed.payload().getDrafts()) {
                    drafts.add(Dota2MatchLineupDto.Dota2DraftDto.builder()
                            .order(d.getOrder())
                            .isPick(d.getIsPick() != null ? d.getIsPick() : false)
                            .isRadiant(d.getIsRadiant() != null ? d.getIsRadiant() : false)
                            .heroId(d.getHeroId())
                            .heroName(d.getHeroName())
                            .build());
                }
            }

            List<Dota2MatchLineupDto.Dota2PlayerSlotDto> radiantPlayers = new ArrayList<>();
            List<Dota2MatchLineupDto.Dota2PlayerSlotDto> direPlayers = new ArrayList<>();
            if (parsed.payload().getPlayers() != null) {
                for (var p : parsed.payload().getPlayers()) {
                    Dota2MatchLineupDto.Dota2PlayerSlotDto slot = Dota2MatchLineupDto.Dota2PlayerSlotDto.builder()
                            .nickname(p.getNickname())
                            .heroName(p.getHeroName())
                            .kills(p.getKills() != null ? p.getKills() : 0)
                            .deaths(p.getDeaths() != null ? p.getDeaths() : 0)
                            .assists(p.getAssists() != null ? p.getAssists() : 0)
                            .build();
                    if ("radiant".equalsIgnoreCase(p.getTeam()) || (parsed.radiantTeam() != null && parsed.radiantTeam().equalsIgnoreCase(p.getTeam()))) {
                        radiantPlayers.add(slot);
                    } else {
                        direPlayers.add(slot);
                    }
                }
            }

            Dota2MatchLineupDto dotaLineup = Dota2MatchLineupDto.builder()
                    .matchId(matchId)
                    .sourceOrigin("opendota")
                    .externalEventId(String.valueOf(parsed.matchId()))
                    .confirmed(true)
                    .timestamp(timestamp)
                    .drafts(drafts)
                    .radiantPlayers(radiantPlayers)
                    .direPlayers(direPlayers)
                    .build();

            emitCanonicalLineup(dotaLineup);
        } catch (Exception e) {
            log.warn("Failed emitting Dota 2 lineup for match {}: {}", parsed.matchId(), e.getMessage());
        }
    }
}

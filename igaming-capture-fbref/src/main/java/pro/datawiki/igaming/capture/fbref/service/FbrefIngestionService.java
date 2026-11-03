package pro.datawiki.igaming.capture.fbref.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.capture.fbref.entity.FbrefRawReport;
import pro.datawiki.igaming.capture.fbref.entity.UnderstatRawShot;
import pro.datawiki.igaming.capture.fbref.kafka.FbrefKafkaProducer;
import pro.datawiki.igaming.capture.fbref.repository.FbrefRawReportRepository;
import pro.datawiki.igaming.capture.fbref.repository.UnderstatRawShotRepository;
import pro.datawiki.igaming.dto.CanonicalMatchXgDto;
import pro.datawiki.igaming.dto.CanonicalShotMapDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FbrefIngestionService {

    private final FbrefRawReportRepository rawReportRepository;
    private final UnderstatRawShotRepository rawShotRepository;
    private final FbrefKafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Ingests, persists raw FBref report, and streams to Kafka topic sports.canonical.xg.
     */
    @Transactional
    public FbrefRawReport processAndPublishXgReport(CanonicalMatchXgDto dto) {
        String matchId = dto.getMatchId() != null ? dto.getMatchId() : dto.getExternalMatchId();
        log.info("Processing FBref xG report for matchId [{}]", matchId);

        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.warn("Could not serialize DTO to JSON payload: {}", e.getMessage());
        }

        FbrefRawReport report = FbrefRawReport.builder()
                .matchId(matchId)
                .externalMatchId(dto.getExternalMatchId())
                .matchUrl(dto.getMatchUrl())
                .competition(dto.getCompetition())
                .season(dto.getSeason())
                .homeTeam(dto.getHomeTeam())
                .awayTeam(dto.getAwayTeam())
                .homeScore(dto.getHomeScore())
                .awayScore(dto.getAwayScore())
                .homeXg(dto.getHomeXg())
                .awayXg(dto.getAwayXg())
                .homeXag(dto.getHomeXag())
                .awayXag(dto.getAwayXag())
                .homeSca(dto.getHomeSca())
                .awaySca(dto.getAwaySca())
                .homeGca(dto.getHomeGca())
                .awayGca(dto.getAwayGca())
                .homeNpxg(dto.getHomeNpxg())
                .awayNpxg(dto.getAwayNpxg())
                .status("PROCESSED")
                .payload(payloadJson)
                .createdAt(Instant.now())
                .build();

        FbrefRawReport saved = rawReportRepository.save(report);

        // Stream to Kafka
        kafkaProducer.publishCanonicalXg(dto);

        return saved;
    }

    /**
     * Ingests, persists raw Understat shots, and streams to Kafka topic sports.canonical.shots.
     */
    @Transactional
    public List<UnderstatRawShot> processAndPublishShotMap(CanonicalShotMapDto dto) {
        String matchId = dto.getMatchId() != null ? dto.getMatchId() : dto.getExternalMatchId();
        log.info("Processing Understat shot map for matchId [{}] with [{}] shots",
                matchId, dto.getShots() != null ? dto.getShots().size() : 0);

        List<UnderstatRawShot> entityList = new ArrayList<>();
        if (dto.getShots() != null) {
            for (CanonicalShotMapDto.CanonicalShotDetailDto s : dto.getShots()) {
                String shotJson = null;
                try {
                    shotJson = objectMapper.writeValueAsString(s);
                } catch (Exception e) {
                    log.warn("Could not serialize shot to JSON: {}", e.getMessage());
                }

                UnderstatRawShot shot = UnderstatRawShot.builder()
                        .matchId(matchId)
                        .externalShotId(s.getShotId())
                        .minute(s.getMinute())
                        .result(s.getResult())
                        .xCoord(s.getX())
                        .yCoord(s.getY())
                        .xg(s.getXg())
                        .player(s.getPlayer())
                        .teamSide(s.getTeamSide())
                        .situation(s.getSituation())
                        .shotType(s.getShotType())
                        .lastAction(s.getLastAction())
                        .status("PROCESSED")
                        .payload(shotJson)
                        .createdAt(Instant.now())
                        .build();

                entityList.add(shot);
            }
        }

        List<UnderstatRawShot> saved = rawShotRepository.saveAll(entityList);

        // Stream to Kafka
        kafkaProducer.publishCanonicalShotMap(dto);

        return saved;
    }
}

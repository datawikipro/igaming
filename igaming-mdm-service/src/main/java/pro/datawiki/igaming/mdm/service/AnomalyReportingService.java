package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.MutantMatchAnomalyEvent;
import pro.datawiki.igaming.dto.UnmappedAnomalyEvent;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournament;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnomalyReportingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.anomaly-errors:mdm.anomaly.errors}")
    private String anomalyTopic;

    @Value("${app.kafka.topics.mutant-matches:mdm.match.mutants}")
    private String mutantMatchesTopic;

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

    public void reportMutantMatch(
            String externalSystemCode,
            MdmTournament tournament,
            MdmTeam homeTeam,
            MdmTeam awayTeam,
            String mismatchType,
            String reason
    ) {
        log.error("🚨 [MDM-MUTANT-BLOCKED] Source '{}': {} in tournament '{}' (#{}): home '{}' (#{}, gender={}, age={}) vs away '{}' (#{}, gender={}, age={}). Reason: {}",
                externalSystemCode, mismatchType,
                tournament != null ? tournament.getCanonicalName() : "N/A",
                tournament != null ? tournament.getId() : -1,
                homeTeam.getCanonicalName(), homeTeam.getId(), homeTeam.getGender(), homeTeam.getAgeCategory(),
                awayTeam.getCanonicalName(), awayTeam.getId(), awayTeam.getGender(), awayTeam.getAgeCategory(),
                reason);

        MutantMatchAnomalyEvent event = MutantMatchAnomalyEvent.builder()
                .externalSystemCode(externalSystemCode)
                .sportId(tournament != null && tournament.getSport() != null ? tournament.getSport().getId() : (homeTeam.getSport() != null ? homeTeam.getSport().getId() : null))
                .sportSlug(tournament != null && tournament.getSport() != null ? tournament.getSport().getSlug() : (homeTeam.getSport() != null ? homeTeam.getSport().getSlug() : null))
                .tournamentId(tournament != null ? tournament.getId() : null)
                .tournamentName(tournament != null ? tournament.getCanonicalName() : null)
                .tournamentGender(tournament != null ? tournament.getGender() : null)
                .tournamentAgeCategory(tournament != null ? tournament.getAgeCategory() : null)
                .homeTeamId(homeTeam.getId())
                .homeTeamName(homeTeam.getCanonicalName())
                .homeTeamGender(homeTeam.getGender())
                .homeTeamAgeCategory(homeTeam.getAgeCategory())
                .awayTeamId(awayTeam.getId())
                .awayTeamName(awayTeam.getCanonicalName())
                .awayTeamGender(awayTeam.getGender())
                .awayTeamAgeCategory(awayTeam.getAgeCategory())
                .mismatchType(mismatchType)
                .reason(reason)
                .build();

        try {
            String key = (tournament != null ? tournament.getId() : "0") + ":" + homeTeam.getId() + ":" + awayTeam.getId();
            kafkaTemplate.send(mutantMatchesTopic, key, event);
            log.info("📢 Emitted mutant match anomaly to Kafka topic '{}': key='{}'", mutantMatchesTopic, key);
        } catch (Exception e) {
            log.error("Failed to publish mutant match anomaly to Kafka topic '{}': {}", mutantMatchesTopic, e.getMessage(), e);
        }
    }

    public void reportCrawlerMetricAnomaly(
            String externalSystemCode,
            String sportSlug,
            Long matchId,
            String matchTitle,
            String metricValue,
            String anomalyType,
            String details,
            String resolutionHint
    ) {
        log.warn("🚨 [Crawler Metric Anomaly] {} on match #{} '{}' (sport={}): {} | {}",
                anomalyType, matchId, matchTitle, sportSlug, details, resolutionHint);
        UnmappedAnomalyEvent event = UnmappedAnomalyEvent.builder()
                .externalSystemCode(externalSystemCode)
                .entityType(anomalyType)
                .rawKey(matchId != null ? matchId.toString() : null)
                .rawName(matchTitle)
                .rawCategory(sportSlug)
                .details(details + (resolutionHint != null ? " (" + resolutionHint + ")" : ""))
                .build();
        reportAnomaly(event);
    }
}

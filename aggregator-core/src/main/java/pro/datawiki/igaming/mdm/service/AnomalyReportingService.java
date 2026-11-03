/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.kafka.core.KafkaTemplate
 *  org.springframework.stereotype.Service
 *  pro.datawiki.igaming.dto.MutantMatchAnomalyEvent
 *  pro.datawiki.igaming.dto.UnmappedAnomalyEvent
 */
package pro.datawiki.igaming.mdm.service;

import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.MutantMatchAnomalyEvent;
import pro.datawiki.igaming.dto.UnmappedAnomalyEvent;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournament;

@Service
public class AnomalyReportingService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(AnomalyReportingService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value(value="${app.kafka.topics.anomaly-errors:mdm.anomaly.errors}")
    private String anomalyTopic;
    @Value(value="${app.kafka.topics.mutant-matches:mdm.match.mutants}")
    private String mutantMatchesTopic;

    public void reportAnomaly(UnmappedAnomalyEvent event) {
        log.warn("\ud83d\udea8 [MDM Stage-1 Anomaly] Unmapped entity detected: system={}, type={}, rawKey='{}', rawName='{}'", new Object[]{event.getExternalSystemCode(), event.getEntityType(), event.getRawKey(), event.getRawName()});
        try {
            String key = event.getExternalSystemCode() + ":" + event.getEntityType() + ":" + event.getRawKey();
            this.kafkaTemplate.send(this.anomalyTopic, (Object)key, (Object)event);
        }
        catch (Exception e) {
            log.error("Failed to publish anomaly to Kafka topic '{}': {}", new Object[]{this.anomalyTopic, e.getMessage(), e});
        }
    }

    public void reportLeagueMismatch(String externalSystemCode, String violationType, Long tournamentId, String tournamentName, Long teamId, String teamName, String details) {
        log.warn("\ud83d\udea8 [MDM League Anomaly] [{}]: {}: tournament='{}' (#{}) vs team='{}' (#{}) - {}", new Object[]{externalSystemCode, violationType, tournamentName, tournamentId, teamName, teamId, details});
        UnmappedAnomalyEvent event = UnmappedAnomalyEvent.builder().externalSystemCode(externalSystemCode).entityType(violationType).rawKey(teamId != null ? teamId.toString() : null).rawName(teamName).rawParentKey(tournamentId != null ? tournamentId.toString() : null).rawCategory(tournamentName).details(details).build();
        this.reportAnomaly(event);
    }

    public void reportMutantMatch(String externalSystemCode, MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String mismatchType, String reason) {
        log.error("\ud83d\udea8 [MDM-MUTANT-BLOCKED] Source '{}': {} in tournament '{}' (#{}): home '{}' (#{}, gender={}, age={}) vs away '{}' (#{}, gender={}, age={}). Reason: {}", new Object[]{externalSystemCode, mismatchType, tournament != null ? tournament.getCanonicalName() : "N/A", tournament != null ? tournament.getId() : -1L, homeTeam.getCanonicalName(), homeTeam.getId(), homeTeam.getGender(), homeTeam.getAgeCategory(), awayTeam.getCanonicalName(), awayTeam.getId(), awayTeam.getGender(), awayTeam.getAgeCategory(), reason});
        MutantMatchAnomalyEvent event = MutantMatchAnomalyEvent.builder().externalSystemCode(externalSystemCode).sportId(tournament != null && tournament.getSport() != null ? tournament.getSport().getId() : (homeTeam.getSport() != null ? homeTeam.getSport().getId() : null)).sportSlug(tournament != null && tournament.getSport() != null ? tournament.getSport().getSlug() : (homeTeam.getSport() != null ? homeTeam.getSport().getSlug() : null)).tournamentId(tournament != null ? tournament.getId() : null).tournamentName(tournament != null ? tournament.getCanonicalName() : null).tournamentGender(tournament != null ? tournament.getGender() : null).tournamentAgeCategory(tournament != null ? tournament.getAgeCategory() : null).homeTeamId(homeTeam.getId()).homeTeamName(homeTeam.getCanonicalName()).homeTeamGender(homeTeam.getGender()).homeTeamAgeCategory(homeTeam.getAgeCategory()).awayTeamId(awayTeam.getId()).awayTeamName(awayTeam.getCanonicalName()).awayTeamGender(awayTeam.getGender()).awayTeamAgeCategory(awayTeam.getAgeCategory()).mismatchType(mismatchType).reason(reason).build();
        try {
            String key = String.valueOf(tournament != null ? tournament.getId() : "0") + ":" + homeTeam.getId() + ":" + awayTeam.getId();
            this.kafkaTemplate.send(this.mutantMatchesTopic, (Object)key, (Object)event);
            log.info("\ud83d\udce2 Emitted mutant match anomaly to Kafka topic '{}': key='{}'", (Object)this.mutantMatchesTopic, (Object)key);
        }
        catch (Exception e) {
            log.error("Failed to publish mutant match anomaly to Kafka topic '{}': {}", new Object[]{this.mutantMatchesTopic, e.getMessage(), e});
        }
    }

    public void reportCrawlerMetricAnomaly(String externalSystemCode, String sportSlug, Long matchId, String matchTitle, String metricValue, String anomalyType, String details, String resolutionHint) {
        log.warn("\ud83d\udea8 [Crawler Metric Anomaly] {} on match #{} '{}' (sport={}): {} | {}", new Object[]{anomalyType, matchId, matchTitle, sportSlug, details, resolutionHint});
        UnmappedAnomalyEvent event = UnmappedAnomalyEvent.builder().externalSystemCode(externalSystemCode).entityType(anomalyType).rawKey(matchId != null ? matchId.toString() : null).rawName(matchTitle).rawCategory(sportSlug).details(details + (String)(resolutionHint != null ? " (" + resolutionHint + ")" : "")).build();
        this.reportAnomaly(event);
    }

    @Generated
    public AnomalyReportingService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
}


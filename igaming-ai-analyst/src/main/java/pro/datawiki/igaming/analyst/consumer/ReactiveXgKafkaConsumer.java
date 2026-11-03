package pro.datawiki.igaming.analyst.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.model.TacticalMatchDossier;
import pro.datawiki.igaming.analyst.model.TacticalTensor;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;
import pro.datawiki.igaming.dto.CanonicalMatchXgDto;
import pro.datawiki.igaming.dto.CanonicalShotMapDto;

/**
 * Reactive Kafka consumer for xG streams and shot maps.
 * Enriches the match dossier and tactical pitch tensors directly in memory without heavy SQL joins.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveXgKafkaConsumer {

    private final TacticalDossierService tacticalDossierService;

    /**
     * Consumes advanced FBref xG statistics from topic sports.canonical.xg.
     */
    @KafkaListener(
            topics = "${app.kafka.topics.xg:sports.canonical.xg}",
            groupId = "${spring.kafka.consumer.group-id:igaming-ai-analyst-group}"
    )
    public void consumeMatchXg(CanonicalMatchXgDto xgDto) {
        if (xgDto == null) {
            log.warn("Received null CanonicalMatchXgDto from Kafka");
            return;
        }

        String matchId = xgDto.getMatchId() != null ? xgDto.getMatchId() : xgDto.getExternalMatchId();
        log.info("ReactiveXgKafkaConsumer received xG report for match [{}]: homeXg={}, awayXg={}",
                matchId, xgDto.getHomeXg(), xgDto.getAwayXg());

        TacticalMatchDossier dossier = tacticalDossierService.enrichWithXg(xgDto);
        log.info("Tactical dossier reactively updated for match [{}] with classification [{}]",
                matchId, dossier.getTacticalClassification());
    }

    /**
     * Consumes spatial Understat shot maps from topic sports.canonical.shots.
     */
    @KafkaListener(
            topics = "${app.kafka.topics.shots:sports.canonical.shots}",
            groupId = "${spring.kafka.consumer.group-id:igaming-ai-analyst-group}"
    )
    public void consumeShotMap(CanonicalShotMapDto shotMapDto) {
        if (shotMapDto == null) {
            log.warn("Received null CanonicalShotMapDto from Kafka");
            return;
        }

        String matchId = shotMapDto.getMatchId() != null ? shotMapDto.getMatchId() : shotMapDto.getExternalMatchId();
        int shotsCount = shotMapDto.getShots() != null ? shotMapDto.getShots().size() : 0;
        log.info("ReactiveXgKafkaConsumer received shot map for match [{}] with [{}] shots",
                matchId, shotsCount);

        TacticalTensor tensor = tacticalDossierService.enrichWithShotMap(shotMapDto);
        log.info("Tactical spatial tensor reactively updated for match [{}] with [{}] danger zones",
                matchId, tensor.getHomeDangerZones() != null ? tensor.getHomeDangerZones().size() : 0);
    }
}

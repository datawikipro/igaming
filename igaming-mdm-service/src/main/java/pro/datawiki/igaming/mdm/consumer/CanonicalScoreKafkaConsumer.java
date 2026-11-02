package pro.datawiki.igaming.mdm.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.mdm.service.MdmLiveScoreConsensusService;

/**
 * Event-driven Kafka consumer for canonical live and terminal match scores.
 * Replaces direct point-to-point HTTP requests with asynchronous Kafka streaming.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CanonicalScoreKafkaConsumer {

    private final MdmLiveScoreConsensusService liveScoreService;

    @KafkaListener(
            topics = "${app.kafka.topics.canonical-scores:sports.canonical.scores}",
            groupId = "${spring.kafka.consumer.group-id:mdm-score-consensus-group}"
    )
    public void onCanonicalScore(CanonicalLiveScoreDto scoreDto) {
        if (scoreDto == null || scoreDto.getMatchId() == null) {
            return;
        }

        try {
            liveScoreService.ingestLiveScore(scoreDto);
            log.debug("🎯 [MDM-Score-Kafka] Ingested live score for match #{}: {}:{} ({})",
                    scoreDto.getMatchId(), scoreDto.getScore1(), scoreDto.getScore2(), scoreDto.getStatus());
        } catch (Exception ex) {
            log.warn("❌ [MDM-Score-Kafka] Failed ingesting score for match #{}: {}", scoreDto.getMatchId(), ex.getMessage());
        }
    }
}

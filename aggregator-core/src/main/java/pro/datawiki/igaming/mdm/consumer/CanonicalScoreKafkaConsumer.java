/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.kafka.annotation.KafkaListener
 *  org.springframework.stereotype.Component
 *  pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto
 */
package pro.datawiki.igaming.mdm.consumer;

import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;
import pro.datawiki.igaming.mdm.service.MdmLiveScoreConsensusService;

@Component
public class CanonicalScoreKafkaConsumer {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(CanonicalScoreKafkaConsumer.class);
    private final MdmLiveScoreConsensusService liveScoreService;
    private final MdmMatchRepository matchRepository;

    @KafkaListener(topics={"${app.kafka.topics.canonical-scores:sports.canonical.scores}"}, groupId="${spring.kafka.consumer.group-id:mdm-score-consensus-group}")
    public void onCanonicalScore(CanonicalLiveScoreDto scoreDto) {
        if (scoreDto == null) {
            return;
        }
        if (scoreDto.getMatchId() == null && "sofascore".equalsIgnoreCase(scoreDto.getSourceCode()) && scoreDto.getExternalEventId() != null) {
            try {
                Long sofaId = Long.parseLong(scoreDto.getExternalEventId());
                this.matchRepository.findBySofascoreId(sofaId).ifPresent(m -> scoreDto.setMatchId(m.getId()));
            }
            catch (NumberFormatException sofaId) {
                // empty catch block
            }
        }
        if (scoreDto.getMatchId() == null) {
            return;
        }
        try {
            this.liveScoreService.ingestLiveScore(scoreDto);
            log.debug("\ud83c\udfaf [MDM-Score-Kafka] Ingested live score for match #{}: {}:{} ({})", new Object[]{scoreDto.getMatchId(), scoreDto.getScore1(), scoreDto.getScore2(), scoreDto.getStatus()});
        }
        catch (Exception ex) {
            log.warn("\u274c [MDM-Score-Kafka] Failed ingesting score for match #{}: {}", (Object)scoreDto.getMatchId(), (Object)ex.getMessage());
        }
    }

    @Generated
    public CanonicalScoreKafkaConsumer(MdmLiveScoreConsensusService liveScoreService, MdmMatchRepository matchRepository) {
        this.liveScoreService = liveScoreService;
        this.matchRepository = matchRepository;
    }
}


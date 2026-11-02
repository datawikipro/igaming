package pro.datawiki.igaming.analyst.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.service.MatchSettlementService;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.dto.live.MatchStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * High-performance Kafka consumer listening to canonical match scores.
 * Replaces the 2-minute polling cron with event-driven sub-second settlement.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveSettlementKafkaConsumer {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final MatchSettlementService settlementService;

    @KafkaListener(
            topics = "${app.kafka.topics.canonical-scores:sports.canonical.scores}",
            groupId = "${spring.kafka.consumer.group-id:ai-analyst-reactive-settlement-group}"
    )
    public void onCanonicalScoreEvent(CanonicalLiveScoreDto scoreDto) {
        if (scoreDto == null || scoreDto.getMatchId() == null) {
            return;
        }

        Long matchId = scoreDto.getMatchId();

        // We only trigger settlement when the match has reached a terminal state (FINISHED, CANCELLED, POSTPONED)
        if (!scoreDto.isFinished() && (scoreDto.getStatus() == null || !scoreDto.getStatus().isTerminal())) {
            return;
        }

        try {
            List<MatchAiRecommendation> pending = recommendationRepository.findByMatchIdAndResultStatus(matchId, "PENDING");
            if (pending == null || pending.isEmpty()) {
                return;
            }

            log.info("⚡ [REACTIVE-SETTLEMENT] Received terminal score event for match #{}: status={}, score={}:{} (Pending recs: {})",
                    matchId, scoreDto.getStatus(), scoreDto.getScore1(), scoreDto.getScore2(), pending.size());

            MatchStatus status = scoreDto.getStatus();
            if (status == MatchStatus.CANCELLED || status == MatchStatus.POSTPONED) {
                for (MatchAiRecommendation rec : pending) {
                    rec.setResultStatus("VOID");
                    rec.setEvaluationStage("SETTLED");
                    rec.setFinalScore("-:-");
                    rec.setActualPnl(BigDecimal.ZERO);
                    rec.setRoiPct(BigDecimal.ZERO);
                    rec.setErrorCategory("MATCH_" + status.name());
                    rec.setLessonLearned("Матч отменен или перенесен. Оформлен возврат (VOID) через шину событий Kafka.");
                    recommendationRepository.save(rec);
                    log.info("🎯 [REACTIVE-SETTLEMENT] Settled rec #{} for match #{} as VOID ({})", rec.getId(), matchId, status);
                }
                return;
            }

            if (scoreDto.getScore1() != null && scoreDto.getScore2() != null) {
                String scoreStr = scoreDto.getScore1() + ":" + scoreDto.getScore2();
                for (MatchAiRecommendation rec : pending) {
                    boolean settled = settlementService.settleRecommendationWithScore(rec, scoreStr);
                    if (settled) {
                        log.info("🎯 [REACTIVE-SETTLEMENT] Successfully settled rec #{} for match #{} with score {} (Result: {})",
                                rec.getId(), matchId, scoreStr, rec.getResultStatus());
                    } else {
                        settlementService.settleSingleRecommendation(rec);
                    }
                }
            } else {
                for (MatchAiRecommendation rec : pending) {
                    settlementService.settleSingleRecommendation(rec);
                }
            }
        } catch (Exception ex) {
            log.warn("❌ [REACTIVE-SETTLEMENT] Error settling recommendations for match #{}: {}", matchId, ex.getMessage(), ex);
        }
    }
}

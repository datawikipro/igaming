package pro.datawiki.igaming.analyst.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.service.MatchSettlementService;
import pro.datawiki.igaming.analyst.service.StrategyEngineService;
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
    private final pro.datawiki.igaming.analyst.service.StrategyEngineService strategyEngineService;
    private final pro.datawiki.igaming.analyst.service.MatchVisionReconciler matchVisionReconciler;
    private final pro.datawiki.igaming.analyst.rating.SportMmrService sportMmrService;
    private final pro.datawiki.igaming.analyst.service.MatchDossierService dossierService;

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

        // 1. Dynamic Universal Sports MMR Update for finished racket sport matches
        if (scoreDto.isFinished() && scoreDto.getScore1() != null && scoreDto.getScore2() != null) {
            String sport = scoreDto.getSport();
            if (sport != null) {
                String sLower = sport.toLowerCase(java.util.Locale.ROOT).replace('_', '-');
                if ("tennis".equals(sLower) || "table-tennis".equals(sLower)) {
                    try {
                        pro.datawiki.igaming.dto.CanonicalMatchHeaderDto header = dossierService.fetchMdmMatchHeader(matchId);
                        if (header != null && header.getHomeTeamId() != null && header.getAwayTeamId() != null) {
                            sportMmrService.processFinishedMdmMatch(
                                    matchId,
                                    sLower,
                                    header.getTournamentName(),
                                    header.getHomeTeamId(),
                                    header.getAwayTeamId(),
                                    scoreDto.getScore1(),
                                    scoreDto.getScore2(),
                                    header.getStartTimeEpochSec() != null ? java.time.Instant.ofEpochSecond(header.getStartTimeEpochSec()) : java.time.Instant.now(),
                                    header.getHomeTeamName(),
                                    header.getAwayTeamName(),
                                    header.getHomeCountryName(),
                                    header.getAwayCountryName(),
                                    scoreDto.getPeriods()
                            );
                        }
                    } catch (Exception ex) {
                        log.debug("Dynamic MMR update skipped for match #{}: {}", matchId, ex.getMessage());
                    }
                }
            }
        }

        try {
            List<MatchAiRecommendation> allRecs = recommendationRepository.findAllByMatchId(matchId);
            if (allRecs == null || allRecs.isEmpty()) {
                return;
            }

            // Target recommendations that are not settled yet, OR were prematurely settled as VOID / "-:-" / missing score
            List<MatchAiRecommendation> pending = allRecs.stream()
                    .filter(r -> !"SETTLED".equalsIgnoreCase(r.getEvaluationStage())
                            || "-:-".equals(r.getFinalScore())
                            || r.getFinalScore() == null
                            || "VOID".equalsIgnoreCase(r.getResultStatus()))
                    .toList();
            if (pending.isEmpty()) {
                return;
            }

            log.info("⚡ [REACTIVE-SETTLEMENT] Received terminal score event for match #{}: status={}, score={}:{} (Pending/re-settle recs: {})",
                    matchId, scoreDto.getStatus(), scoreDto.getScore1(), scoreDto.getScore2(), pending.size());

            MatchStatus status = scoreDto.getStatus();
            if (status == MatchStatus.CANCELLED || status == MatchStatus.POSTPONED) {
                for (MatchAiRecommendation rec : pending) {
                    rec.setEvaluationStage("SETTLED");
                    rec.setFinalScore("-:-");
                    rec.setErrorCategory("MATCH_" + status.name());
                    rec.setLessonLearned("Матч отменен или перенесен. Оформлен возврат (VOID) через шину событий Kafka.");
                    recommendationRepository.save(rec);
                    String spName = rec.getSportName() != null ? rec.getSportName() : scoreDto.getSport();
                    if (spName != null) {
                        strategyEngineService.settleVotesForRecommendation(rec.getId(), "-:-", 0, 0, pro.datawiki.igaming.dto.sport.Sports.get(spName));
                    }
                    log.info("🎯 [REACTIVE-SETTLEMENT] Settled rec #{} for match #{} as VOID ({})", rec.getId(), matchId, status);
                }
                return;
            }

            if (scoreDto.getScore1() != null && scoreDto.getScore2() != null) {
                String scoreStr = scoreDto.getScore1() + ":" + scoreDto.getScore2();
                matchVisionReconciler.reconcileMatchVision(matchId, scoreStr, null);
                for (MatchAiRecommendation rec : pending) {
                    pro.datawiki.igaming.dto.sport.SettlementResult res = settlementService.settleRecommendationWithScore(rec, scoreStr);
                    if (res.isTerminal()) {
                        log.info("🎯 [REACTIVE-SETTLEMENT] Successfully settled rec #{} for match #{} with score {} -> {}",
                                rec.getId(), matchId, scoreStr, res);
                    } else if (res.isDeferred()) {
                        log.debug("⏳ [REACTIVE-SETTLEMENT] Settlement deferred for rec #{}: {}", rec.getId(), res);
                    } else {
                        log.warn("⚠️ [REACTIVE-SETTLEMENT] Settlement rejected for rec #{}: {}", rec.getId(), res);
                    }
                }
            } else {
                for (MatchAiRecommendation rec : pending) {
                    pro.datawiki.igaming.dto.sport.SettlementResult res = settlementService.settleSingleRecommendation(rec);
                    if (res.isTerminal()) {
                        log.info("🎯 [REACTIVE-SETTLEMENT] Successfully settled rec #{} for match #{} -> {}",
                                rec.getId(), matchId, res);
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("❌ [REACTIVE-SETTLEMENT] Error settling recommendations for match #{}: {}", matchId, ex.getMessage(), ex);
        }
    }
}

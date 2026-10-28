package pro.datawiki.igaming.analyst.state;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Polymorphic State Machine and Resolver for Match AI Recommendations.
 * Encapsulates lifecycle transitions, timing deltas, and in-play progression.
 */
@Slf4j
@Component
public class RecommendationStateMachine {

    public static final RecommendationStates.SettledWonState SETTLED_WON = new RecommendationStates.SettledWonState();
    public static final RecommendationStates.SettledLostState SETTLED_LOST = new RecommendationStates.SettledLostState();
    public static final RecommendationStates.SettledVoidState SETTLED_VOID = new RecommendationStates.SettledVoidState();
    public static final RecommendationStates.CancelledState CANCELLED = new RecommendationStates.CancelledState();
    public static final RecommendationStates.ScheduleConflictState SCHEDULE_CONFLICT = new RecommendationStates.ScheduleConflictState();
    public static final RecommendationStates.SkippedState SKIPPED = new RecommendationStates.SkippedState();
    public static final RecommendationStates.InPlayLiveState IN_PLAY = new RecommendationStates.InPlayLiveState();
    public static final RecommendationStates.AwaitingSettlementState AWAITING_SETTLEMENT = new RecommendationStates.AwaitingSettlementState();
    public static final RecommendationStates.KickoffVerifiedState KICKOFF_VERIFIED = new RecommendationStates.KickoffVerifiedState();
    public static final RecommendationStates.WaitKickoffState WAIT_KICKOFF = new RecommendationStates.WaitKickoffState();
    public static final RecommendationStates.LineupConfirmedState LINEUP_CONFIRMED = new RecommendationStates.LineupConfirmedState();
    public static final RecommendationStates.Stage2PreviewState STAGE_2_PREVIEW = new RecommendationStates.Stage2PreviewState();
    public static final RecommendationStates.ProjectedLineupState PROJECTED = new RecommendationStates.ProjectedLineupState();
    public static final RecommendationStates.PreLineupState PRE_LINEUP = new RecommendationStates.PreLineupState();

    /**
     * Resolves the current lifecycle state of a match recommendation.
     */
    public RecommendationLifecycleState resolve(MatchAiRecommendation rec, Instant now) {
        if (rec == null) return PRE_LINEUP;

        String status = rec.getResultStatus() != null ? rec.getResultStatus().toUpperCase().trim() : "PENDING";
        String stage = rec.getEvaluationStage() != null ? rec.getEvaluationStage().toUpperCase().trim() : "PRE_LINEUP";
        String lineup = rec.getLineupStatus() != null ? rec.getLineupStatus().toUpperCase().trim() : "ESTIMATED";

        // 1. Terminal / Settled outcomes
        if ("WON".equals(status)) return SETTLED_WON;
        if ("LOST".equals(status)) return SETTLED_LOST;
        if ("VOID".equals(status)) return SETTLED_VOID;
        if ("CANCELLED".equals(status) || "CANCELLED_SCHEDULE_CONFLICT".equals(stage) || "CANCELLED_SYNTHETIC_COLLISION".equals(stage)) return CANCELLED;
        if ("SCHEDULE_CONFLICT_PENDING_RESOLUTION".equals(stage) || "CONFLICT".equals(status)) return SCHEDULE_CONFLICT;
        if ("SKIPPED".equals(status) || stage.startsWith("SKIPPED_")) return SKIPPED;

        // 2. Pending match with start time
        LocalDateTime startLdt = rec.getMatchStartTime();
        if (startLdt != null) {
            long startMs = startLdt.toInstant(ZoneOffset.UTC).toEpochMilli();
            long nowMs = now.toEpochMilli();
            long elapsedMinutes = (nowMs - startMs) / 60000L;
            Sport sport = Sports.get(rec.getSportName() != null ? rec.getSportName() : "FOOTBALL");
            int maxDur = sport.getTypicalMatchDurationMinutes();

            if (startMs <= nowMs) {
                // Kickoff grace: within 2 minutes of start while stage is WAIT_KICKOFF
                if ("WAIT_KICKOFF".equals(stage) && (nowMs - startMs <= 120000L)) {
                    return WAIT_KICKOFF;
                }
                if (elapsedMinutes > maxDur) {
                    return AWAITING_SETTLEMENT;
                }
                return IN_PLAY;
            }

            // Future match
            if ("KICKOFF_VERIFIED".equals(stage)) return KICKOFF_VERIFIED;
            if ("WAIT_KICKOFF".equals(stage)) return WAIT_KICKOFF;
            if ("CONFIRMED".equals(lineup) || "LINEUP_CONFIRMED".equals(stage)) return LINEUP_CONFIRMED;
            if ("STAGE_2_PREVIEW".equals(stage)) return STAGE_2_PREVIEW;
            if ("PROJECTED".equals(lineup) || "PROJECTED".equals(stage)) return PROJECTED;
            return PRE_LINEUP;
        }

        // Fallbacks without start time
        if ("IN_PLAY".equals(stage)) return IN_PLAY;
        if ("CONFIRMED".equals(lineup) || "LINEUP_CONFIRMED".equals(stage)) return LINEUP_CONFIRMED;
        if ("PROJECTED".equals(lineup) || "PROJECTED".equals(stage)) return PROJECTED;
        return PRE_LINEUP;
    }

    /**
     * Polymorphically advances a pending match through its lifecycle:
     * - Transitions to KICKOFF_VERIFIED within 1 minute of start
     * - Transitions to IN_PLAY once 2+ minutes past start
     * Returns true if entity state was updated.
     */
    public boolean advanceLifecycle(MatchAiRecommendation rec, Instant now) {
        if (rec == null) return false;
        String status = rec.getResultStatus() != null ? rec.getResultStatus().toUpperCase().trim() : "PENDING";
        if (!"PENDING".equals(status)) return false;

        LocalDateTime startLdt = rec.getMatchStartTime();
        if (startLdt == null) return false;

        long startMs = startLdt.toInstant(ZoneOffset.UTC).toEpochMilli();
        long nowMs = now.toEpochMilli();
        long diffMinutes = (startMs - nowMs) / 60000L; // positive if in future, negative if in past

        if (diffMinutes < -2) {
            // Match is in live in-play
            if (!"IN_PLAY".equals(rec.getEvaluationStage())) {
                return IN_PLAY.applyTransition(rec, now);
            }
        } else if (diffMinutes <= 1) {
            // Match is in kickoff phase
            if (!"KICKOFF_VERIFIED".equals(rec.getEvaluationStage()) && !"IN_PLAY".equals(rec.getEvaluationStage())) {
                return KICKOFF_VERIFIED.applyTransition(rec, now);
            }
        }
        return false;
    }
}

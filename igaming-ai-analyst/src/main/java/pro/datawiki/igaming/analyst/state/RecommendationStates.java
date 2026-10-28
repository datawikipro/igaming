package pro.datawiki.igaming.analyst.state;

import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.time.Instant;
import java.time.ZoneOffset;

public final class RecommendationStates {

    private RecommendationStates() {}

    // -------------------------------------------------------------------------
    // Terminal States
    // -------------------------------------------------------------------------

    public static class SettledWonState implements RecommendationLifecycleState {
        @Override public String getCode() { return "WON"; }
        @Override public String getDisplayName() { return "✅ ВЫИГРАНО"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return true; }
        @Override public boolean isTerminal() { return true; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            return rec != null && "WON".equalsIgnoreCase(rec.getResultStatus());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    public static class SettledLostState implements RecommendationLifecycleState {
        @Override public String getCode() { return "LOST"; }
        @Override public String getDisplayName() { return "❌ ПРОИГРАНО"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return true; }
        @Override public boolean isTerminal() { return true; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            return rec != null && "LOST".equalsIgnoreCase(rec.getResultStatus());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    public static class SettledVoidState implements RecommendationLifecycleState {
        @Override public String getCode() { return "VOID"; }
        @Override public String getDisplayName() { return "🔄 ВОЗВРАТ"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return true; }
        @Override public boolean isTerminal() { return true; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            return rec != null && "VOID".equalsIgnoreCase(rec.getResultStatus());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    public static class CancelledState implements RecommendationLifecycleState {
        @Override public String getCode() { return "CANCELLED"; }
        @Override public String getDisplayName() { return "🚫 ОТМЕНЕН"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return true; }
        @Override public boolean isTerminal() { return true; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            if (rec == null) return false;
            return "CANCELLED".equalsIgnoreCase(rec.getResultStatus()) ||
                    "CANCELLED_SCHEDULE_CONFLICT".equalsIgnoreCase(rec.getEvaluationStage());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    public static class SkippedState implements RecommendationLifecycleState {
        @Override public String getCode() { return "SKIPPED"; }
        @Override public String getDisplayName() { return "🚫 SKIPPED"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return true; }
        @Override public boolean isTerminal() { return true; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            if (rec == null) return false;
            String st = rec.getEvaluationStage();
            return "SKIPPED".equalsIgnoreCase(rec.getResultStatus()) ||
                    (st != null && st.toUpperCase().startsWith("SKIPPED_"));
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Active In-Play / Pending States
    // -------------------------------------------------------------------------

    public static class InPlayLiveState implements RecommendationLifecycleState {
        @Override public String getCode() { return "IN_PLAY"; }
        @Override public String getDisplayName() { return "🔴 LIVE / В игре"; }
        @Override public boolean isLive() { return true; }
        @Override public boolean isSettled() { return false; }
        @Override public boolean isTerminal() { return false; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            if (rec == null) return false;
            if ("IN_PLAY".equalsIgnoreCase(rec.getEvaluationStage())) return true;
            if ("PENDING".equalsIgnoreCase(rec.getResultStatus()) && rec.getMatchStartTime() != null) {
                Instant start = rec.getMatchStartTime().toInstant(ZoneOffset.UTC);
                return !start.isAfter(now);
            }
            return false;
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            boolean modified = false;
            if (!"IN_PLAY".equalsIgnoreCase(rec.getEvaluationStage())) {
                rec.setEvaluationStage("IN_PLAY");
                modified = true;
            }
            if (rec.getCoreHypothesis() != null) {
                String updated = rec.getCoreHypothesis()
                        .replaceFirst("^⚡ ПРЕДМАТЧЕВАЯ ВЕРИФИКАЦИЯ \\(ML Tactical Net\\): ", "🔴 LIVE / IN-PLAY (В игре): ")
                        .replaceFirst("^🔮 СЦЕНАРНЫЙ АНАЛИЗ СОСТАВОВ \\(Projected XI\\): ", "🔴 LIVE / IN-PLAY (В игре): ")
                        .replaceFirst("^✅ БОЕВАЯ ВЕРИФИКАЦИЯ \\(Составы подтверждены\\): ", "🔴 LIVE / IN-PLAY (В игре): ")
                        .replaceFirst("^⚡ КИК-ОФФ ВЕРИФИКАЦИЯ [^:]+: ", "🔴 LIVE / IN-PLAY (В игре): ");
                if (!updated.equals(rec.getCoreHypothesis())) {
                    rec.setCoreHypothesis(updated);
                    modified = true;
                }
            }
            String liveRisk = "Матч находится в лайве (In-Play). Исходная оценка подтверждена тактической моделью. Следим за ходом матча.";
            if (!liveRisk.equals(rec.getIdentifiedRisk())) {
                rec.setIdentifiedRisk(liveRisk);
                modified = true;
            }
            return modified;
        }
    }

    public static class AwaitingSettlementState implements RecommendationLifecycleState {
        @Override public String getCode() { return "AWAITING_SETTLEMENT"; }
        @Override public String getDisplayName() { return "⏱️ ОЖИДАЕТ РАСЧЕТА"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return false; }
        @Override public boolean isTerminal() { return false; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            if (rec == null || rec.getMatchStartTime() == null || !"PENDING".equalsIgnoreCase(rec.getResultStatus())) {
                return false;
            }
            Sport sport = Sports.get(rec.getSportName() != null ? rec.getSportName() : "FOOTBALL");
            long startSec = rec.getMatchStartTime().toEpochSecond(ZoneOffset.UTC);
            long elapsedSec = now.getEpochSecond() - startSec;
            return elapsedSec > (sport.getTypicalMatchDurationMinutes() * 60L);
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Pre-Match Kickoff States
    // -------------------------------------------------------------------------

    public static class KickoffVerifiedState implements RecommendationLifecycleState {
        @Override public String getCode() { return "KICKOFF_VERIFIED"; }
        @Override public String getDisplayName() { return "⚡ Старт 1м"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return false; }
        @Override public boolean isTerminal() { return false; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            return rec != null && "KICKOFF_VERIFIED".equalsIgnoreCase(rec.getEvaluationStage());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            boolean modified = false;
            if (!"KICKOFF_VERIFIED".equalsIgnoreCase(rec.getEvaluationStage())) {
                rec.setEvaluationStage("KICKOFF_VERIFIED");
                modified = true;
            }
            if (rec.getCoreHypothesis() != null) {
                String updated = rec.getCoreHypothesis()
                        .replaceFirst("^⚡ ПРЕДМАТЧЕВАЯ ВЕРИФИКАЦИЯ \\(ML Tactical Net\\): ", "⚡ КИК-ОФФ ВЕРИФИКАЦИЯ (Стартовый свисток): ")
                        .replaceFirst("^🔮 СЦЕНАРНЫЙ АНАЛИЗ СОСТАВОВ \\(Projected XI\\): ", "⚡ КИК-ОФФ ВЕРИФИКАЦИЯ (Стартовый свисток): ");
                if (!updated.equals(rec.getCoreHypothesis())) {
                    rec.setCoreHypothesis(updated);
                    modified = true;
                }
            }
            return modified;
        }
    }

    public static class WaitKickoffState implements RecommendationLifecycleState {
        @Override public String getCode() { return "WAIT_KICKOFF"; }
        @Override public String getDisplayName() { return "⏱️ Ждет свистка"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return false; }
        @Override public boolean isTerminal() { return false; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            return rec != null && "WAIT_KICKOFF".equalsIgnoreCase(rec.getEvaluationStage());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Pre-Match Tactical Screening States
    // -------------------------------------------------------------------------

    public static class LineupConfirmedState implements RecommendationLifecycleState {
        @Override public String getCode() { return "LINEUP_CONFIRMED"; }
        @Override public String getDisplayName() { return "✅ Составы 11x11"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return false; }
        @Override public boolean isTerminal() { return false; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            if (rec == null) return false;
            return "CONFIRMED".equalsIgnoreCase(rec.getLineupStatus()) ||
                    "LINEUP_CONFIRMED".equalsIgnoreCase(rec.getEvaluationStage());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    public static class Stage2PreviewState implements RecommendationLifecycleState {
        @Override public String getCode() { return "STAGE_2_PREVIEW"; }
        @Override public String getDisplayName() { return "⚡ Предматч"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return false; }
        @Override public boolean isTerminal() { return false; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            return rec != null && "STAGE_2_PREVIEW".equalsIgnoreCase(rec.getEvaluationStage());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    public static class ProjectedLineupState implements RecommendationLifecycleState {
        @Override public String getCode() { return "PROJECTED"; }
        @Override public String getDisplayName() { return "🧠 Прогнозируемые составы"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return false; }
        @Override public boolean isTerminal() { return false; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            if (rec == null) return false;
            return "PROJECTED".equalsIgnoreCase(rec.getLineupStatus()) ||
                    "PROJECTED".equalsIgnoreCase(rec.getEvaluationStage());
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }

    public static class PreLineupState implements RecommendationLifecycleState {
        @Override public String getCode() { return "PRE_LINEUP"; }
        @Override public String getDisplayName() { return "🔮 Pre-Lineup"; }
        @Override public boolean isLive() { return false; }
        @Override public boolean isSettled() { return false; }
        @Override public boolean isTerminal() { return false; }

        @Override
        public boolean matches(MatchAiRecommendation rec, Instant now) {
            return true; // Fallback
        }

        @Override
        public boolean applyTransition(MatchAiRecommendation rec, Instant now) {
            return false;
        }
    }
}

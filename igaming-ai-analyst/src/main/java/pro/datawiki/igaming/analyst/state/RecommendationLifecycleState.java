package pro.datawiki.igaming.analyst.state;

import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import java.time.Instant;

/**
 * Domain interface representing a recommendation lifecycle state (State Pattern).
 */
public interface RecommendationLifecycleState {

    /**
     * Canonical state code (e.g. IN_PLAY, KICKOFF_VERIFIED, WON, LOST, VOID, CANCELLED, etc.).
     */
    String getCode();

    /**
     * Localized UI display name.
     */
    String getDisplayName();

    /**
     * Whether the match is currently live in-play.
     */
    boolean isLive();

    /**
     * Whether outcome is settled.
     */
    boolean isSettled();

    /**
     * Whether state is terminal (no further transitions).
     */
    boolean isTerminal();

    /**
     * Whether the recommendation matches this state at the given time instant.
     */
    boolean matches(MatchAiRecommendation rec, Instant now);

    /**
     * Applies state transition mutations to the recommendation entity.
     * Returns true if recommendation was modified.
     */
    boolean applyTransition(MatchAiRecommendation rec, Instant now);
}

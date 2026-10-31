package pro.datawiki.igaming.analyst.forensic.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.List;
import java.util.Map;

/**
 * Full-spectrum match dossier context passed to sport-specific forensic auditors.
 * Carries pre-match vector expectations and post-match ground-truth protocols.
 */
@Getter
@Builder
@ToString
public class ForensicMatchContext {
    private final Long recommendationId;
    private final Long matchId;
    private final Sport sport;
    private final String sportSlug;
    private final String homeTeam;
    private final String awayTeam;
    private final String leagueName;
    private final String matchStartTime;
    private final String recommendedMarket;
    private final Double recommendedOdd;
    private final Double fairProbability;
    private final String coreHypothesis;
    private final String finalScore;
    private final String periodTimeline;

    // Lineup & Biological Age Coordinates
    private final Double homeDefenseAvgAge;
    private final Double homeAttackAvgAge;
    private final Double awayDefenseAvgAge;
    private final Double awayAttackAvgAge;
    private final Double paceMismatchDelta;

    // Attacking Hierarchy: Alpha Talisman vs Beta Wingman
    private final String homeTalismanName;
    private final Double homeTalismanGoalPct;
    private final String homeTalismanStatus; // "PLAYING", "BENCH", "INJURED", "SUSPENDED"
    private final String homeBetaName;
    private final String homeBetaStatus;

    private final String awayTalismanName;
    private final Double awayTalismanGoalPct;
    private final String awayTalismanStatus;
    private final String awayBetaName;
    private final String awayBetaStatus;

    // International Travel & Bio-Congestion
    private final Integer homeNationalTeamFatiguedCount;
    private final Integer awayNationalTeamFatiguedCount;
    private final List<String> homeMissingList;
    private final List<String> awayMissingList;

    // Timelines & Protocols
    private final List<String> goalChronology;
    private final List<String> cardsChronology;
    private final List<String> subsChronology;

    // Sport-Specific Tactical Payload
    private final Map<String, Object> sportAttributes;
}

package pro.datawiki.igaming.playerfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Surface-specific and career tiebreak statistics and clutch performance metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TiebreakStats {

    /** Total tiebreaks played */
    private Integer tiebreaksPlayed;

    /** Total tiebreaks won */
    private Integer tiebreaksWon;

    /** Tiebreak winning percentage (e.g. 62.5%) */
    private Double tiebreakWinPct;

    /** Deciding-set tiebreaks played */
    private Integer decidingSetTiebreaksPlayed;

    /** Deciding-set tiebreaks won */
    private Integer decidingSetTiebreaksWon;

    /**
     * Calculates tiebreak win percentage from played and won counts if missing.
     */
    public Double getEffectiveWinPct() {
        if (tiebreakWinPct != null) {
            return tiebreakWinPct;
        }
        if (tiebreaksPlayed != null && tiebreaksPlayed > 0 && tiebreaksWon != null) {
            return (tiebreaksWon * 100.0) / tiebreaksPlayed;
        }
        return 50.0;
    }
}

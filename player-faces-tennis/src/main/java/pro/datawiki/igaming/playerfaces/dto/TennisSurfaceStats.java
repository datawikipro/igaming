package pro.datawiki.igaming.playerfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.playerfaces.domain.TennisSurface;

/**
 * Surface-segregated point-by-point stats (Hard, Clay, Grass, Indoor).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TennisSurfaceStats {

    /** Court surface (HARD, CLAY, GRASS, INDOOR) */
    private TennisSurface surface;

    /** Number of matches in statistical sample */
    private Integer matchesPlayed;

    /** Total games won / played */
    private Integer gamesWon;
    private Integer gamesTotal;

    /** Serving statistics */
    private PointByPointServeStats serveStats;

    /** Returning and break point conversion statistics */
    private PointByPointReturnStats returnStats;

    /** Break points faced and saved on serve */
    private Integer breakPointsFaced;
    private Integer breakPointsSaved;

    /** % of break points saved on serve */
    private Double breakPointSavePct;

    /** Tiebreak statistics */
    private TiebreakStats tiebreakStats;

    /** Surface-specific dominance ratio (Hold% / (100% - Break%)) */
    public Double getDominanceRatio() {
        if (serveStats == null || returnStats == null) {
            return 1.0;
        }
        Double holdPct = serveStats.getServiceGamesWonPct();
        Double breakPct = returnStats.getReturnGamesWonPct();
        if (holdPct == null || breakPct == null || breakPct >= 100.0) {
            return 1.0;
        }
        double opponentHoldPct = 100.0 - breakPct;
        return opponentHoldPct <= 0 ? 1.0 : (holdPct / opponentHoldPct);
    }
}

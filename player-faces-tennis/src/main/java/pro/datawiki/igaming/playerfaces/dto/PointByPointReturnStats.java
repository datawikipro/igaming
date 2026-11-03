package pro.datawiki.igaming.playerfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Point-by-point return and break-point conversion statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointByPointReturnStats {

    /** % of points won returning opponent's 1st serve (e.g. 32.5%) */
    private Double firstServeReturnWonPct;

    /** % of points won returning opponent's 2nd serve (e.g. 52.8%) */
    private Double secondServeReturnWonPct;

    /** Number of break points converted */
    private Integer breakPointsWon;

    /** Total break point opportunities had */
    private Integer breakPointsTotal;

    /** Break point conversion percentage (e.g. 42.1%) */
    private Double breakPointConversionPct;

    /** Return games won percentage (Break % e.g. 26.4%) */
    private Double returnGamesWonPct;

    /** Total return points analyzed */
    private Integer totalReturnPoints;
}

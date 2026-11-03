package pro.datawiki.igaming.playerfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Point-by-point serving statistics harvested from Tennis Abstract / Tennis Explorer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointByPointServeStats {

    /** % of first serves landing in court (e.g. 64.5%) */
    private Double firstServeInPct;

    /** % of points won when first serve lands in (e.g. 76.8%) */
    private Double firstServeWonPct;

    /** % of points won on second serve (e.g. 54.2%) */
    private Double secondServeWonPct;

    /** % of service points ending in an ace */
    private Double acePct;

    /** % of service points ending in a double fault */
    private Double doubleFaultPct;

    /** % of service games held (e.g. 84.6%) */
    private Double serviceGamesWonPct;

    /** Total service points analyzed */
    private Integer totalServicePoints;
}

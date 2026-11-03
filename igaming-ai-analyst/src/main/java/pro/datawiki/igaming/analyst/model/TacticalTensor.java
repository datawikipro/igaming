package pro.datawiki.igaming.analyst.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Tactical tensor representing spatial pitch threat and shot probability distributions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TacticalTensor implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("match_id")
    private String matchId;

    @JsonProperty("grid_rows")
    @Builder.Default
    private int gridRows = 10;

    @JsonProperty("grid_cols")
    @Builder.Default
    private int gridCols = 10;

    /**
     * 10x10 spatial pitch danger grid for home team (x: pitch length 0..1, y: pitch width 0..1).
     */
    @JsonProperty("home_spatial_danger_matrix")
    private double[][] homeSpatialDangerMatrix;

    /**
     * 10x10 spatial pitch danger grid for away team.
     */
    @JsonProperty("away_spatial_danger_matrix")
    private double[][] awaySpatialDangerMatrix;

    /**
     * Named danger zone distributions (e.g., SIX_YARD_BOX, PENALTY_BOX, CENTRAL_BOX, LEFT_FLANK, RIGHT_FLANK, LONG_RANGE).
     */
    @JsonProperty("home_danger_zones")
    private Map<String, Double> homeDangerZones;

    @JsonProperty("away_danger_zones")
    private Map<String, Double> awayDangerZones;

    /**
     * 4-component vector of offensive vector pressure: [left, central, right, depth].
     */
    @JsonProperty("home_pressure_vector")
    private double[] homePressureVector;

    @JsonProperty("away_pressure_vector")
    private double[] awayPressureVector;

    @JsonProperty("total_accumulated_xg")
    private Double totalAccumulatedXg;

    @JsonProperty("last_updated_epoch_ms")
    private Long lastUpdatedEpochMs;
}

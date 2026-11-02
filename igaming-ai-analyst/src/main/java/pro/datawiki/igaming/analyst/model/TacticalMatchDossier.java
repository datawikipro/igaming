package pro.datawiki.igaming.analyst.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Match tactical dossier enriched reactively from xG and shot map streams.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TacticalMatchDossier implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("match_id")
    private String matchId;

    @JsonProperty("sport")
    private String sport;

    @JsonProperty("competition")
    private String competition;

    @JsonProperty("season")
    private String season;

    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    @JsonProperty("home_score")
    private Integer homeScore;

    @JsonProperty("away_score")
    private Integer awayScore;

    // xG advanced metrics
    @JsonProperty("home_xg")
    private Double homeXg;

    @JsonProperty("away_xg")
    private Double awayXg;

    @JsonProperty("xg_differential")
    private Double xgDifferential;

    @JsonProperty("home_xag")
    private Double homeXag;

    @JsonProperty("away_xag")
    private Double awayXag;

    @JsonProperty("home_sca")
    private Integer homeSca;

    @JsonProperty("away_sca")
    private Integer awaySca;

    @JsonProperty("home_gca")
    private Integer homeGca;

    @JsonProperty("away_gca")
    private Integer awayGca;

    @JsonProperty("home_npxg")
    private Double homeNpxg;

    @JsonProperty("away_npxg")
    private Double awayNpxg;

    @JsonProperty("home_prg_p")
    private Integer homePrgP;

    @JsonProperty("away_prg_p")
    private Integer awayPrgP;

    @JsonProperty("home_prg_c")
    private Integer homePrgC;

    @JsonProperty("away_prg_c")
    private Integer awayPrgC;

    // Shot map enrichment
    @JsonProperty("total_shots")
    private Integer totalShots;

    @JsonProperty("home_shots")
    private Integer homeShots;

    @JsonProperty("away_shots")
    private Integer awayShots;

    @JsonProperty("home_shot_conversion_rate")
    private Double homeShotConversionRate;

    @JsonProperty("away_shot_conversion_rate")
    private Double awayShotConversionRate;

    @JsonProperty("tactical_classification")
    private String tacticalClassification;

    @JsonProperty("key_findings")
    private List<String> keyFindings;

    @JsonProperty("player_impact_metrics")
    private Map<String, Object> playerImpactMetrics;

    @JsonProperty("last_enriched_epoch_ms")
    private Long lastEnrichedEpochMs;
}

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
 * Match tactical dossier enriched reactively from Kafka event streams:
 * xG, shot maps, canonical injuries, canonical weather, referee analytics,
 * smart money liquidity, and tennis point-by-point momentum.
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

    // Tactical classification
    @JsonProperty("tactical_classification")
    private String tacticalClassification;

    @JsonProperty("key_findings")
    private List<String> keyFindings;

    @JsonProperty("player_impact_metrics")
    private Map<String, Object> playerImpactMetrics;

    // Canonical Injury stream enrichment
    @JsonProperty("home_absence_impact")
    private Double homeAbsenceImpact;

    @JsonProperty("away_absence_impact")
    private Double awayAbsenceImpact;

    @JsonProperty("net_injury_advantage_home")
    private Double netInjuryAdvantageHome;

    @JsonProperty("injury_confidence_score")
    private Double injuryConfidenceScore;

    @JsonProperty("home_absent_players")
    private List<String> homeAbsentPlayers;

    @JsonProperty("away_absent_players")
    private List<String> awayAbsentPlayers;

    // Canonical Weather stream enrichment
    @JsonProperty("weather_condition")
    private String weatherCondition;

    @JsonProperty("weather_under_bias")
    private Double weatherUnderBias;

    @JsonProperty("weather_wind_speed_kmh")
    private Double weatherWindSpeedKmh;

    @JsonProperty("weather_temperature_c")
    private Double weatherTemperatureC;

    @JsonProperty("weather_precipitation_prob")
    private Double weatherPrecipitationProb;

    // Canonical Referee stream enrichment
    @JsonProperty("referee_name")
    private String refereeName;

    @JsonProperty("referee_strictness_index")
    private Double refereeStrictnessIndex;

    @JsonProperty("referee_strictness_tier")
    private String refereeStrictnessTier;

    @JsonProperty("referee_yellow_cards_per_match")
    private Double refereeYellowCardsPerMatch;

    @JsonProperty("referee_red_cards_per_match")
    private Double refereeRedCardsPerMatch;

    @JsonProperty("referee_fouls_per_match")
    private Double refereeFoulsPerMatch;

    @JsonProperty("referee_penalties_per_match")
    private Double refereePenaltiesPerMatch;

    // Smart Money / Betfair liquidity stream enrichment
    @JsonProperty("smart_money_alert_count")
    private Integer smartMoneyAlertCount;

    @JsonProperty("smart_money_dominant_side")
    private String smartMoneyDominantSide;

    @JsonProperty("smart_money_volume_eur")
    private Double smartMoneyVolumeEur;

    @JsonProperty("smart_money_signal_strength")
    private Double smartMoneySignalStrength;

    // Tennis Point-by-Point momentum enrichment
    @JsonProperty("tennis_first_serve_pct")
    private Double tennisFirstServePct;

    @JsonProperty("tennis_second_serve_win_pct")
    private Double tennisSecondServeWinPct;

    @JsonProperty("tennis_momentum_score")
    private Double tennisMomentumScore;

    @JsonProperty("last_enriched_epoch_ms")
    private Long lastEnrichedEpochMs;
}

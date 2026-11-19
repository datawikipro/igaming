package pro.datawiki.igaming.analyst.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Unified External Context Warehouse ("Склад фактов") per match.
 * Persists demographic/geographic public-bias indicators (mdm_city / mdm_venue),
 * stadium weather forecast (Open-Meteo), anomaly-triggered news radar findings,
 * and structured odds-quality assessment — strictly separated from raw ML outcome features.
 */
@Entity
@Table(name = "match_external_context")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchExternalContextEntity {

    @Id
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "sport_slug")
    private String sportSlug;

    @Column(name = "mdm_home_team_id")
    private Long mdmHomeTeamId;

    @Column(name = "mdm_away_team_id")
    private Long mdmAwayTeamId;

    @Column(name = "home_city_id")
    private Long homeCityId;

    @Column(name = "away_city_id")
    private Long awayCityId;

    @Column(name = "home_city_name")
    private String homeCityName;

    @Column(name = "away_city_name")
    private String awayCityName;

    @Column(name = "home_city_population")
    private Long homeCityPopulation;

    @Column(name = "away_city_population")
    private Long awayCityPopulation;

    @Column(name = "home_is_capital")
    private Boolean homeIsCapital;

    @Column(name = "away_is_capital")
    private Boolean awayIsCapital;

    @Column(name = "metro_bias_index", precision = 6, scale = 3)
    private BigDecimal metroBiasIndex;

    @Column(name = "travel_distance_km")
    private Integer travelDistanceKm;

    @Column(name = "altitude_diff_m")
    private Integer altitudeDiffM;

    @Column(name = "timezone_diff_hours", precision = 4, scale = 1)
    private BigDecimal timezoneDiffHours;

    @Column(name = "venue_capacity")
    private Integer venueCapacity;

    @Column(name = "is_indoor")
    private Boolean isIndoor;

    @Column(name = "weather_temp_c", precision = 5, scale = 1)
    private BigDecimal weatherTempC;

    @Column(name = "weather_wind_kmh", precision = 5, scale = 1)
    private BigDecimal weatherWindKmh;

    @Column(name = "weather_precip_mm", precision = 5, scale = 2)
    private BigDecimal weatherPrecipMm;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(name = "weather_risk_level")
    private String weatherRiskLevel;

    @Column(name = "weather_summary")
    private String weatherSummary;

    @Column(name = "news_radar_triggered")
    private Boolean newsRadarTriggered;

    @Column(name = "news_signal_type")
    private String newsSignalType;

    @Column(name = "news_headline_summary", columnDefinition = "TEXT")
    private String newsHeadlineSummary;

    @Column(name = "news_raw_json", columnDefinition = "TEXT")
    private String newsRawJson;

    @Column(name = "news_checked_at")
    private OffsetDateTime newsCheckedAt;

    @Column(name = "odds_quality_grade")
    private String oddsQualityGrade;

    @Column(name = "odds_quality_score", precision = 6, scale = 2)
    private BigDecimal oddsQualityScore;

    @Column(name = "sharp_anchor_odd", precision = 8, scale = 3)
    private BigDecimal sharpAnchorOdd;

    @Column(name = "executable_odd", precision = 8, scale = 3)
    private BigDecimal executableOdd;

    @Column(name = "executable_bookmaker")
    private String executableBookmaker;

    @Column(name = "opening_odd", precision = 8, scale = 3)
    private BigDecimal openingOdd;

    @Column(name = "line_drift_pct", precision = 6, scale = 2)
    private BigDecimal lineDriftPct;

    @Column(name = "sharp_vs_soft_pct", precision = 6, scale = 2)
    private BigDecimal sharpVsSoftPct;

    @Column(name = "odds_quality_summary", columnDefinition = "TEXT")
    private String oddsQualitySummary;

    @Column(name = "target_market")
    private String targetMarket;

    @Column(name = "closing_sharp_odd", precision = 8, scale = 3)
    private BigDecimal closingSharpOdd;

    @Column(name = "closing_captured_at")
    private OffsetDateTime closingCapturedAt;

    @Column(name = "clv_pct", precision = 6, scale = 2)
    private BigDecimal clvPct;

    @Column(name = "home_matches_7d")
    private Integer homeMatches7d;

    @Column(name = "away_matches_7d")
    private Integer awayMatches7d;

    @Column(name = "home_matches_14d")
    private Integer homeMatches14d;

    @Column(name = "away_matches_14d")
    private Integer awayMatches14d;

    @Column(name = "fatigue_diff_7d")
    private Integer fatigueDiff7d;

    @Column(name = "referee_name")
    private String refereeName;

    @Column(name = "referee_avg_yellow_cards", precision = 4, scale = 2)
    private BigDecimal refereeAvgYellowCards;

    @Column(name = "referee_avg_penalties", precision = 4, scale = 2)
    private BigDecimal refereeAvgPenalties;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

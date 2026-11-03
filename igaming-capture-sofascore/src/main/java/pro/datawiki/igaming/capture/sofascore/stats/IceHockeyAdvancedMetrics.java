package pro.datawiki.igaming.capture.sofascore.stats;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Institutional advanced metrics model for Ice Hockey (KHL / NHL).
 * Encompasses 5v5 / all-situations Corsi, Fenwick, High Danger Scoring Chances (HDCF),
 * and Goaltender Goals Saved Above Expected (GSAx).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IceHockeyAdvancedMetrics implements Serializable {

    // ── Corsi (All Shot Attempts = On Goal + Missed + Blocked) ────────────────
    @JsonProperty("home_corsi_for")
    private Double homeCorsiFor;

    @JsonProperty("home_corsi_against")
    private Double homeCorsiAgainst;

    /**
     * Corsi For Percentage: CF / (CF + CA) * 100
     */
    @JsonProperty("home_corsi_pct")
    private Double homeCorsiPct;

    @JsonProperty("away_corsi_for")
    private Double awayCorsiFor;

    @JsonProperty("away_corsi_against")
    private Double awayCorsiAgainst;

    @JsonProperty("away_corsi_pct")
    private Double awayCorsiPct;

    // ── Fenwick (Unblocked Shot Attempts = On Goal + Missed) ──────────────────
    @JsonProperty("home_fenwick_for")
    private Double homeFenwickFor;

    @JsonProperty("home_fenwick_against")
    private Double homeFenwickAgainst;

    /**
     * Fenwick For Percentage: FF / (FF + FA) * 100
     */
    @JsonProperty("home_fenwick_pct")
    private Double homeFenwickPct;

    @JsonProperty("away_fenwick_for")
    private Double awayFenwickFor;

    @JsonProperty("away_fenwick_against")
    private Double awayFenwickAgainst;

    @JsonProperty("away_fenwick_pct")
    private Double awayFenwickPct;

    // ── High Danger Scoring Chances (HDCF / HDCA) ─────────────────────────────
    @JsonProperty("home_hdcf")
    private Double homeHdcf;

    @JsonProperty("home_hdca")
    private Double homeHdca;

    /**
     * High Danger Chances For Percentage: HDCF / (HDCF + HDCA) * 100
     */
    @JsonProperty("home_hdcf_pct")
    private Double homeHdcfPct;

    @JsonProperty("away_hdcf")
    private Double awayHdcf;

    @JsonProperty("away_hdca")
    private Double awayHdca;

    @JsonProperty("away_hdcf_pct")
    private Double awayHdcfPct;

    // ── Goaltender Metrics & GSAx (Goals Saved Above Expected) ───────────────
    @JsonProperty("home_goalie_name")
    private String homeGoalieName;

    @JsonProperty("home_goalie_shots_against")
    private Integer homeGoalieShotsAgainst;

    @JsonProperty("home_goalie_saves")
    private Integer homeGoalieSaves;

    @JsonProperty("home_goalie_goals_against")
    private Double homeGoalieGoalsAgainst;

    @JsonProperty("home_goalie_expected_goals_against")
    private Double homeGoalieExpectedGoalsAgainst;

    /**
     * GSAx = xGA - GA. Positive indicates above-average netminding.
     */
    @JsonProperty("home_goalie_gsax")
    private Double homeGoalieGsax;

    @JsonProperty("away_goalie_name")
    private String awayGoalieName;

    @JsonProperty("away_goalie_shots_against")
    private Integer awayGoalieShotsAgainst;

    @JsonProperty("away_goalie_saves")
    private Integer awayGoalieSaves;

    @JsonProperty("away_goalie_goals_against")
    private Double awayGoalieGoalsAgainst;

    @JsonProperty("away_goalie_expected_goals_against")
    private Double awayGoalieExpectedGoalsAgainst;

    /**
     * GSAx = xGA - GA.
     */
    @JsonProperty("away_goalie_gsax")
    private Double awayGoalieGsax;

    // ── Special Teams Efficiency ──────────────────────────────────────────────
    @JsonProperty("home_power_play_pct")
    private Double homePowerPlayPct;

    @JsonProperty("away_power_play_pct")
    private Double awayPowerPlayPct;

    @JsonProperty("home_penalty_kill_pct")
    private Double homePenaltyKillPct;

    @JsonProperty("away_penalty_kill_pct")
    private Double awayPenaltyKillPct;
}

package pro.datawiki.igaming.analyst.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAnalysisResultDto {

    @JsonProperty("match_id")
    private Long matchId;

    @JsonProperty("expected_xg")
    private String expectedXg;

    @JsonProperty("fair_p1")
    private BigDecimal fairP1;

    @JsonProperty("fair_px")
    private BigDecimal fairPx;

    @JsonProperty("fair_p2")
    private BigDecimal fairP2;

    @JsonProperty("fair_over25")
    private BigDecimal fairOver25;

    @JsonProperty("fair_under25")
    private BigDecimal fairUnder25;

    @JsonProperty("fair_btts_yes")
    private BigDecimal fairBttsYes;

    @JsonProperty("fair_btts_no")
    private BigDecimal fairBttsNo;

    @JsonProperty("recommended_bet")
    @com.fasterxml.jackson.annotation.JsonAlias({"recommended_market", "recommendedMarket", "recommendedBet", "market", "bet", "pick", "recommendedPick", "selection"})
    private String recommendedBet;

    @JsonProperty("market_type")
    @com.fasterxml.jackson.annotation.JsonAlias({"marketType", "type", "market_name"})
    private String marketType;

    @JsonProperty("market_odd")
    @com.fasterxml.jackson.annotation.JsonAlias({"marketOdd", "recommended_odd", "recommendedOdd", "odd", "odds", "price"})
    private BigDecimal marketOdd;

    @JsonProperty("best_bookmaker")
    private String bestBookmaker;

    @JsonProperty("expected_value_pct")
    private BigDecimal expectedValuePct;

    @JsonProperty("kelly_stake_pct")
    private BigDecimal kellyStakePct;

    @JsonProperty("confidence_score")
    private Integer confidenceScore;

    @JsonProperty("tactical_rationale")
    private String tacticalRationale;

    @JsonProperty("key_factors")
    private List<String> keyFactors;

    @JsonProperty("steam_direction")
    private String steamDirection;

    @JsonProperty("lineup_status")
    private String lineupStatus;

    @JsonProperty("core_hypothesis")
    private String coreHypothesis;

    @JsonProperty("identified_risk")
    private String identifiedRisk;

    @JsonProperty("shadow_picks")
    private List<ShadowPickDto> shadowPicks;
}

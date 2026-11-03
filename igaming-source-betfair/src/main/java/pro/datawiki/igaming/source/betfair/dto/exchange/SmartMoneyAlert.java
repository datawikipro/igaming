package pro.datawiki.igaming.source.betfair.dto.exchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Alert published to Kafka topic `odds.smart-money` when an abnormal traded volume surge
 * or institutional steam is detected on the Betfair Exchange.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartMoneyAlert implements Serializable {

    private String alertId;
    @Builder.Default
    private String bookmaker = "betfair";
    @Builder.Default
    private String exchange = "betfair-exchange";
    private String eventId;
    private String matchName;
    private String sport;
    private String league;
    private String marketId;
    private String marketName;
    private String runnerName;
    private String selectionId;

    private Double previousTradedVolume;
    private Double currentTradedVolume;
    private Double volumeDelta;
    private Double volumeGrowthPercent;
    private Double volumeShareOfMarket;

    private Double lastPriceTraded;
    private Double bestBackPrice;
    private Double bestLayPrice;

    private String severity;   // INSTITUTIONAL_LOAD, CRITICAL_STEAM, SIGNIFICANT_INFLOW, VOLUME_SPIKE
    private String direction;  // BACK_STEAM, LAY_PRESSURE, LIQUIDITY_SURGE
    private String detectedAt; // ISO-8601 timestamp string
    private String description;
}

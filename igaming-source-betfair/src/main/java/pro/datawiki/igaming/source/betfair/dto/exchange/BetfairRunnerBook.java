package pro.datawiki.igaming.source.betfair.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the order book and traded volume for a specific runner / outcome in a Betfair Exchange market.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetfairRunnerBook implements Serializable {

    private String selectionId;
    private String runnerName;
    private Double handicap;
    private String status;
    private Double lastPriceTraded;
    private Double totalMatched; // Total traded volume on this runner

    private Double bestBackPrice;
    private Double bestBackSize;
    private Double bestLayPrice;
    private Double bestLaySize;

    @Builder.Default
    private List<PriceSizeLadder> availableToBack = new ArrayList<>();

    @Builder.Default
    private List<PriceSizeLadder> availableToLay = new ArrayList<>();

    @Builder.Default
    private List<PriceSizeLadder> tradedVolumeLadder = new ArrayList<>();
}

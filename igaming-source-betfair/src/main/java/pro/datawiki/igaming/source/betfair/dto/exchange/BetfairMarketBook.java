package pro.datawiki.igaming.source.betfair.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete Betfair Exchange market with its traded volume and runners orderbook.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetfairMarketBook implements Serializable {

    private String marketId;
    private String marketName;
    private String eventId;
    private String matchName;
    private String sport;
    private String league;
    private Long eventDate;
    private Double totalMatched; // Total volume traded on this market
    private Double totalAvailable;
    private String status;
    private boolean isLive;

    @Builder.Default
    private List<BetfairRunnerBook> runners = new ArrayList<>();
}

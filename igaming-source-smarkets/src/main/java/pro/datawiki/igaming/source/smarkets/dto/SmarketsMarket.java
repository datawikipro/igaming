package pro.datawiki.igaming.source.smarkets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmarketsMarket {
    private String id;
    private String name;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("market_type")
    private SmarketsMarketType marketType;

    private String state;
    private String category;
    private String description;
}

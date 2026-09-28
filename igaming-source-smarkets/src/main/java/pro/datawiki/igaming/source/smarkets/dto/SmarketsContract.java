package pro.datawiki.igaming.source.smarkets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmarketsContract {
    private String id;
    private String name;

    @JsonProperty("market_id")
    private String marketId;

    private String state;
    private String slug;

    @JsonProperty("short_name")
    private String shortName;
}

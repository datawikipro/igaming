package pro.datawiki.igaming.source.favbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavbetMarket {
    @JsonProperty("market_id")
    private Long marketId;

    @JsonProperty("market_name")
    private String marketName;

    @JsonProperty("market_type_id")
    private Integer marketTypeId;

    @JsonProperty("market_template_id")
    private Integer marketTemplateId;

    @JsonProperty("result_type_id")
    private Integer resultTypeId;

    @JsonProperty("outcomes")
    private List<FavbetOutcome> outcomes = new ArrayList<>();
}

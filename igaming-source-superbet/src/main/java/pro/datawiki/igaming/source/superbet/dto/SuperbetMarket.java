package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetMarket {
    private Long id;
    private String name;
    private String code;
    @JsonProperty("marketId")
    private Long marketId;
    @JsonProperty("specifiers")
    private String specifiers;
    private List<SuperbetOutcome> outcomes;
}

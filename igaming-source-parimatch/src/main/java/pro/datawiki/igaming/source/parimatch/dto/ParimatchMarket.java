package pro.datawiki.igaming.source.parimatch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParimatchMarket {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("period")
    private String period;

    @JsonProperty("marketType")
    private String marketType;

    @JsonProperty("outcomes")
    private List<ParimatchOutcome> outcomes = new ArrayList<>();
}

package pro.datawiki.igaming.source.sts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StsMarket {
    private Long id;
    private String name;
    private String type;
    @JsonProperty("marketType")
    private String marketType;
    @JsonProperty("param")
    private Double param;
    private List<StsOutcome> outcomes;
}

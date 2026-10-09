package pro.datawiki.igaming.source.fortuna.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FortunaMarket {
    private Long id;
    private String name;
    @JsonProperty("marketType")
    private String marketType;
    @JsonProperty("code")
    private String code;
    @JsonProperty("param")
    private Double param;
    private List<FortunaOutcome> outcomes;
}

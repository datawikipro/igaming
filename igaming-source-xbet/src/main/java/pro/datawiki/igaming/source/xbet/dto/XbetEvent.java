package pro.datawiki.igaming.source.xbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XbetEvent {

    @JsonProperty("T")
    private Integer type;

    @JsonProperty("G")
    private Integer group;

    @JsonProperty("C")
    private Double coefficient;

    @JsonProperty("P")
    private Double parameter;
}

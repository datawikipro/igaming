package pro.datawiki.igaming.source.core.engine.xbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XbetFamilyEvent {

    @JsonProperty("T")
    private Integer type;

    @JsonProperty("G")
    private Integer group;

    @JsonProperty("C")
    private Double coefficient;

    @JsonProperty("P")
    private Double parameter;
}

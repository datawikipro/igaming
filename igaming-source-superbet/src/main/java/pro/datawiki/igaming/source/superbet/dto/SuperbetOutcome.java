package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetOutcome {
    private Long id;
    private String name;
    private String code;
    private Double price;
    private Double value;
    @JsonProperty("specifiers")
    private String specifiers;
}

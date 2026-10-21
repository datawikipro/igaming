package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetOutcome {
    private Long id;
    private String uuid;
    private Double price;
    private Double value;
    private Integer status;
    private Boolean display;
    private SuperbetOutcomeMetadata metadata;

    private String name;
    private String code;
    @JsonProperty("specifiers")
    private Object specifiers;

    public String getResolvedName() {
        if (metadata != null && metadata.getName() != null && !metadata.getName().isBlank()) {
            return metadata.getName();
        }
        return name;
    }

    public String getResolvedCode() {
        if (metadata != null && metadata.getCode() != null && !metadata.getCode().isBlank()) {
            return metadata.getCode();
        }
        return code;
    }

    public String getSpecialBetValue() {
        if (metadata != null && metadata.getSpecialBetValue() != null) {
            return metadata.getSpecialBetValue();
        }
        return null;
    }
}

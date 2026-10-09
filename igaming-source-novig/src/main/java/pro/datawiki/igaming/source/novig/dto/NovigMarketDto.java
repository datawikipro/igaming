package pro.datawiki.igaming.source.novig.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NovigMarketDto {
    private String id;
    private String description;
    private String type;
    private Double strike;
    private String status;
    private List<NovigOutcomeDto> outcomes = new ArrayList<>();
}

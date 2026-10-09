package pro.datawiki.igaming.source.stake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StakeMarketDto {
    private String id;
    private String name;
    private String status;
    private List<StakeOutcomeDto> outcomes;
}

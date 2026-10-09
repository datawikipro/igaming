package pro.datawiki.igaming.source.bcgame.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BcgameMarketDto {
    private String id;
    private String name;
    private String marketType;
    private String status;
    private List<BcgameOutcomeDto> outcomes;
}

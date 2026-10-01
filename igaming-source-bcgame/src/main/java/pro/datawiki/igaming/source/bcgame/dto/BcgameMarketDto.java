package pro.datawiki.igaming.source.bcgame.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BcgameMarketDto {
    private String id;
    private String name;
    private String marketType;
    private String status;
    private List<BcgameOutcomeDto> outcomes = new ArrayList<>();
}

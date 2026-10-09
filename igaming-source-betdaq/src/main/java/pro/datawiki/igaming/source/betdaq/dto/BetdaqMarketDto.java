package pro.datawiki.igaming.source.betdaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetdaqMarketDto {
    private String id;
    private String name;
    private String type;
    private String status;
    private List<BetdaqSelectionDto> selections;
}

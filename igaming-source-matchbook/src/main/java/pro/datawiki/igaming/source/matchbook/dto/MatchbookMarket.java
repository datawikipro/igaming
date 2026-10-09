package pro.datawiki.igaming.source.matchbook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchbookMarket {
    private Long id;
    private String name;

    @JsonProperty("market-type")
    private String marketType;

    private String status;

    @JsonProperty("live")
    private Boolean live;

    private List<MatchbookRunner> runners = new ArrayList<>();
}

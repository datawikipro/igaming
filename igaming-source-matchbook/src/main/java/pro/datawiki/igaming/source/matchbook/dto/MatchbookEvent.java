package pro.datawiki.igaming.source.matchbook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchbookEvent {
    private Long id;
    private String name;

    @JsonProperty("sport-id")
    private Integer sportId;

    private String start;
    private String status;

    @JsonProperty("in-running-flag")
    private Boolean inRunningFlag;

    private List<MatchbookMarket> markets = new ArrayList<>();
}

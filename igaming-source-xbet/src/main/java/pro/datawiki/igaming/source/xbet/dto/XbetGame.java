package pro.datawiki.igaming.source.xbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XbetGame {

    @JsonProperty("I")
    private Long id; // external ID

    @JsonProperty("S")
    private Integer sportId;

    @JsonProperty("SN")
    private String sportName;

    @JsonProperty("L")
    private Integer leagueId;

    @JsonProperty("LN")
    private String leagueName;

    @JsonProperty("O1")
    private String team1;

    @JsonProperty("O2")
    private String team2;

    @JsonProperty("SC")
    private String score;

    @JsonProperty("T")
    private Long startTime; // seconds since epoch

    @JsonProperty("E")
    private List<XbetEvent> events; // Markets/Odds
}

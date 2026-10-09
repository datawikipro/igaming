package pro.datawiki.igaming.source.vbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VbetEvent {
    @JsonProperty("Id")
    private Long id;

    @JsonProperty("N")
    private String name;

    @JsonProperty("HT")
    private String homeTeam;

    @JsonProperty("AT")
    private String awayTeam;

    @JsonProperty("SId")
    private Long sportId;

    @JsonProperty("SN")
    private String sportName;

    @JsonProperty("ESN")
    private String englishSportName;

    @JsonProperty("D")
    private String date;

    @JsonProperty("CId")
    private Long championshipId;

    @JsonProperty("CN")
    private String championshipName;

    @JsonProperty("ECN")
    private String englishChampionshipName;

    @JsonProperty("IsLive")
    private Boolean isLive;

    @JsonProperty("StakeTypes")
    private List<VbetStakeGroup> stakeTypes = new ArrayList<>();
}

package pro.datawiki.igaming.source.parimatch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParimatchEvent {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("sportId")
    private String sportId;

    @JsonProperty("sportName")
    private String sportName;

    @JsonProperty("categoryId")
    private String categoryId;

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("tournamentId")
    private String tournamentId;

    @JsonProperty("tournamentName")
    private String tournamentName;

    @JsonProperty("startDate")
    private Long startDate;

    @JsonProperty("isLive")
    private Boolean isLive;

    @JsonProperty("markets")
    private List<ParimatchMarket> markets = new ArrayList<>();
}

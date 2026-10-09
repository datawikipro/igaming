package pro.datawiki.igaming.source.mozzartbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MozzartEvent {
    private Long id;
    @JsonProperty("matchId")
    private Long matchId;

    private String name;
    @JsonProperty("home")
    private String home;
    @JsonProperty("visitor")
    private String visitor;

    @JsonProperty("homeTeam")
    private String homeTeam;
    @JsonProperty("awayTeam")
    private String awayTeam;

    @JsonProperty("sportId")
    private Long sportId;
    @JsonProperty("sportName")
    private String sportName;

    @JsonProperty("competitionId")
    private Long competitionId;
    @JsonProperty("competitionName")
    private String competitionName;

    @JsonProperty("startTime")
    private Long startTime;
    @JsonProperty("startDate")
    private Long startDate;

    @JsonProperty("live")
    private Boolean live;
    @JsonProperty("isLive")
    private Boolean isLive;

    @JsonProperty("markets")
    private List<MozzartMarket> markets;
    @JsonProperty("subgames")
    private List<MozzartMarket> subgames;

    public Long getResolvedId() {
        if (id != null) return id;
        return matchId;
    }

    public String getResolvedName() {
        if (name != null && !name.isBlank()) return name;
        if (home != null && visitor != null) return home + " - " + visitor;
        if (homeTeam != null && awayTeam != null) return homeTeam + " - " + awayTeam;
        return null;
    }

    public Long getResolvedStartTime() {
        if (startTime != null && startTime > 0) return startTime;
        if (startDate != null && startDate > 0) return startDate;
        return System.currentTimeMillis();
    }

    public List<MozzartMarket> getResolvedMarkets() {
        if (markets != null && !markets.isEmpty()) return markets;
        return subgames;
    }
}

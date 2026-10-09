package pro.datawiki.igaming.source.tipsport.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipsportEvent {
    private Long id;
    @JsonProperty("matchId")
    private Long matchId;

    private String name;
    @JsonProperty("matchName")
    private String matchName;

    @JsonProperty("homeTeamName")
    private String homeTeamName;
    @JsonProperty("awayTeamName")
    private String awayTeamName;

    @JsonProperty("sport")
    private String sport;
    @JsonProperty("sportName")
    private String sportName;

    @JsonProperty("competition")
    private String competition;
    @JsonProperty("competitionName")
    private String competitionName;

    @JsonProperty("dateTime")
    private Long dateTime;
    @JsonProperty("matchStart")
    private Long matchStart;

    @JsonProperty("live")
    private Boolean live;
    @JsonProperty("isLive")
    private Boolean isLive;

    @JsonProperty("markets")
    private List<TipsportMarket> markets;
    @JsonProperty("tables")
    private List<TipsportMarket> tables;

    public Long getResolvedId() {
        if (id != null) return id;
        return matchId;
    }

    public String getResolvedName() {
        if (name != null && !name.isBlank()) return name;
        if (matchName != null && !matchName.isBlank()) return matchName;
        if (homeTeamName != null && awayTeamName != null) return homeTeamName + " - " + awayTeamName;
        return null;
    }

    public Long getResolvedStartTime() {
        if (dateTime != null && dateTime > 0) return dateTime;
        if (matchStart != null && matchStart > 0) return matchStart;
        return System.currentTimeMillis();
    }

    public List<TipsportMarket> getResolvedMarkets() {
        if (markets != null && !markets.isEmpty()) return markets;
        return tables;
    }
}

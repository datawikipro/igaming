package pro.datawiki.igaming.source.sts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StsEvent {
    private Long id;
    @JsonProperty("eventId")
    private Long eventId;

    private String name;
    @JsonProperty("title")
    private String title;

    @JsonProperty("homeTeam")
    private String homeTeam;
    @JsonProperty("awayTeam")
    private String awayTeam;

    @JsonProperty("sportId")
    private Long sportId;
    @JsonProperty("sportName")
    private String sportName;

    @JsonProperty("leagueId")
    private Long leagueId;
    @JsonProperty("leagueName")
    private String leagueName;

    @JsonProperty("startDate")
    private Long startDate;
    @JsonProperty("startTime")
    private Long startTime;

    @JsonProperty("isLive")
    private Boolean isLive;
    @JsonProperty("live")
    private Boolean live;

    private List<StsMarket> markets;

    public Long getResolvedId() {
        if (id != null) return id;
        return eventId;
    }

    public String getResolvedName() {
        if (name != null && !name.isBlank()) return name;
        if (title != null && !title.isBlank()) return title;
        if (homeTeam != null && awayTeam != null) return homeTeam + " - " + awayTeam;
        return null;
    }

    public Long getResolvedStartTime() {
        if (startDate != null && startDate > 0) return startDate;
        if (startTime != null && startTime > 0) return startTime;
        return System.currentTimeMillis();
    }
}

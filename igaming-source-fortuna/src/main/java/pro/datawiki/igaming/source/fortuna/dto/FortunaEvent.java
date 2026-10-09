package pro.datawiki.igaming.source.fortuna.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FortunaEvent {
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

    @JsonProperty("tournamentId")
    private Long tournamentId;
    @JsonProperty("tournamentName")
    private String tournamentName;

    @JsonProperty("matchDate")
    private Long matchDate;
    @JsonProperty("startTime")
    private Long startTime;

    @JsonProperty("live")
    private Boolean live;
    @JsonProperty("isLive")
    private Boolean isLive;

    private List<FortunaMarket> markets;

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
        if (matchDate != null && matchDate > 0) return matchDate;
        if (startTime != null && startTime > 0) return startTime;
        return System.currentTimeMillis();
    }
}

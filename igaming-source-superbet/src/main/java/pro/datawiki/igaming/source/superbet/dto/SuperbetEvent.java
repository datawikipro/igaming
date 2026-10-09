package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetEvent {
    @JsonProperty("eventId")
    private Long eventId;
    private Long id;

    @JsonProperty("eventName")
    private String eventName;
    private String name;

    @JsonProperty("matchName")
    private String matchName;

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

    @JsonProperty("eventDate")
    private String eventDate;
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("matchDate")
    private Long matchDate;

    @JsonProperty("live")
    private Boolean live;
    @JsonProperty("isLive")
    private Boolean isLive;

    private List<SuperbetMarket> markets;
    private List<SuperbetOutcome> odds;

    public Long getResolvedId() {
        if (eventId != null) return eventId;
        return id;
    }

    public String getResolvedName() {
        if (matchName != null && !matchName.isBlank()) return matchName;
        if (eventName != null && !eventName.isBlank()) return eventName;
        if (name != null && !name.isBlank()) return name;
        if (homeTeam != null && awayTeam != null) return homeTeam + " - " + awayTeam;
        return null;
    }
}

package pro.datawiki.igaming.source.betvictor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetvictorEvent {
    private Long id;
    
    @JsonProperty("event_id")
    private Long eventId;

    private String title;
    private String description;
    private String name;

    @JsonProperty("sport_id")
    private Integer sportId;

    @JsonProperty("sport_name")
    private String sportName;

    @JsonProperty("meeting_name")
    private String meetingName;

    @JsonProperty("competition_name")
    private String competitionName;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("start_time_epoch")
    private Long startTimeEpoch;

    @JsonProperty("is_live")
    private Boolean isLive;

    @JsonProperty("in_play")
    private Boolean inPlay;

    private String status;

    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    @JsonProperty("markets")
    private List<BetvictorMarket> markets;

    public Long getResolvedId() {
        if (id != null) return id;
        return eventId;
    }

    public String getResolvedName() {
        if (title != null && !title.isBlank()) return title;
        if (description != null && !description.isBlank()) return description;
        if (name != null && !name.isBlank()) return name;
        if (homeTeam != null && awayTeam != null) return homeTeam + " vs " + awayTeam;
        return "";
    }

    public String getResolvedLeague() {
        if (competitionName != null && !competitionName.isBlank()) return competitionName;
        if (meetingName != null && !meetingName.isBlank()) return meetingName;
        return "General";
    }

    public Boolean getResolvedIsLive() {
        if (isLive != null) return isLive;
        if (inPlay != null) return inPlay;
        return Boolean.FALSE;
    }
}

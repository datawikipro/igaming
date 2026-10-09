package pro.datawiki.igaming.source.betano.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetanoEvent {
    private String id;
    
    @JsonProperty("eventId")
    private String eventId;

    private String name;
    
    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("title")
    private String title;

    @JsonProperty("startTime")
    private Long startTime;

    @JsonProperty("startTimeEpoch")
    private Long startTimeEpoch;

    @JsonProperty("sportId")
    private String sportId;

    @JsonProperty("sportName")
    private String sportName;

    @JsonProperty("regionName")
    private String regionName;

    @JsonProperty("leagueName")
    private String leagueName;

    @JsonProperty("tournamentName")
    private String tournamentName;

    @JsonProperty("isLive")
    private Boolean isLive;

    @JsonProperty("live")
    private Boolean live;

    @JsonProperty("markets")
    private List<BetanoMarket> markets;

    public String getResolvedId() {
        if (id != null) return id;
        return eventId;
    }

    public String getResolvedName() {
        if (shortName != null && !shortName.isBlank()) return shortName;
        if (name != null && !name.isBlank()) return name;
        if (title != null && !title.isBlank()) return title;
        return "";
    }

    public String getResolvedLeague() {
        if (leagueName != null && !leagueName.isBlank()) return leagueName;
        if (tournamentName != null && !tournamentName.isBlank()) return tournamentName;
        if (regionName != null && !regionName.isBlank()) return regionName;
        return "General";
    }

    public String getResolvedSport() {
        if (sportName != null && !sportName.isBlank()) return sportName;
        if (sportId != null && !sportId.isBlank()) return sportId;
        return "Soccer";
    }

    public Boolean getResolvedIsLive() {
        if (isLive != null) return isLive;
        if (live != null) return live;
        return Boolean.FALSE;
    }

    public Long getResolvedStartTime() {
        if (startTime != null) return startTime;
        if (startTimeEpoch != null) return startTimeEpoch;
        return System.currentTimeMillis();
    }
}

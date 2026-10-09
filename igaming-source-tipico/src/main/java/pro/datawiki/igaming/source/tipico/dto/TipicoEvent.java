package pro.datawiki.igaming.source.tipico.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipicoEvent {
    private Long id;
    
    @JsonProperty("eventId")
    private Long eventId;

    private String name;
    private String title;
    
    @JsonProperty("matchName")
    private String matchName;

    @JsonProperty("sport")
    private String sport;

    @JsonProperty("sportName")
    private String sportName;

    @JsonProperty("competition")
    private String competition;

    @JsonProperty("competitionName")
    private String competitionName;

    @JsonProperty("leagueName")
    private String leagueName;

    @JsonProperty("eventTime")
    private String eventTime;

    @JsonProperty("startTime")
    private String startTime;

    @JsonProperty("startTimeEpoch")
    private Long startTimeEpoch;

    @JsonProperty("isLive")
    private Boolean isLive;

    @JsonProperty("live")
    private Boolean live;

    @JsonProperty("team1")
    private String team1;

    @JsonProperty("team2")
    private String team2;

    @JsonProperty("homeTeam")
    private String homeTeam;

    @JsonProperty("awayTeam")
    private String awayTeam;

    @JsonProperty("markets")
    private List<TipicoMarket> markets;

    public Long getResolvedId() {
        if (id != null) return id;
        return eventId;
    }

    public String getResolvedName() {
        if (matchName != null && !matchName.isBlank()) return matchName;
        if (name != null && !name.isBlank()) return name;
        if (title != null && !title.isBlank()) return title;
        String h = getResolvedHomeTeam();
        String a = getResolvedAwayTeam();
        if (h != null && a != null) return h + " vs " + a;
        return "";
    }

    public String getResolvedHomeTeam() {
        if (team1 != null && !team1.isBlank()) return team1;
        if (homeTeam != null && !homeTeam.isBlank()) return homeTeam;
        return null;
    }

    public String getResolvedAwayTeam() {
        if (team2 != null && !team2.isBlank()) return team2;
        if (awayTeam != null && !awayTeam.isBlank()) return awayTeam;
        return null;
    }

    public String getResolvedSport() {
        if (sportName != null && !sportName.isBlank()) return sportName;
        if (sport != null && !sport.isBlank()) return sport;
        return "Soccer";
    }

    public String getResolvedLeague() {
        if (competitionName != null && !competitionName.isBlank()) return competitionName;
        if (competition != null && !competition.isBlank()) return competition;
        if (leagueName != null && !leagueName.isBlank()) return leagueName;
        return "General";
    }

    public Boolean getResolvedIsLive() {
        if (isLive != null) return isLive;
        if (live != null) return live;
        return Boolean.FALSE;
    }
}

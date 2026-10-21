package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetEvent {
    @JsonProperty("event_id")
    private Long eventId;
    private Long id;

    private SuperbetFixture fixture;

    @JsonProperty("inplay_stats_metadata")
    private SuperbetInPlayMetadata inplayStatsMetadata;

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
        if (fixture != null && fixture.getEventName() != null && !fixture.getEventName().isBlank()) {
            return fixture.getEventName();
        }
        if (matchName != null && !matchName.isBlank()) return matchName;
        if (eventName != null && !eventName.isBlank()) return eventName;
        if (name != null && !name.isBlank()) return name;
        if (homeTeam != null && awayTeam != null) return homeTeam + " - " + awayTeam;
        return null;
    }

    public String getResolvedSportName() {
        if (sportName != null && !sportName.isBlank()) {
            return sportName;
        }
        Long sId = (fixture != null && fixture.getSportId() != null) ? fixture.getSportId() : sportId;
        if (sId == null) return "Soccer";
        if (sId == 5L || sId == 1L) return "Soccer";
        if (sId == 4L) return "Basketball";
        if (sId == 2L) return "Tennis";
        if (sId == 3L) return "Ice Hockey";
        if (sId == 6L) return "Handball";
        if (sId == 8L) return "Volleyball";
        if (sId == 9L) return "Table Tennis";
        if (sId == 11L) return "Baseball";
        if (sId == 12L) return "American Football";
        if (sId == 21L) return "MMA";
        if (sId == 22L) return "Darts";
        if (sId == 40L) return "Esports";
        return "Soccer";
    }

    public Long getResolvedMatchDate() {
        if (fixture != null && fixture.getUnixDateMillis() != null && fixture.getUnixDateMillis() > 0) {
            return fixture.getUnixDateMillis();
        }
        if (matchDate != null && matchDate > 0) {
            return matchDate;
        }
        return null;
    }

    public boolean isMatchLive() {
        if (Boolean.TRUE.equals(isLive) || Boolean.TRUE.equals(live)) {
            return true;
        }
        if (inplayStatsMetadata != null && inplayStatsMetadata.getStatus() != null) {
            String status = inplayStatsMetadata.getStatus().toUpperCase();
            return !status.contains("NOT_STARTED") && !status.contains("FINISHED") && !status.contains("CANCELLED");
        }
        return false;
    }
}

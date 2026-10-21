package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetFixture {
    @JsonProperty("event_name")
    private String eventName;
    @JsonProperty("sport_id")
    private Long sportId;
    @JsonProperty("tournament_id")
    private Long tournamentId;
    @JsonProperty("utc_date")
    private String utcDate;
    @JsonProperty("unix_date_millis")
    private Long unixDateMillis;
    @JsonProperty("home_team_id")
    private String homeTeamId;
    @JsonProperty("away_team_id")
    private String awayTeamId;
}

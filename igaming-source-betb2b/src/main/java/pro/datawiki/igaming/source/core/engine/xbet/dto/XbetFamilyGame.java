package pro.datawiki.igaming.source.core.engine.xbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XbetFamilyGame {

    @JsonProperty("I")
    private Long id; // external ID

    @JsonProperty("SI")
    private Integer sportIdNew;

    @JsonProperty("S")
    private JsonNode sportIdOrStartTime; // S is sportId in legacy, startTime in modern

    @JsonProperty("SN")
    private String sportName;

    @JsonProperty("LI")
    private Integer leagueIdNew;

    @JsonProperty("L")
    private JsonNode leagueIdOrName; // L is leagueId in legacy, leagueName in modern

    @JsonProperty("LN")
    private String leagueNameOld;

    @JsonProperty("O1")
    private String team1;

    @JsonProperty("O2")
    private String team2;

    @JsonProperty("SC")
    private JsonNode score; // Changed to JsonNode to handle legacy String or modern Object

    @JsonProperty("T")
    private Long startTimeOld; // T is startTime in legacy

    @JsonProperty("E")
    private List<XbetFamilyEvent> events; // Markets/Odds

    public Integer getSportId() {
        if (sportIdNew != null) {
            return sportIdNew;
        }
        if (sportIdOrStartTime != null && sportIdOrStartTime.isNumber()) {
            long val = sportIdOrStartTime.asLong();
            if (val < 1000000) {
                return (int) val;
            }
        }
        return null;
    }

    public Long getStartTime() {
        if (sportIdOrStartTime != null && sportIdOrStartTime.isNumber()) {
            long val = sportIdOrStartTime.asLong();
            if (val >= 1000000) {
                return val;
            }
        }
        return startTimeOld;
    }

    public Integer getLeagueId() {
        if (leagueIdNew != null) {
            return leagueIdNew;
        }
        if (leagueIdOrName != null && leagueIdOrName.isNumber()) {
            return leagueIdOrName.asInt();
        }
        return null;
    }

    public String getLeagueName() {
        if (leagueIdOrName != null && leagueIdOrName.isTextual()) {
            return leagueIdOrName.asText();
        }
        return leagueNameOld;
    }
}

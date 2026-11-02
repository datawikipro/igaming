package pro.datawiki.igaming.capture.esports.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Structured payload for Valorant matches.
 * Contains agent compositions per map, combat K/D/A, and Econ rating.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValorantMatchPayloadDto implements Serializable {

    private String matchId;
    private String tournamentName;
    private String team1;
    private String team2;
    private Integer team1Score;
    private Integer team2Score;
    private String status;

    private List<ValorantMapDto> maps;
    private List<ValorantPlayerStatDto> players;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValorantMapDto implements Serializable {
        private String mapName; // Ascent, Bind, Haven, Split, Breeze, Lotus, Sunset, Abyss
        private Integer team1Score;
        private Integer team2Score;
        private List<String> team1Agents;
        private List<String> team2Agents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValorantPlayerStatDto implements Serializable {
        private String nickname;
        private String team;
        private String agent;
        private Integer kills;
        private Integer deaths;
        private Integer assists;
        private Double combatScore;
        private Double econRating;
        private Integer firstBloods;
        private Integer plants;
        private Integer defuses;
    }
}

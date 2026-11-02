package pro.datawiki.igaming.capture.esports.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Detailed structured payload for CS2 matches.
 * Encapsulates map veto, rosters, player ratings (Rating 2.0, ADR, KAST),
 * round-by-round CT/T details, economy, and clutches.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cs2MatchPayloadDto implements Serializable {

    private Long matchId;
    private String tournamentTier; // TIER_1, TIER_2, TIER_3, TIER_4
    private String tournamentName;
    private String team1;
    private String team2;
    private Integer team1Score;
    private Integer team2Score;
    private String matchStatus; // UPCOMING, LIVE, FINISHED

    private List<VetoActionDto> vetoActions;
    private List<MapDetailDto> maps;
    private List<PlayerStatDto> players;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VetoActionDto implements Serializable {
        private String team; // team1 or team2
        private String mapName; // Mirage, Inferno, Nuke, Ancient, Anubis, Dust2
        private String action; // REMOVED, PICKED, LEFT_OVER
        private Integer order;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MapDetailDto implements Serializable {
        private String mapName;
        private Integer team1Score;
        private Integer team2Score;
        private Integer team1CtScore;
        private Integer team1TScore;
        private Integer team2CtScore;
        private Integer team2TScore;
        private Integer team1OtScore;
        private Integer team2OtScore;
        private Integer team1PistolWins;
        private Integer team2PistolWins;
        private String pickTeam;
        private Integer durationSeconds;
        private List<RoundDetailDto> rounds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoundDetailDto implements Serializable {
        private Integer roundNumber;
        private String winningTeam;
        private String winType; // BOMB_DEFUSED, TARGET_BOMBED, CT_ELIMINATION, T_ELIMINATION
        private String team1BuyType; // FULL_BUY, FORCE_BUY, ECO, SEMI_BUY
        private String team2BuyType;
        private Boolean isClutch;
        private String clutchPlayer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerStatDto implements Serializable {
        private String nickname;
        private String realName;
        private String team;
        private Double rating20; // Rating 2.0 (e.g. 1.28)
        private Double adr; // Average Damage per Round (e.g. 84.5)
        private Double kastPct; // KAST % (e.g. 74.2)
        private Integer kills;
        private Integer deaths;
        private Integer assists;
        private Double headshotPct;
        private Integer firstKills;
        private Integer firstDeaths;
        private Integer clutchesWon;
    }
}

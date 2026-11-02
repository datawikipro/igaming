package pro.datawiki.igaming.capture.esports.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Structured payload for Dota 2 matches.
 * Contains hero drafts, radiant/dire team stats, minute-by-minute Net Worth / XP graphs,
 * item purchase timings, and combat metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dota2MatchPayloadDto implements Serializable {

    private Long matchId;
    private Long leagueId;
    private String leagueName;
    private String radiantTeam;
    private String direTeam;
    private Boolean radiantWin;
    private Integer durationSeconds;
    private Integer radiantScore;
    private Integer direScore;

    private List<HeroDraftDto> drafts;
    private List<Integer> radiantGoldAdvTimeline;
    private List<Integer> radiantXpAdvTimeline;
    private List<ItemTimingDto> itemTimings;
    private List<Dota2PlayerStatDto> players;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeroDraftDto implements Serializable {
        private Integer heroId;
        private String heroName;
        private Boolean isPick; // true = pick, false = ban
        private Boolean isRadiant; // true = radiant, false = dire
        private Integer order; // 0 to 23
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemTimingDto implements Serializable {
        private String playerHero;
        private String team;
        private String itemName; // e.g. "black_king_bar", "blink", "aghanims_shard"
        private Integer timestampSec; // match time when item was completed
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dota2PlayerStatDto implements Serializable {
        private String nickname;
        private String team;
        private String heroName;
        private Integer kills;
        private Integer deaths;
        private Integer assists;
        private Integer gpm;
        private Integer xpm;
        private Integer heroDamage;
        private Integer towerDamage;
        private Integer lastHits;
        private Integer denies;
    }
}

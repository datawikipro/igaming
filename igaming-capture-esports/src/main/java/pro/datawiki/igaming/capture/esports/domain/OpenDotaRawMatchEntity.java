package pro.datawiki.igaming.capture.esports.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Dedicated persistent raw entity for OpenDota matches.
 * Stores hero drafts (picks/bans), minute-by-minute Net Worth / XP advantage,
 * item purchase timings, and player combat stats for ML training and retroactive analysis.
 */
@Entity
@Table(name = "opendota_raw_matches", indexes = {
        @Index(name = "idx_opendota_raw_league", columnList = "league_id, match_date"),
        @Index(name = "idx_opendota_raw_teams", columnList = "radiant_team, dire_team")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenDotaRawMatchEntity {

    @Id
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "league_id")
    private Long leagueId;

    @Column(name = "league_name", length = 256)
    private String leagueName;

    @Column(name = "radiant_team", length = 128)
    private String radiantTeam;

    @Column(name = "dire_team", length = 128)
    private String direTeam;

    @Column(name = "radiant_win")
    private Boolean radiantWin;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "draft_json", columnDefinition = "TEXT")
    private String draftJson;

    @Column(name = "networth_adv_json", columnDefinition = "TEXT")
    private String networthAdvJson;

    @Column(name = "xp_adv_json", columnDefinition = "TEXT")
    private String xpAdvJson;

    @Column(name = "item_timings_json", columnDefinition = "TEXT")
    private String itemTimingsJson;

    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

    @Column(name = "match_date")
    private Long matchDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onPrePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }
}

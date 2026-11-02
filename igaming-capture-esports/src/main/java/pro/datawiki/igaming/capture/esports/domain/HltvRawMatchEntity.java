package pro.datawiki.igaming.capture.esports.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Dedicated persistent raw entity for HLTV CS2 matches.
 * Preserves complete raw responses for retrospective analytics:
 * Tier-1 to Tier-4 tournaments, rosters, player ratings 2.0 / ADR,
 * map veto, and round-by-round CT/T economy and clutches.
 */
@Entity
@Table(name = "hltv_raw_matches", indexes = {
        @Index(name = "idx_hltv_raw_tier_date", columnList = "tournament_tier, match_date"),
        @Index(name = "idx_hltv_raw_teams", columnList = "team1, team2")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HltvRawMatchEntity {

    @Id
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "tournament_tier", length = 32)
    private String tournamentTier; // TIER_1, TIER_2, TIER_3, TIER_4

    @Column(name = "tournament_name", length = 256)
    private String tournamentName;

    @Column(name = "team1", length = 128)
    private String team1;

    @Column(name = "team2", length = 128)
    private String team2;

    @Column(name = "match_date")
    private Long matchDate;

    @Column(name = "veto_json", columnDefinition = "TEXT")
    private String vetoJson;

    @Column(name = "rosters_json", columnDefinition = "TEXT")
    private String rostersJson;

    @Column(name = "maps_json", columnDefinition = "TEXT")
    private String mapsJson;

    @Column(name = "rounds_json", columnDefinition = "TEXT")
    private String roundsJson;

    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

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

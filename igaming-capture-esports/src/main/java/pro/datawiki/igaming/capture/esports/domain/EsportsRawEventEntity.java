package pro.datawiki.igaming.capture.esports.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Persistent raw Esports event record (CS2, Dota 2, Valorant).
 * Stores raw payload JSON containing map veto, player ratings (Rating 2.0, ADR, KAST),
 * rounds, economy, hero drafts, networth graphs, and agent compositions.
 * Supports Top-Down MDM Blueprint ingestion and retroactive backfilling.
 */
@Entity
@Table(name = "esports_raw_events", indexes = {
        @Index(name = "idx_esports_raw_game_status", columnList = "game, status"),
        @Index(name = "idx_esports_raw_mdm_status", columnList = "mdm_status"),
        @Index(name = "idx_esports_raw_teams", columnList = "team1_raw, team2_raw"),
        @Index(name = "idx_esports_raw_date", columnList = "match_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsportsRawEventEntity {

    @Id
    @Column(name = "event_id", length = 128, nullable = false)
    private String eventId;

    @Column(name = "game", length = 32, nullable = false)
    private String game; // CS2, DOTA2, VALORANT

    @Column(name = "tournament_raw", length = 256)
    private String tournamentRaw;

    @Column(name = "team1_raw", length = 128)
    private String team1Raw;

    @Column(name = "team2_raw", length = 128)
    private String team2Raw;

    @Column(name = "match_date")
    private Long matchDate; // Epoch milliseconds

    @Column(name = "status", length = 32)
    private String status; // UPCOMING, LIVE, FINISHED, CANCELLED

    @Column(name = "mdm_match_id")
    private Long mdmMatchId;

    @Column(name = "mdm_home_team_id")
    private Long mdmHomeTeamId;

    @Column(name = "mdm_away_team_id")
    private Long mdmAwayTeamId;

    @Column(name = "mdm_status", length = 32, nullable = false)
    @Builder.Default
    private String mdmStatus = "UNMAPPED"; // MAPPED / UNMAPPED

    @Column(name = "raw_payload_json", columnDefinition = "TEXT")
    private String rawPayloadJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onPrePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (mdmStatus == null) {
            mdmStatus = "UNMAPPED";
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }
}

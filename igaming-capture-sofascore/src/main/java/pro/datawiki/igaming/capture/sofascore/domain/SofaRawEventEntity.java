package pro.datawiki.igaming.capture.sofascore.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Persistent raw SofaScore event record.
 * Stores original JSON payloads (event details, lineups, H2H, streaks)
 * to support retroactive analytics, out-of-order backfilling, and event sourcing.
 */
@Entity
@Table(name = "sofascore_raw_events", indexes = {
        @Index(name = "idx_sofa_raw_sport_date", columnList = "sport, matchDate"),
        @Index(name = "idx_sofa_raw_teams", columnList = "homeTeam, awayTeam")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SofaRawEventEntity {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "sport", length = 32)
    @Builder.Default
    private String sport = "football";

    @Column(name = "tournament", length = 128)
    private String tournament;

    @Column(name = "season", length = 32)
    private String season;

    @Column(name = "match_date")
    private Long matchDate; // Epoch millis

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "home_team", length = 128)
    private String homeTeam;

    @Column(name = "away_team", length = 128)
    private String awayTeam;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "event_json", columnDefinition = "TEXT")
    private String eventJson;

    @Column(name = "lineups_json", columnDefinition = "TEXT")
    private String lineupsJson;

    @Column(name = "h2h_json", columnDefinition = "TEXT")
    private String h2hJson;

    @Column(name = "streaks_json", columnDefinition = "TEXT")
    private String streaksJson;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;
}

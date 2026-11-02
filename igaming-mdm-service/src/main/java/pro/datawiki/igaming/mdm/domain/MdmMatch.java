package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Master Data Management (MDM) Match Entity.
 * Guarantees cross-sport integrity via composite foreign keys.
 * Acts as the authoritative Golden Record for all matches across bookmakers and SofaScore.
 */
@Entity
@Table(name = "mdm_match", indexes = {
        @Index(name = "idx_mdm_match_sofascore", columnList = "sofascore_id"),
        @Index(name = "idx_mdm_match_teams_time", columnList = "sport_id, home_team_id, away_team_id, start_time"),
        @Index(name = "idx_mdm_match_reverse_teams_time", columnList = "sport_id, away_team_id, home_team_id, start_time"),
        @Index(name = "idx_mdm_match_active", columnList = "status, start_time"),
        @Index(name = "idx_mdm_match_tourn_time", columnList = "tournament_id, start_time")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private MdmSport sport;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private MdmTournament tournament;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private MdmTeam homeTeam;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private MdmTeam awayTeam;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "sofascore_id", unique = true)
    private Long sofascoreId;

    @Builder.Default
    @Column(name = "source_origin", nullable = false, length = 32)
    private String sourceOrigin = "SOFASCORE"; // 'SOFASCORE', 'BOOKMAKER', 'OPERATOR'

    @Column(name = "created_by_source", nullable = false, length = 64)
    private String createdBySource;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 32)
    private String status = "SCHEDULED";

    @Column(name = "score1", length = 16)
    private String score1;

    @Column(name = "score2", length = 16)
    private String score2;

    @Column(name = "regular_score1", length = 16)
    private String regularScore1;

    @Column(name = "regular_score2", length = 16)
    private String regularScore2;

    @Column(name = "overtime_score1", length = 16)
    private String overtimeScore1;

    @Column(name = "overtime_score2", length = 16)
    private String overtimeScore2;

    @Column(name = "penalties_score1", length = 16)
    private String penaltiesScore1;

    @Column(name = "penalties_score2", length = 16)
    private String penaltiesScore2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private MdmSeasonStage stageEntity;

    @Builder.Default
    @Column(name = "best_of", nullable = false)
    private Integer bestOf = 3;

    @Column(name = "stage", length = 64)
    private String stage;

    @Column(name = "round", length = 64)
    private String round;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

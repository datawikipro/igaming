package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Registry of participating teams for a tournament / league season.
 * Enforces strict roster containment so low-tier or cross-country clubs cannot leak into top-tier leagues.
 */
@Entity
@Table(name = "mdm_tournament_team", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mdm_tournament_team", columnNames = {"tournament_id", "team_id"})
}, indexes = {
        @Index(name = "idx_mdm_tourn_team_lookup", columnList = "tournament_id, team_id"),
        @Index(name = "idx_mdm_team_tourn_lookup", columnList = "team_id, tournament_id"),
        @Index(name = "idx_mdm_tourn_team_active", columnList = "tournament_id, is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTournamentTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private MdmTournament tournament;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private MdmTeam team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private MdmSport sport;

    @Column(length = 32)
    private String season;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Master Data Management (MDM) Season Entity.
 * Represents a specific season/iteration of a tournament or league (e.g. "IEM Beijing 2026", "Season 21", "2026/2027").
 */
@Entity
@Table(name = "mdm_season", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mdm_season_tourn_slug", columnNames = {"tournament_id", "slug"}),
        @UniqueConstraint(name = "uk_mdm_season_id_sport", columnNames = {"id", "sport_id"})
}, indexes = {
        @Index(name = "idx_mdm_season_tourn", columnList = "tournament_id"),
        @Index(name = "idx_mdm_season_active", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmSeason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private MdmTournament tournament;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private MdmSport sport;

    @Column(nullable = false, length = 191)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column
    private Integer year;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Builder.Default
    @Column(nullable = false, length = 32)
    private String status = "ACTIVE"; // ACTIVE, FINISHED, UPCOMING

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

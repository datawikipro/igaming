package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Master Data Management (MDM) Season Stage Entity.
 * Represents a distinct competitive stage/bracket within a season (e.g. "Swiss Stage", "Upper Bracket", "Playoffs", "Grand Final").
 * Encapsulates tournament format (SWISS_SYSTEM, DOUBLE_ELIMINATION, SINGLE_ELIMINATION, GSL_GROUPS, ROUND_ROBIN).
 */
@Entity
@Table(name = "mdm_season_stage", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mdm_stage_season_slug", columnNames = {"season_id", "slug"}),
        @UniqueConstraint(name = "uk_mdm_stage_id_sport", columnNames = {"id", "sport_id"})
}, indexes = {
        @Index(name = "idx_mdm_stage_season", columnList = "season_id"),
        @Index(name = "idx_mdm_stage_type", columnList = "stage_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmSeasonStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private MdmSeason season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private MdmSport sport;

    @Column(nullable = false, length = 191)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "stage_type", nullable = false, length = 64)
    private String stageType = "SINGLE_ELIMINATION"; // DOUBLE_ELIMINATION, SWISS_SYSTEM, GSL_GROUPS, SINGLE_ELIMINATION, ROUND_ROBIN

    @Builder.Default
    @Column(name = "default_best_of", nullable = false)
    private Integer defaultBestOf = 3;

    @Builder.Default
    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder = 1;

    @Builder.Default
    @Column(name = "is_playoff", nullable = false)
    private Boolean isPlayoff = false;

    @Builder.Default
    @Column(name = "is_grand_final", nullable = false)
    private Boolean isGrandFinal = false;

    @Column(name = "bracket_meta_json", columnDefinition = "jsonb")
    private String bracketMetaJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

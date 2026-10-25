package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_tournament", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_tournament_sport_cat_slug", columnNames = {"sport_id", "category_id", "slug"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private MdmSport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MdmCategory category;

    @Column(nullable = false, length = 191)
    private String slug;

    @Column(name = "sofascore_id", unique = true)
    private Long sofascoreId;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Builder.Default
    @Column(nullable = false)
    private Integer tier = 3;

    @Builder.Default
    @Column(nullable = false, length = 16)
    private String gender = "MALE";

    @Builder.Default
    @Column(name = "age_category", nullable = false, length = 16)
    private String ageCategory = "SENIOR";

    @Builder.Default
    @Column(name = "is_cup", nullable = false)
    private Boolean isCup = false;

    @Builder.Default
    @Column(name = "popularity_rank", nullable = false)
    private Integer popularityRank = 1000;

    @Builder.Default
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Builder.Default
    @Column(name = "source_origin", nullable = false, length = 32)
    private String sourceOrigin = "SOFASCORE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

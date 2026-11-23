package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_team", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_team_sport_slug", columnNames = {"sport_id", "slug"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTeam {

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
    @Column(name = "is_national", nullable = false)
    private Boolean isNational = false;

    @Builder.Default
    @Column(name = "is_individual", nullable = false)
    private Boolean isIndividual = false;

    @Builder.Default
    @Column(nullable = false, length = 16)
    private String gender = "MALE";

    @Builder.Default
    @Column(name = "age_category", nullable = false, length = 16)
    private String ageCategory = "SENIOR";

    @Column(name = "normalized_signature", nullable = false)
    private String normalizedSignature;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Builder.Default
    @jakarta.persistence.Transient
    private String logoSource = "OFFICIAL";

    @Builder.Default
    @jakarta.persistence.Transient
    private Boolean isOfficialLogo = true;

    /**
     * Authoritative country reference via FK to mdm_country.
     * Null for stateless/virtual esports teams or unknown origin.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private MdmCountry country;

    /**
     * Denormalized country name as read-cache / fallback.
     * Authoritative field is country_id.
     */
    @Column(name = "country_name", length = 128)
    private String countryName;

    @Builder.Default
    @Column(name = "popularity_rank", nullable = false)
    private Integer popularityRank = 1000;

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

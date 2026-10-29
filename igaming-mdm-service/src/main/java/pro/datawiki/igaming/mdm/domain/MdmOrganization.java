package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Master Sports Organization (Club / Franchise / Multi-sport Institution).
 * Parent entity uniting multiple teams across divisions, youth tiers, and stat-disciplines.
 */
@Entity
@Table(name = "mdm_organization", indexes = {
        @Index(name = "idx_mdm_organization_slug", columnList = "slug"),
        @Index(name = "idx_mdm_organization_country", columnList = "country_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Column(nullable = false, length = 191)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private MdmCountry country;

    @Column(name = "country_name", length = 128)
    private String countryName;

    @Column(length = 128)
    private String city;

    @Column(name = "foundation_year")
    private Integer foundationYear;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Builder.Default
    @Column(name = "academy_tier", nullable = false, length = 32)
    private String academyTier = "STANDARD";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

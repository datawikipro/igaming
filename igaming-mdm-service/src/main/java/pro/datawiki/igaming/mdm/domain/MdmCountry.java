package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Canonical country/region reference table.
 * Sport-agnostic: one "England" entry serves Football, Tennis, Cricket, etc.
 * <p>
 * Used as FK in mdm_tournament and mdm_team to enable country-aware resolution
 * and prevent cross-country collisions (e.g. "Premier League" in England vs Bahrain vs Nigeria).
 */
@Entity
@Table(name = "mdm_country", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_country_name", columnNames = {"canonical_name"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmCountry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** ISO 3166-1 alpha-2 or alpha-3 code (e.g. "GB", "NG", "BH"). Nullable for regions/continents. */
    @Column(name = "iso_code", unique = true, length = 4)
    private String isoCode;

    /** Human-readable canonical name used for display and resolution (e.g. "England", "Bahrain"). */
    @Column(name = "canonical_name", nullable = false, length = 128)
    private String canonicalName;

    /** Flag emoji key or sprite code for UI rendering. */
    @Column(name = "flag_code", length = 16)
    private String flagCode;

    /** SofaScore category ID if this country was imported from SofaScore. */
    @Column(name = "sofascore_id", unique = true)
    private Integer sofascoreId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

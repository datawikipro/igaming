package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Mapping table from external bookmaker raw country/category keys
 * (e.g. "Англия", "england", "ENG", "104") to canonical MDM Country.
 */
@Entity
@Table(name = "mdm_country_ingestion_map", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mdm_country_map", columnNames = {"external_system_id", "raw_key"})
}, indexes = {
        @Index(name = "idx_mdm_country_map_lookup", columnList = "external_system_id, raw_key"),
        @Index(name = "idx_mdm_country_map_country", columnList = "canonical_country_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmCountryIngestionMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "external_system_id", nullable = false)
    private MdmExternalSystem externalSystem;

    @Column(name = "raw_key", nullable = false, length = 255)
    private String rawKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canonical_country_id", nullable = false)
    private MdmCountry canonicalCountry;

    @Column(name = "raw_name", length = 255)
    private String rawName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

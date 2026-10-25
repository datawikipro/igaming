package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_sport_ingestion_map", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_sport_map", columnNames = {"external_system_id", "raw_key"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmSportIngestionMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "external_system_id", nullable = false)
    private MdmExternalSystem externalSystem;

    @Column(name = "raw_key", nullable = false, length = 128)
    private String rawKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canonical_sport_id", nullable = false)
    private MdmSport canonicalSport;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

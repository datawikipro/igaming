package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_team_ingestion_map", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_team_map", columnNames = {"external_system_id", "raw_key"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTeamIngestionMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "external_system_id", nullable = false)
    private MdmExternalSystem externalSystem;

    @Column(name = "raw_key", nullable = false)
    private String rawKey;

    @Column(name = "source_id")
    private String sourceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canonical_team_id", nullable = false)
    private MdmTeam canonicalTeam;

    @Column(name = "raw_name")
    private String rawName;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

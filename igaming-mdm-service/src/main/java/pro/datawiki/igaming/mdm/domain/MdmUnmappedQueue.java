package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_unmapped_queue", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_unmapped_queue", columnNames = {"external_system_code", "entity_type", "raw_key"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmUnmappedQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_system_code", nullable = false, length = 64)
    private String externalSystemCode;

    @Column(name = "entity_type", nullable = false, length = 32)
    private String entityType; // TOURNAMENT, TEAM

    @Column(name = "raw_key", nullable = false, length = 255)
    private String rawKey;

    @Column(name = "source_id", length = 128)
    private String sourceId;

    @Column(name = "raw_name", length = 255)
    private String rawName;

    @Column(name = "sport_id")
    private Integer sportId;

    @Column(name = "sport_slug", length = 64)
    private String sportSlug;

    @Column(name = "country_id")
    private Integer countryId;

    @Column(name = "country_name", length = 128)
    private String countryName;

    @Column(name = "context_parent_key", length = 255)
    private String contextParentKey;

    @Column(name = "context_parent_name", length = 255)
    private String contextParentName;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 32)
    private String status = "PENDING"; // PENDING, RESOLVED, IGNORED

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_sport")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmSport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MdmSport parent;

    @Column(name = "domain_key", nullable = false, length = 64)
    private String domainKey;

    @Builder.Default
    @Column(name = "is_esports", nullable = false)
    private Boolean isEsports = false;

    @Builder.Default
    @Column(name = "is_team_sport", nullable = false)
    private Boolean isTeamSport = true;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 100;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

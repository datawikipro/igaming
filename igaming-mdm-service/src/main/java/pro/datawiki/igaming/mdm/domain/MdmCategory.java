package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_category", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_category_sport_slug", columnNames = {"sport_id", "slug"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private MdmSport sport;

    @Column(nullable = false, length = 128)
    private String slug;

    @Column(length = 16)
    private String code;

    @Column(name = "sofascore_id", unique = true)
    private Long sofascoreId;

    @Column(name = "flag_code", length = 16)
    private String flagCode;

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

package pro.datawiki.igaming.analyst.rating;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sport_tournament_tier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportTournamentTierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sport_slug", length = 32, nullable = false)
    private String sportSlug;

    @Column(name = "tier_level", nullable = false)
    private Integer tierLevel;

    @Column(name = "tier_name", length = 64, nullable = false)
    private String tierName;

    @Column(name = "anchor_mmr", nullable = false)
    private Integer anchorMmr;

    @Column(name = "soft_cap_mmr", nullable = false)
    private Integer softCapMmr;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}

package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_player_archetype")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmPlayerArchetype {

    @Id
    @Column(name = "player_id")
    private Long playerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "player_id")
    private MdmPlayer player;

    @Column(length = 64)
    private String archetype;

    @Column(name = "overall_rating", precision = 4, scale = 2)
    private BigDecimal overallRating;

    @Builder.Default
    @Column(name = "is_joker_candidate", nullable = false)
    private Boolean isJokerCandidate = false;

    @Column(name = "preferred_foot", length = 16)
    private String preferredFoot;

    @Column(name = "pace_rating", precision = 4, scale = 2)
    private BigDecimal paceRating;

    @Column(name = "shooting_rating", precision = 4, scale = 2)
    private BigDecimal shootingRating;

    @Column(name = "passing_rating", precision = 4, scale = 2)
    private BigDecimal passingRating;

    @Column(name = "reference_tier")
    private Integer referenceTier;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "similar_players", columnDefinition = "jsonb")
    private Object similarPlayers;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

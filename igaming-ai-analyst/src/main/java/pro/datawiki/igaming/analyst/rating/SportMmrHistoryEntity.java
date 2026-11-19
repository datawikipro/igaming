package pro.datawiki.igaming.analyst.rating;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sport_mmr_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportMmrHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "sport_slug", length = 32, nullable = false)
    private String sportSlug;

    @Column(name = "surface", length = 16, nullable = false)
    @Builder.Default
    private String surface = "ALL";

    @Column(name = "match_time", nullable = false)
    private Instant matchTime;

    @Column(name = "mmr_before", nullable = false)
    private Double mmrBefore;

    @Column(name = "mmr_after", nullable = false)
    private Double mmrAfter;

    @Column(name = "rd_before", nullable = false)
    private Double rdBefore;

    @Column(name = "rd_after", nullable = false)
    private Double rdAfter;

    @Column(name = "delta_mmr", nullable = false)
    private Double deltaMmr;

    @Column(name = "opponent_id", nullable = false)
    private Long opponentId;

    @Column(name = "opponent_mmr", nullable = false)
    private Double opponentMmr;

    @Column(name = "tier_level")
    private Integer tierLevel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

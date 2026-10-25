package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_team_dna")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTeamDna {

    @Id
    @Column(name = "team_id")
    private Long teamId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "team_id")
    private MdmTeam team;

    @Column(name = "primary_formation", length = 32)
    private String primaryFormation;

    @Column(name = "play_style", length = 64)
    private String playStyle;

    @Column(name = "pressing_intensity", precision = 5, scale = 2)
    private BigDecimal pressingIntensity;

    @Column(name = "possession_avg_pct", precision = 5, scale = 2)
    private BigDecimal possessionAvgPct;

    @Column(name = "aerial_threat_score", precision = 5, scale = 2)
    private BigDecimal aerialThreatScore;

    @Column(name = "defense_vulnerability_score", precision = 5, scale = 2)
    private BigDecimal defenseVulnerabilityScore;

    @Column(name = "reference_tier")
    private Integer referenceTier;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "similar_teams", columnDefinition = "jsonb")
    private Object similarTeams;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

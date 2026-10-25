package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_team_membership", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_player_team_membership", columnNames = {"player_id", "team_id", "membership_type"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTeamMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private MdmPlayer player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private MdmTeam team;

    /**
     * CLUB_PRIMARY, NATIONAL_TEAM, ON_LOAN, YOUTH_ACADEMY
     */
    @Column(name = "membership_type", nullable = false, length = 32)
    private String membershipType;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    /**
     * STARTER, ROTATION, RESERVE
     */
    @Builder.Default
    @Column(name = "squad_role", nullable = false, length = 32)
    private String squadRole = "ROTATION";

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "contract_until")
    private LocalDate contractUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

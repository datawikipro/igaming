package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_team_sofa_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTeamSofaMetadata {

    @Id
    @Column(name = "team_id")
    private Long teamId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "team_id")
    private MdmTeam team;

    @Column(name = "coach_name", length = 128)
    private String coachName;

    @Column(name = "venue_name", length = 191)
    private String venueName;

    @Column(name = "venue_city", length = 128)
    private String venueCity;

    @Column(name = "venue_capacity")
    private Integer venueCapacity;

    @Column(name = "sofascore_rating", precision = 4, scale = 2)
    private BigDecimal sofascoreRating;

    @Column(name = "form_recent", length = 16)
    private String formRecent;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

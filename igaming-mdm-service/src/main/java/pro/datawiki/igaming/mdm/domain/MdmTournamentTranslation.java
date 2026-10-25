package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_tournament_translation", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_tourn_trans", columnNames = {"tournament_id", "lang"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTournamentTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private MdmTournament tournament;

    @Column(nullable = false, length = 8)
    private String lang;

    @Column(nullable = false)
    private String name;

    @Column(name = "short_name", length = 64)
    private String shortName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

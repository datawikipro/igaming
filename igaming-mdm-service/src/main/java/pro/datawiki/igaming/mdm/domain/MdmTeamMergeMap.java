package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_team_merge_map")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmTeamMergeMap {

    @Id
    @Column(name = "local_id", nullable = false)
    private Long localId;

    @Column(name = "sofa_id", nullable = false)
    private Long sofaId;

    @Column(name = "local_name", nullable = false)
    private String localName;

    @Column(name = "sofa_name", nullable = false)
    private String sofaName;

    @Column(name = "sport_id", nullable = false)
    private Integer sportId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

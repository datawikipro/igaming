package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_player_injury_log", indexes = {
    @Index(name = "idx_injury_player_returned", columnList = "player_id, actual_returned_at"),
    @Index(name = "idx_injury_started_at", columnList = "started_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmPlayerInjuryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private MdmPlayer player;

    @Column(name = "injury_type", nullable = false, length = 64)
    private String injuryType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "severity", nullable = false, length = 32)
    private String severity = "MODERATE"; // MINOR, MODERATE, SEVERE

    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    @Column(name = "expected_return_at")
    private LocalDate expectedReturnAt;

    @Column(name = "actual_returned_at")
    private LocalDate actualReturnedAt;

    @Builder.Default
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

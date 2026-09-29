package pro.datawiki.igaming.vkdonut.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Persisted VK Donut donor record.
 *
 * Each row represents a unique VK user who has (or had) an active Donut
 * subscription to the SmartBet.guru VK community.
 *
 * Per AGENTS.md Rule #7 the table is managed via ddl-auto=update.
 */
@Entity
@Table(name = "donut_donor",
        indexes = @Index(name = "idx_donut_donor_vk_user_id", columnList = "vk_user_id", unique = true))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonutDonor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** VK user ID of the donor (unique, stable identifier). */
    @Column(name = "vk_user_id", nullable = false, unique = true)
    private Long vkUserId;

    /** VK first name (snapshot at subscription time, may be refreshed). */
    @Column(name = "first_name", length = 100)
    private String firstName;

    /** VK last name (snapshot). */
    @Column(name = "last_name", length = 100)
    private String lastName;

    /** Monthly donation amount in rubles (latest known). */
    @Column(name = "amount_rub")
    private Integer amountRub;

    /** Current subscription status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DonutStatus status;

    /** Timestamp when the subscription was first created. */
    @Column(name = "subscribed_at")
    private Instant subscribedAt;

    /** Timestamp of the last status change or renewal. */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Timestamp when the subscription was cancelled or expired (null if still active). */
    @Column(name = "ended_at")
    private Instant endedAt;
}

package pro.datawiki.igaming.boosty.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Persisted record of a Boosty paid subscriber (donor).
 *
 * <p>Populated and updated by {@link pro.datawiki.igaming.boosty.service.BoostyDonorService}
 * during polling cycles.
 *
 * <p>Table: {@code boosty_donor}
 */
@Entity
@Table(name = "boosty_donor",
       uniqueConstraints = @UniqueConstraint(name = "uq_boosty_donor_user_id", columnNames = "boosty_user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoostyDonor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Boosty internal user ID (numeric). */
    @Column(name = "boosty_user_id", nullable = false)
    private Long boostyUserId;

    /** Boosty username / login (slug). */
    @Column(name = "username", length = 128)
    private String username;

    /** Display name (name + surname from Boosty profile). */
    @Column(name = "display_name", length = 255)
    private String displayName;

    /** Current subscription level name (tier title on Boosty). */
    @Column(name = "subscription_level", length = 128)
    private String subscriptionLevel;

    /** Monthly donation amount in rubles. */
    @Column(name = "amount_rub")
    private Integer amountRub;

    /** Subscription status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private BoostyDonorStatus status;

    /** When the subscription was first detected (in our system). */
    @Column(name = "subscribed_at", nullable = false)
    private Instant subscribedAt;

    /** Last time this record was modified. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** When the subscription ended (cancelled/expired), null if still active. */
    @Column(name = "ended_at")
    private Instant endedAt;
}

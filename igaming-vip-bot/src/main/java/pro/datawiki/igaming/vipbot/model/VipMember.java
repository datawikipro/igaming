package pro.datawiki.igaming.vipbot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Persisted VIP member record.
 * Tracks Telegram user IDs that are active members of the VIP channel.
 */
@Entity
@Table(name = "vip_member",
        indexes = @Index(name = "idx_vip_member_telegram_id", columnList = "telegram_user_id", unique = true))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VipMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Telegram user ID (unique across the platform). */
    @Column(name = "telegram_user_id", nullable = false, unique = true)
    private Long telegramUserId;

    /** Telegram username (without @, may change). */
    @Column(name = "username", length = 100)
    private String username;

    /** Display first name of the user. */
    @Column(name = "first_name", length = 100)
    private String firstName;

    /** Current VIP subscription status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VipStatus status;

    /** Portal userId linked to this Telegram account (nullable until linked). */
    @Column(name = "portal_user_id")
    private Long portalUserId;

    /** Timestamp when the VIP subscription was activated. */
    @Column(name = "joined_at")
    private Instant joinedAt;

    /** Timestamp of the last status change. */
    @Column(name = "updated_at")
    private Instant updatedAt;
}

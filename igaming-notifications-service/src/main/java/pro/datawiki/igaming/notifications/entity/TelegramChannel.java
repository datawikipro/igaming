package pro.datawiki.igaming.notifications.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "telegram_channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "chat_id", nullable = false, unique = true)
    private String chatId;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "low_profit_threshold")
    private Double lowProfitThreshold;

    @Column(name = "low_profit_interval_minutes")
    private Integer lowProfitIntervalMinutes;

    @Column(name = "high_profit_interval_minutes")
    private Integer highProfitIntervalMinutes;

    @Column(name = "min_freshness_minutes")
    private Integer minFreshnessMinutes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package pro.datawiki.igaming.analyst.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_user_strategy_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUserStrategySetting {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "active_strategy_slug", nullable = false, length = 50)
    private String activeStrategySlug;

    @Column(name = "custom_overrides_json", columnDefinition = "TEXT")
    private String customOverridesJson;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

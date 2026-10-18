package pro.datawiki.igaming.analyst.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_learning_memory", indexes = {
        @Index(name = "idx_ai_learning_team", columnList = "team_name"),
        @Index(name = "idx_ai_learning_league", columnList = "league_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiLearningMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "team_name", length = 255)
    private String teamName;

    @Column(name = "league_name", length = 255)
    private String leagueName;

    @Column(name = "context_category", length = 100)
    private String contextCategory;

    @Column(name = "lesson_text", columnDefinition = "TEXT")
    private String lessonText;

    @Column(name = "source_match_id")
    private Long sourceMatchId;

    @Column(name = "confidence_score")
    @Builder.Default
    private Integer confidenceScore = 80;
}

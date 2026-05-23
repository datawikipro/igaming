package pro.datawiki.igaming.llm.domain;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "llm_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 64)
    private String providerType;   // e.g. "deepseek", "gemini"

    /**
     * Логическая ссылка на llm_models.id в igaming-llm-admin.
     * Не используем @ManyToOne — разные БД (cross-service FK).
     */
    @Column
    private Long modelId;

    @Column(nullable = false, length = 128)
    private String modelName;      // денормализованное: llm_models.model_id ("gemini-2.5-flash")

    @Column(nullable = false, length = 64)
    private String promptHash;     // SHA-256(providerType + modelName + prompt)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String systemPrompt;

    private Double temperature;
    private Integer maxTokens;

    @Column(length = 128)
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private String metadata;

    /**
     * Если true — ответ кешируется навсегда (expires_at = NULL).
     * Пример: ФИО теннисиста, название команды.
     * Если false — expires_at = created_at + ttl_hours.
     * Пример: анализ матча, прогноз.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean permanent = false;

    /**
     * TTL в часах. Игнорируется если permanent=true.
     */
    @Builder.Default
    @Column(nullable = false)
    private int ttlHours = 24;

    /**
     * PENDING → воркер не взял
     * PROCESSING → воркер взял, обрабатывает
     * COMPLETED → ответ получен
     * FAILED → ошибка обработки
     */
    @Builder.Default
    @Column(nullable = false, length = 32)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String resultText;

    private Integer tokensUsed;

    @Column(length = 64)
    private String finishReason;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /** true если ответ вернули из кеша без вызова LLM */
    @Builder.Default
    @Column(nullable = false)
    private boolean cached = false;

    /** Pod-name воркера, взявшего задачу */
    @Column(length = 256)
    private String workerId;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    /**
     * NULL если permanent=true, иначе createdAt + ttlHours
     */
    private LocalDateTime expiresAt;

    /**
     * Вычисляет expiresAt перед первым сохранением.
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (!permanent && expiresAt == null) {
            expiresAt = createdAt.plusHours(ttlHours);
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package pro.datawiki.igaming.llm.admin.domain;

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

    @Column
    private Long modelId;

    @Column(nullable = false, length = 128)
    private String modelName;      // e.g. "gemini-3-flash-preview"

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

    @Builder.Default
    @Column(nullable = false)
    private boolean permanent = false;

    @Builder.Default
    @Column(nullable = false)
    private int ttlHours = 24;

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

    @Builder.Default
    @Column(nullable = false)
    private boolean cached = false;

    @Column(length = 256)
    private String workerId;

    @Column(name = "logical_type", length = 64)
    private String logicalType;

    @Column(name = "google_required")
    private Boolean googleRequired;

    @Column(name = "urgency", length = 32)
    private String urgency;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

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

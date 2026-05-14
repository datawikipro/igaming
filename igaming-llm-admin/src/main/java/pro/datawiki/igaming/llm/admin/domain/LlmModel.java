package pro.datawiki.igaming.llm.admin.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "llm_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private LlmProvider provider;

    /**
     * Идентификатор модели для вызова API.
     * Примеры: "deepseek-chat", "deepseek-reasoner",
     *          "gemini-2.5-flash-preview-05-20", "gemini-2.5-pro-preview-05-06"
     */
    @Column(nullable = false, length = 128)
    private String modelId;

    /** Человекочитаемое название: "DeepSeek Chat", "Gemini 2.5 Flash" */
    @Column(nullable = false, length = 128)
    private String displayName;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

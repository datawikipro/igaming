package pro.datawiki.igaming.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Расширенный LlmRequest с полями очереди.
 * permanent + ttlHours управляют кешированием.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {
    private String prompt;
    private String systemPrompt;
    private String model;           // e.g. "deepseek-chat", "gemini-2.0-flash"
    private String providerType;    // e.g. "deepseek", "gemini" (optional, вычисляется из model если не задан)
    private Double temperature;
    private Integer maxTokens;
    private String userId;

    /**
     * Вечный кеш (true) — не устаревает никогда.
     * Примеры: ФИО игрока, название команды.
     * Default: false
     */
    @Builder.Default
    private boolean permanent = false;

    /**
     * TTL кеша в часах. Игнорируется если permanent=true.
     * Default: 24 часа.
     */
    @Builder.Default
    private int ttlHours = 24;

    private java.util.Map<String, Object> metadata;
}

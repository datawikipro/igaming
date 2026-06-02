package pro.datawiki.igaming.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("system_prompt")
    private String systemPrompt;

    private String model;           // e.g. "deepseek-chat", "gemini-2.0-flash"
    private String providerType;    // e.g. "deepseek", "gemini" (optional, вычисляется из model если не задан)
    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("user_id")
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

    private LlmMetadataDto metadata;

    @JsonProperty("logical_type")
    private String logicalType;

    @JsonProperty("use_search")
    private Boolean useSearch;

    private String urgency;
}

package pro.datawiki.accounts.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * LLM subscription credentials for a given Account.
 * Currently supports Antigravity (Google AI) via refresh token.
 * Can be extended for OpenAI, Anthropic, etc.
 *
 * Supported providers: ANTIGRAVITY, OPENAI, ANTHROPIC
 */
@Entity
@Table(name = "llm_credentials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Account account;

    /** Human-readable label, e.g. user email */
    @Column(nullable = false)
    private String email;

    /**
     * ANTIGRAVITY: Google OAuth refresh token (for agy.exe).
     * OPENAI: API key.
     * ANTHROPIC: API key.
     */
    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    /** ANTIGRAVITY, OPENAI, ANTHROPIC */
    @Column(nullable = false)
    @Builder.Default
    private String provider = "ANTIGRAVITY";

    @Builder.Default
    private Boolean isActive = true;

    /** Google account country association (e.g., US, RU, TR) */
    private String region;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (provider == null) provider = "ANTIGRAVITY";
        if (isActive == null) isActive = true;
    }
}

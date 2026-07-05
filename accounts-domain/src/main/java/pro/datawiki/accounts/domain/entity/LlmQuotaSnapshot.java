package pro.datawiki.accounts.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Point-in-time snapshot of LLM quota usage for one LlmCredential.
 * Written by llm-poller every 15 minutes (after agy.exe /usage output is parsed).
 */
@Entity
@Table(name = "llm_quota_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmQuotaSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to llm_credentials */
    @Column(nullable = false)
    private Long llmCredentialId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // ── Gemini ────────────────────────────────────────────────────────────────

    /** Remaining fraction 0.0–1.0 for Gemini weekly limit */
    private Double geminiWeeklyPct;

    /** Remaining fraction 0.0–1.0 for Gemini 5-hour limit */
    private Double gemini5hPct;

    /** When the Gemini weekly quota resets */
    private LocalDateTime geminiWeeklyResetsAt;

    /** When the Gemini 5h quota resets */
    private LocalDateTime gemini5hResetsAt;

    // ── Claude / GPT ──────────────────────────────────────────────────────────

    /** Remaining fraction 0.0–1.0 for Claude+GPT weekly limit */
    private Double claudeWeeklyPct;

    /** Remaining fraction 0.0–1.0 for Claude+GPT 5-hour limit */
    private Double claude5hPct;

    /** When the Claude+GPT weekly quota resets */
    private LocalDateTime claudeWeeklyResetsAt;

    // ── Debug ─────────────────────────────────────────────────────────────────

    /** Raw agy.exe output (ANSI stripped) for debugging */
    @Column(columnDefinition = "TEXT")
    private String rawOutput;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }
}

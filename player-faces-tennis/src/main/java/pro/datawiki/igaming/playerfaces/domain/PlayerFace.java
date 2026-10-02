package pro.datawiki.igaming.playerfaces.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.Normalizer;
import java.time.LocalDateTime;

/**
 * Domain entity representing an athlete's headshot avatar profile,
 * ranking, nationality and seed details for solo sports cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "player_face",
    indexes = {
        @Index(name = "idx_player_normalized_name", columnList = "normalized_name"),
        @Index(name = "idx_player_sport_tour", columnList = "sport, tour")
    }
)
public class PlayerFace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(name = "aliases", columnDefinition = "TEXT")
    private String aliases;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport", nullable = false, length = 50)
    private SportType sport;

    @Enumerated(EnumType.STRING)
    @Column(name = "tour", length = 50)
    private TourType tour;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "flag_url", length = 512)
    private String flagUrl;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "seed_number")
    private Integer seedNumber;

    @Column(name = "avatar_url", nullable = false, length = 512)
    private String avatarUrl;

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_provider", nullable = false, length = 50)
    private AvatarSourceProvider sourceProvider;

    @Builder.Default
    @Column(name = "webp_optimized")
    private Boolean webpOptimized = Boolean.TRUE;

    @Column(name = "last_harvested_at", nullable = false)
    private LocalDateTime lastHarvestedAt;

    /**
     * Standardizes athlete name for fuzzy matching across various bookmakers and data providers.
     * Strips diacritics, unifies transliterations (e.g. Djokovic / Djoković), removes noise characters.
     */
    public static String normalizeName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String clean = raw.trim().toLowerCase();

        // Remove bracketed or parenthesized tags like (SUI), [1], (RUS)
        clean = clean.replaceAll("\\([^)]*\\)", " ")
                     .replaceAll("\\[[^\\]]*\\]", " ");

        // Custom character replacements
        clean = clean.replace("đ", "dj")
                     .replace("ø", "o")
                     .replace("æ", "ae")
                     .replace("œ", "oe")
                     .replace("ß", "ss");

        // Decompose diacritics and strip non-spacing marks
        clean = Normalizer.normalize(clean, Normalizer.Form.NFD);
        clean = clean.replaceAll("\\p{M}", "");

        // Keep only alphanumeric, hyphens, and whitespace
        clean = clean.replaceAll("[^a-z0-9\\s-]", " ");

        // Collapse multiple whitespace
        clean = clean.replaceAll("\\s+", " ").trim();

        return clean;
    }
}

package pro.datawiki.igaming.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO representing a scheduled fixture reference from any provider (SofaScore, ESPN, etc.).
 * Extended with player face avatar URLs for single-player sports (Tennis, MMA, Boxing, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceFixtureDto {

    /** Provider name, e.g. "SOFASCORE", "ESPN" */
    private String provider;

    private String externalFixtureId;
    private String sport;
    private String leagueName;
    private String countryName;

    private String team1Name;
    private String team1NameLocal;
    private String team1ExternalId;
    private String team1LogoUrl;

    /**
     * URL of player face/portrait photo for team1 (participant 1).
     * Populated only for single-player sports: Tennis, MMA, Boxing, Table Tennis, Badminton, Squash, Darts.
     * For SofaScore: https://api.sofascore.app/api/v1/player/{playerId}/image
     */
    private String team1PlayerFaceUrl;

    private String team2Name;
    private String team2NameLocal;
    private String team2ExternalId;
    private String team2LogoUrl;

    /**
     * URL of player face/portrait photo for team2 (participant 2).
     * Populated only for single-player sports: Tennis, MMA, Boxing, Table Tennis, Badminton, Squash, Darts.
     * For SofaScore: https://api.sofascore.app/api/v1/player/{playerId}/image
     */
    private String team2PlayerFaceUrl;

    // ── National team detection fields ───────────────────────────────────────

    /**
     * True when team1 is detected as a national (representative) team.
     * Detection uses SofaScore's team.national flag and/or name-based heuristics.
     */
    private Boolean team1IsNational;

    /**
     * ISO-3166-1 alpha-2 country code for team1 when it is a national team.
     * E.g. "DE", "BR", "RU". Null for club teams.
     */
    private String team1CountryCode;

    /**
     * Flag image URL for team1 when it is a national team.
     * Sourced from SofaScore category.flag or derived from ISO code.
     */
    private String team1FlagUrl;

    /**
     * True when team2 is detected as a national (representative) team.
     * Detection uses SofaScore's team.national flag and/or name-based heuristics.
     */
    private Boolean team2IsNational;

    /**
     * ISO-3166-1 alpha-2 country code for team2 when it is a national team.
     * E.g. "DE", "BR", "RU". Null for club teams.
     */
    private String team2CountryCode;

    /**
     * Flag image URL for team2 when it is a national team.
     * Sourced from SofaScore category.flag or derived from ISO code.
     */
    private String team2FlagUrl;

    private Long startTimeEpochMs;
    private String status;

    /** Arbitrary key-value metadata for provider-specific extensions */
    private Map<String, Object> metadata;
}

package pro.datawiki.igaming.playerfaces.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.playerfaces.domain.TennisSurface;
import pro.datawiki.igaming.playerfaces.domain.TourType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Event published to Kafka topic 'sports.tennis.point-by-point'.
 * Contains deep point-by-point serving, returning, break-point, and tiebreak
 * metrics across surfaces with MDM player entity resolution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TennisPointByPointEvent {

    /** Internal MDM player identifier */
    private Long mdmPlayerId;

    /** Official athlete display name */
    private String playerName;

    /** Normalized lookup key */
    private String normalizedName;

    /** Known aliases and alternative spellings */
    private List<String> aliases;

    /** Professional tour organization (ATP, WTA, etc.) */
    private TourType tour;

    /** Country ISO code (e.g. ESP, ITA, SRB, POL) */
    private String countryCode;

    /** Current official ranking */
    private Integer currentRanking;

    /** Headshot avatar URL */
    private String avatarUrl;

    /** Data source provider ("TENNIS_ABSTRACT", "TENNIS_EXPLORER", "AGGREGATED") */
    private String sourceProvider;

    /**
     * Surface-specific metrics breakdown keyed by TennisSurface (HARD, CLAY, GRASS, INDOOR).
     */
    private Map<TennisSurface, TennisSurfaceStats> surfaceStats;

    /** Overall career aggregate tiebreak record */
    private TiebreakStats overallTiebreaks;

    /** Event creation timestamp */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}

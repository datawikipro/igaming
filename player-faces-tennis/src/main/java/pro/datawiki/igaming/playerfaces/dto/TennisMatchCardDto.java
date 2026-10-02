package pro.datawiki.igaming.playerfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;

import java.time.LocalDateTime;

/**
 * Head-to-Head solo match card DTO with player portraits, rankings, seeds,
 * match surface, live scores, and arbitrage / value bet indicators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TennisMatchCardDto {

    private String matchId;
    private String tournamentName;
    private String round;
    private String surface; // "Hard", "Clay", "Grass", "Indoor Hard"
    private SportType sport;
    private TourType tour;
    private PlayerFaceDto player1;
    private PlayerFaceDto player2;
    private Double odds1;
    private Double odds2;
    private Double surebetProfitPercent;
    private String currentScore;
    private String status; // "LIVE", "SCHEDULED", "FINISHED"
    private LocalDateTime startTime;
}

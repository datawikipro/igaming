package pro.datawiki.igaming.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InjuryProbabilityAdjustmentResponse implements Serializable {

    private String sport;
    private String homeTeam;
    private String awayTeam;

    private double originalHomeProb;
    private double originalDrawProb;
    private double originalAwayProb;

    private double adjustedHomeProb;
    private double adjustedDrawProb;
    private double adjustedAwayProb;

    private double originalHandicapHome;
    private double adjustedHandicapHome;

    private double netInjuryAdvantage;
    private String summaryRationale;
}

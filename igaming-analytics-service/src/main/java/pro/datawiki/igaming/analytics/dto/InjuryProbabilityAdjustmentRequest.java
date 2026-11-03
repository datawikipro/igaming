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
public class InjuryProbabilityAdjustmentRequest implements Serializable {

    private String sport;
    private String homeTeam;
    private String awayTeam;

    @Builder.Default
    private double baseHomeProb = 0.45;

    @Builder.Default
    private double baseDrawProb = 0.27;

    @Builder.Default
    private double baseAwayProb = 0.28;

    @Builder.Default
    private double baseHandicapHome = -0.25;

    @Builder.Default
    private double homeKeyAbsenceImpact = 0.0;

    @Builder.Default
    private double awayKeyAbsenceImpact = 0.0;
}

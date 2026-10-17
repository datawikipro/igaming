package pro.datawiki.igaming.analyst.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalystStatsDto {

    private long totalEvaluations;
    private long totalSettled;
    private long won;
    private long lost;
    private long voidCount;
    private long pending;

    private double winratePct;
    private BigDecimal totalPnl;
    private double roiPct;
    private double avgOdds;
    private double avgEvPct;
}

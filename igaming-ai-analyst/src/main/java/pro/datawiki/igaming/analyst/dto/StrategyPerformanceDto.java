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
public class StrategyPerformanceDto {
    private String strategySlug;
    private String icon;
    private String strategyName;
    private String description;
    private boolean isDefault;
    private long totalPicks;
    private long wonCount;
    private long lostCount;
    private long voidCount;
    private Double winratePct;
    private BigDecimal totalPnlRub;
    private Double avgOdd;
    private BigDecimal minOdd;
    private BigDecimal maxOdd;
    private BigDecimal minProbabilityPct;
}

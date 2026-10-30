package pro.datawiki.igaming.analyst.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

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
    private long waiting;
    private long skipped;

    private double winratePct;
    private BigDecimal totalPnl;
    private double roiPct;
    private double avgOdds;
    private double avgEvPct;

    private TierStatsDto ml90;
    private TierStatsDto ml80;
    private TierStatsDto ml70;
    private TierStatsDto ml60;
    private TierStatsDto confident;
    private TierStatsDto goodPick;
    private TierStatsDto pass;
    private TierStatsDto waitTier;

    private Map<String, SportStatsDto> sports;

    private Map<String, TierStatsDto> backtest151;

    private Map<String, Long> stageCounts;
    private Map<String, Long> timeRangeCounts;
    private Map<String, Long> oddRangeCounts;
    private Map<String, Long> strategyCounts;
    private Long activeWithBookmakerCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SportStatsDto {
        private String sport;
        private long total;
        private long settled;
        private long won;
        private long lost;
        private long voidCount;
        private long pending;
        private long skipped;
        private double winratePct;
        private BigDecimal totalPnl;
        private double roiPct;
        private double avgOdds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierStatsDto {
        private String tier;
        private long total;
        private long settled;
        private long won;
        private long lost;
        private long voidCount;
        private long pending;
        private long skipped;
        private double winratePct;
        private BigDecimal totalPnl;
        private double roiPct;
        private double avgOdds;
    }
}

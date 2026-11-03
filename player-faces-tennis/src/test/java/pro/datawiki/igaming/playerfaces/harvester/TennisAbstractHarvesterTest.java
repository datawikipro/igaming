package pro.datawiki.igaming.playerfaces.harvester;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import pro.datawiki.igaming.playerfaces.domain.TennisSurface;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.TennisSurfaceStats;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TennisAbstractHarvesterTest {

    private TennisAbstractHarvester harvester;

    @BeforeEach
    void setUp() {
        harvester = new TennisAbstractHarvester(new RestTemplateBuilder());
    }

    @Test
    @DisplayName("Should harvest point-by-point stats across Hard, Clay, Grass, and Indoor surfaces")
    void testHarvestSurfaceStatsAllSurfaces() {
        Optional<Map<TennisSurface, TennisSurfaceStats>> statsOpt =
                harvester.harvestSurfaceStats("Carlos Alcaraz", TourType.ATP);

        assertThat(statsOpt).isPresent();
        Map<TennisSurface, TennisSurfaceStats> stats = statsOpt.get();

        assertThat(stats).containsKeys(
                TennisSurface.HARD,
                TennisSurface.CLAY,
                TennisSurface.GRASS,
                TennisSurface.INDOOR
        );

        // Verify Hard surface stats
        TennisSurfaceStats hard = stats.get(TennisSurface.HARD);
        assertThat(hard.getServeStats()).isNotNull();
        assertThat(hard.getServeStats().getFirstServeWonPct()).isGreaterThan(70.0);
        assertThat(hard.getServeStats().getSecondServeWonPct()).isGreaterThan(50.0);
        assertThat(hard.getReturnStats()).isNotNull();
        assertThat(hard.getReturnStats().getBreakPointConversionPct()).isGreaterThan(40.0);
        assertThat(hard.getTiebreakStats()).isNotNull();
        assertThat(hard.getTiebreakStats().getTiebreakWinPct()).isGreaterThan(50.0);

        // Verify Clay surface stats
        TennisSurfaceStats clay = stats.get(TennisSurface.CLAY);
        assertThat(clay.getReturnStats().getBreakPointConversionPct()).isNotNull();
        assertThat(clay.getReturnStats().getReturnGamesWonPct()).isGreaterThan(30.0);
    }

    @Test
    @DisplayName("Should generate calibrated baseline stats for unknown players")
    void testHarvestBaselineStats() {
        Optional<Map<TennisSurface, TennisSurfaceStats>> statsOpt =
                harvester.harvestSurfaceStats("Unknown Prospect", TourType.ATP);

        assertThat(statsOpt).isPresent();
        Map<TennisSurface, TennisSurfaceStats> stats = statsOpt.get();
        assertThat(stats).hasSize(4);
        assertThat(stats.get(TennisSurface.GRASS).getServeStats().getFirstServeWonPct()).isGreaterThan(70.0);
    }
}

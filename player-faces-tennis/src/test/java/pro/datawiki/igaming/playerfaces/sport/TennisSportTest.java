package pro.datawiki.igaming.playerfaces.sport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.playerfaces.domain.TennisSurface;
import pro.datawiki.igaming.playerfaces.dto.PointByPointReturnStats;
import pro.datawiki.igaming.playerfaces.dto.PointByPointServeStats;
import pro.datawiki.igaming.playerfaces.dto.TennisSurfaceStats;
import pro.datawiki.igaming.playerfaces.dto.TiebreakStats;

import static org.assertj.core.api.Assertions.assertThat;

class TennisSportTest {

    private TennisSport tennisSport;

    @BeforeEach
    void setUp() {
        tennisSport = new TennisSport();
    }

    @Test
    @DisplayName("Should calculate higher hold probability on fast surfaces (Grass) than slow surfaces (Clay)")
    void testSurfaceHoldProbabilityDifferences() {
        PointByPointServeStats serve = PointByPointServeStats.builder()
                .firstServeInPct(65.0)
                .firstServeWonPct(78.0)
                .secondServeWonPct(53.0)
                .build();

        PointByPointReturnStats ret = PointByPointReturnStats.builder()
                .firstServeReturnWonPct(31.0)
                .secondServeReturnWonPct(50.0)
                .build();

        double grassServeP = tennisSport.calculateServePointWinProb(serve, ret, TennisSurface.GRASS);
        double clayServeP = tennisSport.calculateServePointWinProb(serve, ret, TennisSurface.CLAY);

        assertThat(grassServeP).isGreaterThan(clayServeP);

        double grassHold = tennisSport.calculateGameHoldProb(grassServeP);
        double clayHold = tennisSport.calculateGameHoldProb(clayServeP);

        assertThat(grassHold).isGreaterThan(clayHold);
        assertThat(tennisSport.calculateGameBreakProb(clayServeP))
                .isGreaterThan(tennisSport.calculateGameBreakProb(grassServeP));
    }

    @Test
    @DisplayName("Should calculate surface edge favoring player with superior point-by-point stats")
    void testSurfaceEdgeCalculation() {
        TennisSurfaceStats p1Stats = TennisSurfaceStats.builder()
                .surface(TennisSurface.HARD)
                .serveStats(PointByPointServeStats.builder()
                        .firstServeInPct(67.0)
                        .firstServeWonPct(80.0)
                        .secondServeWonPct(57.0)
                        .serviceGamesWonPct(90.0)
                        .build())
                .returnStats(PointByPointReturnStats.builder()
                        .firstServeReturnWonPct(34.0)
                        .secondServeReturnWonPct(55.0)
                        .breakPointConversionPct(48.0)
                        .returnGamesWonPct(32.0)
                        .build())
                .breakPointSavePct(72.0)
                .tiebreakStats(TiebreakStats.builder()
                        .tiebreaksPlayed(20)
                        .tiebreaksWon(15)
                        .tiebreakWinPct(75.0)
                        .build())
                .build();

        TennisSurfaceStats p2Stats = TennisSurfaceStats.builder()
                .surface(TennisSurface.HARD)
                .serveStats(PointByPointServeStats.builder()
                        .firstServeInPct(60.0)
                        .firstServeWonPct(70.0)
                        .secondServeWonPct(48.0)
                        .serviceGamesWonPct(78.0)
                        .build())
                .returnStats(PointByPointReturnStats.builder()
                        .firstServeReturnWonPct(27.0)
                        .secondServeReturnWonPct(46.0)
                        .breakPointConversionPct(35.0)
                        .returnGamesWonPct(20.0)
                        .build())
                .breakPointSavePct(58.0)
                .tiebreakStats(TiebreakStats.builder()
                        .tiebreaksPlayed(15)
                        .tiebreaksWon(7)
                        .tiebreakWinPct(46.7)
                        .build())
                .build();

        double edge = tennisSport.calculateSurfaceEdge(TennisSurface.HARD, p1Stats, p2Stats);
        assertThat(edge).isPositive();
    }
}

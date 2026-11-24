package pro.datawiki.igaming.analyst.rating;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableTennisGlickoEngineTest {

    private TableTennisGlickoEngine engine;
    private TournamentTierClassifier tierClassifier;

    @BeforeEach
    void setUp() {
        engine = new TableTennisGlickoEngine();
        tierClassifier = new TournamentTierClassifier();
        tierClassifier.initVerifiedIdMappings();
    }

    @Test
    @DisplayName("Table Tennis Symmetrical Conservation: Winner gain and loser loss are damped equally, preventing deflation")
    void testSymmetricalExchangeDampingConservesMass() {
        GlickoPlayer p1 = GlickoPlayer.builder()
                .playerId(101L)
                .sportSlug("table-tennis")
                .surface("ALL")
                .mmr(1700.0)
                .rd(50.0)
                .volatility(0.06)
                .build();

        GlickoPlayer p2 = GlickoPlayer.builder()
                .playerId(102L)
                .sportSlug("table-tennis")
                .surface("ALL")
                .mmr(1500.0)
                .rd(50.0)
                .volatility(0.06)
                .build();

        double poolBefore = p1.getMmr() + p2.getMmr();

        TournamentTierClassifier.TierInfo tier = tierClassifier.classify(99075L, "table-tennis"); // Czech Liga Pro Tier 4
        MultiTierGlickoEngine.MatchUpdateResult res = engine.updateMatch(p1, p2, 3, 1, tier, 1.0);

        assertTrue(res.deltaP1() > 0, "Winner must gain rating");
        assertTrue(res.deltaP2() < 0, "Loser must lose rating");

        // When RDs are identical (50.0 vs 50.0), raw deltas are equal and opposite, and symmetrical damping preserves exact net sum
        double netChange = (p1.getMmr() + p2.getMmr()) - poolBefore;
        assertEquals(0.0, netChange, 0.5, "Total rating pool must remain conserved without artificial destruction!");
    }

    @Test
    @DisplayName("Rematch Decay Symmetrically Damps Repeated Encounters")
    void testH2HRematchDecay() {
        GlickoPlayer p1 = GlickoPlayer.builder()
                .playerId(101L)
                .sportSlug("table-tennis")
                .surface("ALL")
                .mmr(1600.0)
                .rd(50.0)
                .volatility(0.06)
                .build();

        GlickoPlayer p2 = GlickoPlayer.builder()
                .playerId(102L)
                .sportSlug("table-tennis")
                .surface("ALL")
                .mmr(1600.0)
                .rd(50.0)
                .volatility(0.06)
                .build();

        TournamentTierClassifier.TierInfo tier = tierClassifier.classify(99075L, "table-tennis");

        // 1st match: h2h = 1
        MultiTierGlickoEngine.MatchUpdateResult res1 = engine.updateMatch(p1, p2, 3, 2, tier, 1.0, 0.0, 0.0, 0.0, 1);

        // Reset MMRs for clean comparison
        p1.setMmr(1600.0);
        p1.setRd(50.0);
        p2.setMmr(1600.0);
        p2.setRd(50.0);

        // 4th match: h2h = 4 (decay = 1/sqrt(4) = 0.5)
        MultiTierGlickoEngine.MatchUpdateResult res4 = engine.updateMatch(p1, p2, 3, 2, tier, 1.0, 0.0, 0.0, 0.0, 4);

        assertEquals(res1.deltaP1() * 0.5, res4.deltaP1(), 0.01, "Delta should be halved at 4th rematch");
        assertEquals(res1.deltaP2() * 0.5, res4.deltaP2(), 0.01, "Loser loss should also be halved at 4th rematch");
    }

    @Test
    @DisplayName("Absolute Rating Floor: Negative MMR is physically impossible, clamped at 500.0")
    void testNegativeMmrImpossible() {
        GlickoPlayer losingPlayer = GlickoPlayer.builder()
                .playerId(999L)
                .sportSlug("table-tennis")
                .surface("ALL")
                .mmr(510.0)
                .rd(350.0)
                .volatility(0.06)
                .build();

        GlickoPlayer strongPlayer = GlickoPlayer.builder()
                .playerId(111L)
                .sportSlug("table-tennis")
                .surface("ALL")
                .mmr(2200.0)
                .rd(50.0)
                .volatility(0.06)
                .build();

        TournamentTierClassifier.TierInfo tier = tierClassifier.classify(99075L, "table-tennis");

        for (int i = 0; i < 50; i++) {
            engine.updateMatch(strongPlayer, losingPlayer, 3, 0, tier, 1.0);
        }

        assertTrue(losingPlayer.getMmr() >= 500.0, "Rating must never drop below 500.0! Actual: " + losingPlayer.getMmr());
    }
}

package pro.datawiki.igaming.analyst.rating;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiTierGlickoEngineTest {

    private final TournamentTierClassifier tierClassifier = new TournamentTierClassifier();
    private final MultiTierGlickoEngine engine = new MultiTierGlickoEngine();

    @Test
    @DisplayName("Polish Junior Guard: 25 consecutive wins in Tier 6 must not breach 1250 soft cap")
    void testPolishJuniorGuardLimitsInflation() {
        GlickoPlayer juniorA = GlickoPlayer.builder()
                .playerId(101L)
                .sportSlug("tennis")
                .surface("ALL")
                .mmr(1000.0)
                .rd(350.0)
                .volatility(0.06)
                .build();

        TournamentTierClassifier.TierInfo tier6 = tierClassifier.classify("Polish Junior Regional Liga U18");
        assertEquals(6, tier6.tierLevel());
        assertEquals(1250, tier6.softCapMmr());

        for (int match = 1; match <= 25; match++) {
            GlickoPlayer localOpponent = GlickoPlayer.builder()
                    .playerId(1000L + match)
                    .sportSlug("tennis")
                    .surface("ALL")
                    .mmr(900.0 + (match % 50))
                    .rd(150.0)
                    .volatility(0.06)
                    .build();

            engine.updateMatch(juniorA, localOpponent, 2, 0, tier6, 1.20);
        }

        assertTrue(juniorA.getMmr() <= 1250.0, 
                "MMR must not exceed soft cap 1250! Actual: " + juniorA.getMmr());
        assertTrue(juniorA.getMmr() >= 1240.0, 
                "MMR should have approached the ceiling! Actual: " + juniorA.getMmr());
        assertEquals(25, juniorA.getWins());
        assertEquals(25, juniorA.getStreak());

        // Now test breakout: Junior A plays in Tier 1 Grand Slam against a 2200 pro
        TournamentTierClassifier.TierInfo tier1 = tierClassifier.classify("Wimbledon Grand Slam");
        assertEquals(1, tier1.tierLevel());
        assertEquals(3500, tier1.softCapMmr());

        GlickoPlayer proPlayer = GlickoPlayer.builder()
                .playerId(9999L)
                .sportSlug("tennis")
                .surface("ALL")
                .mmr(2200.0)
                .rd(50.0)
                .volatility(0.06)
                .build();

        MultiTierGlickoEngine.MatchUpdateResult res = engine.updateMatch(juniorA, proPlayer, 2, 1, tier1, 1.0);
        assertTrue(res.deltaP1() > 40.0, "Delta against 2200 pro in Grand Slam should be significant!");
        assertTrue(juniorA.getMmr() > 1290.0, "Junior A MMR should have unlocked and climbed!");
    }

    @Test
    @DisplayName("Tournament Tier Classifier correctly handles grand slams, challengers, and local leagues")
    void testTournamentClassification() {
        assertEquals(1, tierClassifier.classify("Wimbledon").tierLevel());
        assertEquals(1, tierClassifier.classify("US Open, New York").tierLevel());
        assertEquals(2, tierClassifier.classify("ATP 500 Dubai").tierLevel());
        assertEquals(3, tierClassifier.classify("ATP Challenger 100 Phoenix").tierLevel());
        assertEquals(4, tierClassifier.classify("ITF M25 Antalya").tierLevel());
        assertEquals(5, tierClassifier.classify("ITF M15 Monastir 38 Men").tierLevel());
        assertEquals(6, tierClassifier.classify("Glavno da se igra - svi").tierLevel());
        assertEquals(6, tierClassifier.classify("Skupina Kovacic").tierLevel());
    }
}

package pro.datawiki.igaming.analyst.rating;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiTierGlickoEngineTest {

    private TournamentTierClassifier tierClassifier;
    private MultiTierGlickoEngine engine;

    @BeforeEach
    void setUp() {
        tierClassifier = new TournamentTierClassifier();
        tierClassifier.initVerifiedIdMappings();
        engine = new MultiTierGlickoEngine();
    }

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

        tierClassifier.registerTournamentIdTier("tennis", 99999L, TournamentTierClassifier.TENNIS_TIER_6);
        TournamentTierClassifier.TierInfo tier6 = tierClassifier.classify(99999L, "tennis");
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

        // Breakout: Junior A plays in Tier 1 Grand Slam (Wimbledon ID 711) against a 2200 pro
        TournamentTierClassifier.TierInfo tier1 = tierClassifier.classify(711L, "tennis");
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
    @DisplayName("Constitutional Taboo: Unknown tournaments MUST return UNKNOWN, never Tier 5")
    void testUnknownTournamentNeverDefaultsToTier5() {
        TournamentTierClassifier.TierInfo unknown = tierClassifier.classify(987654321L, "football");
        assertEquals(TournamentTierClassifier.UNKNOWN, unknown);
        assertEquals(0, unknown.tierLevel());
        assertEquals(1500, unknown.softCapMmr());

        TournamentTierClassifier.TierInfo unknownTt = tierClassifier.classify(87654321L, "table-tennis");
        assertEquals(TournamentTierClassifier.UNKNOWN, unknownTt);

        TournamentTierClassifier.TierInfo nullId = tierClassifier.classify(null, "football");
        assertEquals(TournamentTierClassifier.UNKNOWN, nullId);
    }

    @Test
    @DisplayName("Tournament ID resolution works accurately across sports")
    void testTournamentClassification() {
        // Football: ID 17 = Premier League (Tier 2)
        assertEquals(2, tierClassifier.classify(17L, "football").tierLevel());
        assertEquals(2800, tierClassifier.classify(17L, "football").softCapMmr());

        // Football: ID 7 = Champions League (Tier 1)
        assertEquals(1, tierClassifier.classify(7L, "football").tierLevel());
        assertEquals(3200, tierClassifier.classify(7L, "football").softCapMmr());

        // Football: ID 2 = Championship (Tier 3)
        assertEquals(3, tierClassifier.classify(2L, "football").tierLevel());

        // Table Tennis: ID 99075 = Czech Liga Pro (Tier 4)
        assertEquals(4, tierClassifier.classify(99075L, "table-tennis").tierLevel());
        assertEquals(2000, tierClassifier.classify(99075L, "table-tennis").softCapMmr());

        // Table Tennis: ID 78888 = Setka Cup (Tier 5)
        assertEquals(5, tierClassifier.classify(78888L, "table-tennis").tierLevel());
        assertEquals(1650, tierClassifier.classify(78888L, "table-tennis").softCapMmr());

        // Tennis: ID 711 = Wimbledon (Tier 1)
        assertEquals(1, tierClassifier.classify(711L, "tennis").tierLevel());
        assertEquals(3500, tierClassifier.classify(711L, "tennis").softCapMmr());
    }

    @Test
    @DisplayName("Football Draws properly increment draws and reset streaks without counting as loss")
    void testFootballDrawHandling() {
        GlickoPlayer home = GlickoPlayer.builder()
                .playerId(17L)
                .sportSlug("football")
                .surface("ALL")
                .mmr(2100.0)
                .rd(50.0)
                .streak(5)
                .wins(10)
                .losses(2)
                .draws(3)
                .build();

        GlickoPlayer away = GlickoPlayer.builder()
                .playerId(2829L)
                .sportSlug("football")
                .surface("ALL")
                .mmr(2120.0)
                .rd(50.0)
                .streak(-2)
                .wins(12)
                .losses(4)
                .draws(2)
                .build();

        TournamentTierClassifier.TierInfo tier1 = TournamentTierClassifier.FB_TIER_1;
        engine.updateMatch(home, away, 1, 1, tier1, 1.0, 75.0, 1.2, 1.1);

        assertEquals(4, home.getDraws(), "Home draws should increment to 4");
        assertEquals(3, away.getDraws(), "Away draws should increment to 3");
        assertEquals(2, home.getLosses(), "Home losses must not change on draw");
        assertEquals(4, away.getLosses(), "Away losses must not change on draw");
        assertEquals(0, home.getStreak(), "Draw resets streak to 0");
        assertEquals(0, away.getStreak(), "Draw resets streak to 0");
    }

    @Test
    @DisplayName("UNKNOWN Tier does NOT damp elite teams")
    void testUnknownTierDoesNotDampEliteClubs() {
        GlickoPlayer manCity = GlickoPlayer.builder()
                .playerId(17L)
                .sportSlug("football")
                .surface("ALL")
                .mmr(2200.0)
                .rd(50.0)
                .build();

        GlickoPlayer opp = GlickoPlayer.builder()
                .playerId(999L)
                .sportSlug("football")
                .surface("ALL")
                .mmr(2150.0)
                .rd(50.0)
                .build();

        // Match in an UNKNOWN unclassified tournament
        MultiTierGlickoEngine.MatchUpdateResult res = engine.updateMatch(
                manCity, opp, 3, 1, TournamentTierClassifier.UNKNOWN, 1.16, 75.0, 2.5, 0.8
        );

        assertTrue(res.deltaP1() > 0.0, "Delta must be positive on win in unknown tier");
        assertTrue(manCity.getMmr() > 2200.0, "Elite team must NOT be capped by unclassified tournament!");
    }
}

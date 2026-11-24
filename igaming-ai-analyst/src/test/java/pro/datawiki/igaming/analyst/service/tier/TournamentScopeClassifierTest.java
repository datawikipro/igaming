package pro.datawiki.igaming.analyst.service.tier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TournamentScopeClassifierTest {

    private TournamentScopeClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new TournamentScopeClassifier();
    }

    @Test
    @DisplayName("Should classify adult elite leagues as PRO_SENIOR")
    void testAdultEliteLeagues() {
        MatchAiRecommendation rec1 = MatchAiRecommendation.builder()
                .leagueName("Premier League")
                .team1Name("Arsenal")
                .team2Name("Chelsea")
                .tournamentSlug("premier-league")
                .build();
        assertEquals(TournamentTierScope.PRO_SENIOR, classifier.classify(rec1, null));

        MatchAiRecommendation rec2 = MatchAiRecommendation.builder()
                .leagueName("LaLiga")
                .team1Name("Real Madrid")
                .team2Name("Barcelona")
                .tournamentSlug("laliga")
                .build();
        assertEquals(TournamentTierScope.PRO_SENIOR, classifier.classify(rec2, null));

        MatchAiRecommendation rec3 = MatchAiRecommendation.builder()
                .leagueName("UEFA Champions League")
                .team1Name("Bayern Munich")
                .team2Name("PSG")
                .tournamentSlug("uefa-champions-league")
                .build();
        assertEquals(TournamentTierScope.PRO_SENIOR, classifier.classify(rec3, null));
    }

    @Test
    @DisplayName("Should classify youth leagues and tournaments as YOUTH_AMATEUR")
    void testYouthLeagues() {
        MatchAiRecommendation u19 = MatchAiRecommendation.builder()
                .leagueName("Spain Youth League U19")
                .team1Name("Atletico Madrid")
                .team2Name("Real Madrid")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(u19, null));

        MatchAiRecommendation primavera = MatchAiRecommendation.builder()
                .leagueName("Campionato Primavera 1")
                .team1Name("Inter")
                .team2Name("Milan")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(primavera, null));

        MatchAiRecommendation u21 = MatchAiRecommendation.builder()
                .leagueName("Premier League 2 Division 1 U21")
                .team1Name("Manchester City")
                .team2Name("Liverpool")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(u21, null));

        MatchAiRecommendation youthRu = MatchAiRecommendation.builder()
                .leagueName("Юношеская футбольная лига")
                .team1Name("Зенит")
                .team2Name("Спартак")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(youthRu, null));
    }

    @Test
    @DisplayName("Should classify youth teams by team suffix as YOUTH_AMATEUR")
    void testYouthTeams() {
        MatchAiRecommendation rec1 = MatchAiRecommendation.builder()
                .leagueName("Regional Tournament")
                .team1Name("Benfica U19")
                .team2Name("Porto U19")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(rec1, null));

        MatchAiRecommendation rec2 = MatchAiRecommendation.builder()
                .leagueName("Friendly Cup")
                .team1Name("Ajax (Youth)")
                .team2Name("Feyenoord")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(rec2, null));

        MatchAiRecommendation rec3 = MatchAiRecommendation.builder()
                .leagueName("Товарищеский турнир")
                .team1Name("ЦСКА (мол)")
                .team2Name("Динамо (мол)")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(rec3, null));
    }

    @Test
    @DisplayName("Should classify closed amateur series (Liga Pro, Setka Cup, TT Cup) as YOUTH_AMATEUR")
    void testAmateurSeries() {
        MatchAiRecommendation ligaPro = MatchAiRecommendation.builder()
                .leagueName("Liga Pro. Men")
                .team1Name("Ivanov I.")
                .team2Name("Petrov P.")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(ligaPro, null));

        MatchAiRecommendation setkaCup = MatchAiRecommendation.builder()
                .leagueName("Setka Cup")
                .team1Name("Kovalev A.")
                .team2Name("Sidorov B.")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(setkaCup, null));

        MatchAiRecommendation ttCup = MatchAiRecommendation.builder()
                .leagueName("TT Cup")
                .team1Name("Smirnov V.")
                .team2Name("Popov N.")
                .build();
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(ttCup, null));
    }

    @Test
    @DisplayName("Should classify by dossier metadata age_category and tier")
    void testDossierMetadata() {
        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .leagueName("Generic Cup")
                .team1Name("Team A")
                .team2Name("Team B")
                .build();

        // Adult metadata Tier 1
        Map<String, Object> adultMeta = Map.of(
                "tier", 1,
                "age_category", "SENIOR"
        );
        assertEquals(TournamentTierScope.PRO_SENIOR, classifier.classify(rec, adultMeta));

        // Youth metadata Tier 6
        Map<String, Object> youthMeta = Map.of(
                "tier", 6,
                "age_category", "U19"
        );
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(rec, youthMeta));

        // Amateur closed metadata Tier 7
        Map<String, Object> amateurMeta = Map.of(
                "tier", 7
        );
        assertEquals(TournamentTierScope.YOUTH_AMATEUR, classifier.classify(rec, amateurMeta));
    }
}

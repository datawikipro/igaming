package pro.datawiki.igaming.analyst.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.analyst.model.TacticalMatchDossier;
import pro.datawiki.igaming.analyst.model.TacticalTensor;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;
import pro.datawiki.igaming.dto.CanonicalMatchXgDto;
import pro.datawiki.igaming.dto.CanonicalShotMapDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveXgKafkaConsumerTest {

    private TacticalDossierService dossierService;
    private ReactiveXgKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        dossierService = new TacticalDossierService();
        consumer = new ReactiveXgKafkaConsumer(dossierService);
    }

    @Test
    void shouldConsumeAndEnrichMatchDossierFromXg() {
        CanonicalMatchXgDto xgDto = CanonicalMatchXgDto.builder()
                .matchId("premier-league-match-01")
                .sport("FOOTBALL")
                .competition("Premier League")
                .homeTeam("Manchester City")
                .awayTeam("Real Madrid")
                .homeScore(3)
                .awayScore(1)
                .homeXg(3.12)
                .awayXg(0.94)
                .homeSca(32)
                .awaySca(11)
                .homeGca(5)
                .awayGca(1)
                .homeNpxg(2.75)
                .awayNpxg(0.94)
                .build();

        consumer.consumeMatchXg(xgDto);

        TacticalMatchDossier dossier = dossierService.getDossier("premier-league-match-01");
        assertThat(dossier).isNotNull();
        assertThat(dossier.getHomeTeam()).isEqualTo("Manchester City");
        assertThat(dossier.getXgDifferential()).isCloseTo(2.18, org.assertj.core.data.Offset.offset(0.001));
        assertThat(dossier.getTacticalClassification()).isEqualTo("HOME_ATTACK_DOMINANCE");
        assertThat(dossier.getKeyFindings()).isNotEmpty();
    }

    @Test
    void shouldConsumeAndEnrichTacticalTensorFromShotMap() {
        CanonicalShotMapDto shotMapDto = CanonicalShotMapDto.builder()
                .matchId("premier-league-match-01")
                .homeTeam("Manchester City")
                .awayTeam("Real Madrid")
                .shots(List.of(
                        CanonicalShotMapDto.CanonicalShotDetailDto.builder()
                                .shotId("s1")
                                .minute(15)
                                .result("Goal")
                                .x(0.95)
                                .y(0.48)
                                .xg(0.55)
                                .player("Erling Haaland")
                                .teamSide("h")
                                .situation("OpenPlay")
                                .build(),
                        CanonicalShotMapDto.CanonicalShotDetailDto.builder()
                                .shotId("s2")
                                .minute(42)
                                .result("SavedShot")
                                .x(0.85)
                                .y(0.30)
                                .xg(0.20)
                                .player("Kevin De Bruyne")
                                .teamSide("h")
                                .situation("OpenPlay")
                                .build(),
                        CanonicalShotMapDto.CanonicalShotDetailDto.builder()
                                .shotId("s3")
                                .minute(60)
                                .result("Goal")
                                .x(0.86)
                                .y(0.55)
                                .xg(0.35)
                                .player("Vinicius Junior")
                                .teamSide("a")
                                .situation("FastBreak")
                                .build()
                ))
                .build();

        consumer.consumeShotMap(shotMapDto);

        TacticalTensor tensor = dossierService.getTensor("premier-league-match-01");
        assertThat(tensor).isNotNull();
        assertThat(tensor.getHomeSpatialDangerMatrix()).isNotNull();
        assertThat(tensor.getHomeDangerZones()).containsKey("SIX_YARD_BOX");
        assertThat(tensor.getAwayDangerZones()).containsKey("PENALTY_BOX");

        TacticalMatchDossier dossier = dossierService.getDossier("premier-league-match-01");
        assertThat(dossier.getTotalShots()).isEqualTo(3);
        assertThat(dossier.getHomeShots()).isEqualTo(2);
        assertThat(dossier.getAwayShots()).isEqualTo(1);
    }
}

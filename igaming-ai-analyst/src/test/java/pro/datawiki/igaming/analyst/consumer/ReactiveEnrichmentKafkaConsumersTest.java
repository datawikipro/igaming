package pro.datawiki.igaming.analyst.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.analyst.model.TacticalMatchDossier;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;
import pro.datawiki.igaming.dto.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReactiveEnrichmentKafkaConsumersTest {

    private TacticalDossierService tacticalDossierService;
    private ObjectMapper objectMapper;

    private ReactiveInjuryKafkaConsumer injuryConsumer;
    private ReactiveWeatherKafkaConsumer weatherConsumer;
    private ReactiveRefereeKafkaConsumer refereeConsumer;
    private ReactiveSmartMoneyKafkaConsumer smartMoneyConsumer;
    private ReactiveTennisKafkaConsumer tennisConsumer;

    @BeforeEach
    void setUp() {
        tacticalDossierService = new TacticalDossierService();
        objectMapper = new ObjectMapper();

        injuryConsumer = new ReactiveInjuryKafkaConsumer(tacticalDossierService, objectMapper);
        weatherConsumer = new ReactiveWeatherKafkaConsumer(tacticalDossierService, objectMapper);
        refereeConsumer = new ReactiveRefereeKafkaConsumer(tacticalDossierService, objectMapper);
        smartMoneyConsumer = new ReactiveSmartMoneyKafkaConsumer(tacticalDossierService, objectMapper);
        tennisConsumer = new ReactiveTennisKafkaConsumer(tacticalDossierService, objectMapper);
    }

    @Test
    void testConsumeInjuryReport() {
        String matchId = "match-epl-001";
        CanonicalInjuryReportDto injuryDto = CanonicalInjuryReportDto.builder()
                .matchId(matchId)
                .sport("football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .homeAbsenceImpact(0.12)
                .awayAbsenceImpact(0.48)
                .netAdvantageHome(0.36)
                .confidenceScore(0.98)
                .homeAbsentPlayers(List.of("Player A"))
                .awayAbsentPlayers(List.of("Striker B", "Midfielder C"))
                .build();

        injuryConsumer.consumeInjuryReport(injuryDto);

        TacticalMatchDossier dossier = tacticalDossierService.getDossier(matchId);
        assertNotNull(dossier, "Dossier should be reactively created/enriched");
        assertEquals(0.36, dossier.getNetInjuryAdvantageHome(), 0.001);
        assertEquals(0.48, dossier.getAwayAbsenceImpact(), 0.001);
        assertTrue(dossier.getKeyFindings().stream().anyMatch(f -> f.contains("Net home advantage")),
                "Key findings should document the absent players");
    }

    @Test
    void testConsumeWeatherForecast() {
        String matchId = "match-epl-001";
        CanonicalWeatherDto weatherDto = CanonicalWeatherDto.builder()
                .matchId(matchId)
                .sportName("football")
                .condition("HEAVY_RAIN")
                .temperatureC(8.5)
                .windSpeedKmh(34.0)
                .precipitationProbability(0.90)
                .underBias(-0.35)
                .outdoor(true)
                .build();

        weatherConsumer.consumeWeather(weatherDto);

        TacticalMatchDossier dossier = tacticalDossierService.getDossier(matchId);
        assertNotNull(dossier);
        assertEquals("HEAVY_RAIN", dossier.getWeatherCondition());
        assertEquals(-0.35, dossier.getWeatherUnderBias(), 0.001);
        assertEquals(34.0, dossier.getWeatherWindSpeedKmh(), 0.001);
        assertTrue(dossier.getKeyFindings().stream().anyMatch(f -> f.contains("Weather totals compression")),
                "Dossier should log weather compression impact");
    }

    @Test
    void testConsumeRefereeDisciplinaryProfile() {
        String matchId = "match-epl-001";
        CanonicalRefereeDto refereeDto = CanonicalRefereeDto.builder()
                .matchId(matchId)
                .refereeName("Michael Oliver")
                .country("England")
                .strictnessIndex(8.2)
                .strictnessTier("VERY_STRICT")
                .yellowCardsPerMatch(5.4)
                .redCardsPerMatch(0.28)
                .foulsPerMatch(26.0)
                .penaltiesPerMatch(0.42)
                .build();

        refereeConsumer.consumeReferee(refereeDto);

        TacticalMatchDossier dossier = tacticalDossierService.getDossier(matchId);
        assertNotNull(dossier);
        assertEquals("Michael Oliver", dossier.getRefereeName());
        assertEquals(8.2, dossier.getRefereeStrictnessIndex(), 0.001);
        assertEquals("VERY_STRICT", dossier.getRefereeStrictnessTier());
        assertEquals(5.4, dossier.getRefereeYellowCardsPerMatch(), 0.001);
        assertTrue(dossier.getKeyFindings().stream().anyMatch(f -> f.contains("VERY_STRICT")),
                "Dossier should contain referee strictness finding");
    }

    @Test
    void testConsumeSmartMoneyAlert() {
        String matchId = "match-epl-001";
        CanonicalSmartMoneyAlertDto alert = CanonicalSmartMoneyAlertDto.builder()
                .alertId("alert-betfair-999")
                .eventId(matchId)
                .sharpSide("Arsenal -0.75")
                .matchedVolumeEur(85000.0)
                .priceDisparity(0.12)
                .signalStrength(0.92)
                .description("Sharp institutional syndicates loading Arsenal handicap")
                .build();

        smartMoneyConsumer.consumeSmartMoney(alert);

        TacticalMatchDossier dossier = tacticalDossierService.getDossier(matchId);
        assertNotNull(dossier);
        assertEquals(1, dossier.getSmartMoneyAlertCount());
        assertEquals("Arsenal -0.75", dossier.getSmartMoneyDominantSide());
        assertEquals(85000.0, dossier.getSmartMoneyVolumeEur(), 0.001);
        assertTrue(dossier.getKeyFindings().stream().anyMatch(f -> f.contains("Exchange Sharp Flow")),
                "Dossier should log smart money flow");
    }

    @Test
    void testConsumeTennisPointByPointMomentum() {
        String matchId = "tennis-alcaraz-sinner";
        CanonicalTennisPointDto tennisDto = CanonicalTennisPointDto.builder()
                .matchId(matchId)
                .playerName("Carlos Alcaraz")
                .surface("Clay")
                .firstServePct(0.72)
                .secondServeWinPct(0.61)
                .momentumScore(0.84)
                .build();

        tennisConsumer.consumeTennisPoints(tennisDto);

        TacticalMatchDossier dossier = tacticalDossierService.getDossier(matchId);
        assertNotNull(dossier);
        assertEquals(0.72, dossier.getTennisFirstServePct(), 0.001);
        assertEquals(0.84, dossier.getTennisMomentumScore(), 0.001);
        assertTrue(dossier.getKeyFindings().stream().anyMatch(f -> f.contains("Tennis In-Play Momentum")),
                "Dossier should log tennis momentum");
    }
}

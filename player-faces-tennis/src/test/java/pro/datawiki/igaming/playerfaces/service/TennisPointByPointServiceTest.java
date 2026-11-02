package pro.datawiki.igaming.playerfaces.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TennisSurface;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.TennisPointByPointEvent;
import pro.datawiki.igaming.playerfaces.harvester.TennisAbstractHarvester;
import pro.datawiki.igaming.playerfaces.harvester.TennisExplorerHarvester;
import pro.datawiki.igaming.playerfaces.repository.PlayerFaceRepository;
import pro.datawiki.igaming.playerfaces.sport.TennisSport;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TennisPointByPointServiceTest {

    private TennisPointByPointService service;
    private PlayerFaceRepository repository;
    private TennisPointByPointKafkaProducer kafkaProducer;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(PlayerFaceRepository.class);
        kafkaProducer = Mockito.mock(TennisPointByPointKafkaProducer.class);
        TennisMdmPlayerResolver mdmResolver = new TennisMdmPlayerResolver(repository);
        TennisSport tennisSport = new TennisSport();

        TennisAbstractHarvester abstractHarvester = new TennisAbstractHarvester(new RestTemplateBuilder());
        TennisExplorerHarvester explorerHarvester = new TennisExplorerHarvester(new RestTemplateBuilder());

        service = new TennisPointByPointService(
                List.of(abstractHarvester, explorerHarvester),
                mdmResolver,
                kafkaProducer,
                tennisSport
        );
    }

    @Test
    @DisplayName("Should harvest stats, resolve MDM entity, and stream event to Kafka")
    void testHarvestAndPublish() {
        PlayerFace mockPlayer = PlayerFace.builder()
                .id(101L)
                .playerName("Jannik Sinner")
                .normalizedName("jannik sinner")
                .aliases("Sinner J.;Jannik Sinner")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .countryCode("ITA")
                .ranking(1)
                .avatarUrl("https://cdn.smartbet.guru/avatars/sinner.webp")
                .build();

        when(repository.findFirstByNormalizedName("jannik sinner")).thenReturn(Optional.of(mockPlayer));

        TennisPointByPointEvent event = service.harvestAndPublish("Jannik Sinner", TourType.ATP);

        assertThat(event).isNotNull();
        assertThat(event.getMdmPlayerId()).isEqualTo(101L);
        assertThat(event.getCountryCode()).isEqualTo("ITA");
        assertThat(event.getCurrentRanking()).isEqualTo(1);
        assertThat(event.getSurfaceStats()).containsKeys(TennisSurface.HARD, TennisSurface.CLAY, TennisSurface.GRASS, TennisSurface.INDOOR);

        // Verify Kafka dispatch
        verify(kafkaProducer).publish(any(TennisPointByPointEvent.class));
    }

    @Test
    @DisplayName("Should evaluate surface match edge between two players")
    void testEvaluateMatchEdge() {
        PlayerFace sinner = PlayerFace.builder()
                .id(101L)
                .playerName("Jannik Sinner")
                .normalizedName("jannik sinner")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .build();

        PlayerFace alcaraz = PlayerFace.builder()
                .id(102L)
                .playerName("Carlos Alcaraz")
                .normalizedName("carlos alcaraz")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .build();

        when(repository.findFirstByNormalizedName("jannik sinner")).thenReturn(Optional.of(sinner));
        when(repository.findFirstByNormalizedName("carlos alcaraz")).thenReturn(Optional.of(alcaraz));

        Map<String, Object> edgeResult = service.evaluateMatchEdge("Jannik Sinner", "Carlos Alcaraz", "Hard", TourType.ATP);

        assertThat(edgeResult).containsKeys("surface", "player1", "player2", "p1HoldProbability", "p2HoldProbability", "surfaceEdge");
        assertThat(edgeResult.get("surface")).isEqualTo("HARD");
    }
}

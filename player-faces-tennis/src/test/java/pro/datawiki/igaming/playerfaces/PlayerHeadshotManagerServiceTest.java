package pro.datawiki.igaming.playerfaces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.HarvestResultDto;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchRequest;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchResponse;
import pro.datawiki.igaming.playerfaces.dto.PlayerFaceDto;
import pro.datawiki.igaming.playerfaces.dto.TennisMatchCardDto;
import pro.datawiki.igaming.playerfaces.harvester.PlayerHeadshotHarvester;
import pro.datawiki.igaming.playerfaces.repository.PlayerFaceRepository;
import pro.datawiki.igaming.playerfaces.service.PlayerAvatarCdnService;
import pro.datawiki.igaming.playerfaces.service.PlayerHeadshotManagerService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@ActiveProfiles("test")
class PlayerHeadshotManagerServiceTest {

    @Autowired
    private PlayerFaceRepository repository;

    private PlayerAvatarCdnService cdnService;
    private PlayerHeadshotHarvester mockHarvester1;
    private PlayerHeadshotHarvester mockHarvester2;
    private PlayerHeadshotManagerService managerService;

    @BeforeEach
    void setUp() {
        cdnService = new PlayerAvatarCdnService();
        mockHarvester1 = mock(PlayerHeadshotHarvester.class);
        mockHarvester2 = mock(PlayerHeadshotHarvester.class);

        when(mockHarvester1.getProvider()).thenReturn(AvatarSourceProvider.THE_SPORTS_DB);
        when(mockHarvester1.getOrder()).thenReturn(10);
        when(mockHarvester1.isAvailable()).thenReturn(true);

        when(mockHarvester2.getProvider()).thenReturn(AvatarSourceProvider.WIKIDATA);
        when(mockHarvester2.getOrder()).thenReturn(20);
        when(mockHarvester2.isAvailable()).thenReturn(true);

        managerService = new PlayerHeadshotManagerService(
                repository,
                cdnService,
                List.of(mockHarvester2, mockHarvester1) // Order should be sorted
        );
        managerService.init();
    }

    @Test
    @DisplayName("Should return existing player profile without calling harvesters")
    void testReturnCachedPlayer() {
        PlayerFace existing = repository.save(PlayerFace.builder()
                .playerName("Novak Djokovic")
                .normalizedName("novak djokovic")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .avatarUrl("https://cdn.example.com/nole.webp")
                .sourceProvider(AvatarSourceProvider.THE_SPORTS_DB)
                .lastHarvestedAt(LocalDateTime.now())
                .build());

        PlayerFaceDto resolved = managerService.getOrHarvest("Novak Djokovic", SportType.TENNIS, TourType.ATP);

        assertThat(resolved).isNotNull();
        assertThat(resolved.getPlayerName()).isEqualTo("Novak Djokovic");
        assertThat(resolved.getAvatarUrl()).isEqualTo("https://cdn.example.com/nole.webp");
        verify(mockHarvester1, never()).harvest(any(), any(), any());
        verify(mockHarvester2, never()).harvest(any(), any(), any());
    }

    @Test
    @DisplayName("Should harvest via higher priority harvester if not cached")
    void testHarvestViaFirstHarvester() {
        PlayerFaceDto harvestedDto = PlayerFaceDto.builder()
                .playerName("Carlos Alcaraz")
                .avatarUrl("https://cdn.example.com/alcaraz.png")
                .sourceProvider(AvatarSourceProvider.THE_SPORTS_DB)
                .country("Spain")
                .countryCode("ESP")
                .ranking(2)
                .seedNumber(2)
                .build();

        when(mockHarvester1.harvest(eq("Carlos Alcaraz"), eq(SportType.TENNIS), eq(TourType.ATP)))
                .thenReturn(Optional.of(harvestedDto));

        PlayerFaceDto result = managerService.getOrHarvest("Carlos Alcaraz", SportType.TENNIS, TourType.ATP);

        assertThat(result).isNotNull();
        assertThat(result.getPlayerName()).isEqualTo("Carlos Alcaraz");
        assertThat(result.getAvatarUrl()).isEqualTo("https://cdn.example.com/alcaraz.png");
        assertThat(result.getSourceProvider()).isEqualTo(AvatarSourceProvider.THE_SPORTS_DB);

        // Verify entity was persisted in DB
        Optional<PlayerFace> inDb = repository.findFirstByNormalizedName("carlos alcaraz");
        assertThat(inDb).isPresent();
        assertThat(inDb.get().getAvatarUrl()).isEqualTo("https://cdn.example.com/alcaraz.png");

        // Harvester 2 should not have been called because Harvester 1 succeeded
        verify(mockHarvester2, never()).harvest(any(), any(), any());
    }

    @Test
    @DisplayName("Should generate Cyberpunk SVG fallback when all external sources return empty")
    void testFallbackWhenAllHarvestersFail() {
        when(mockHarvester1.harvest(any(), any(), any())).thenReturn(Optional.empty());
        when(mockHarvester2.harvest(any(), any(), any())).thenReturn(Optional.empty());

        PlayerFaceDto fallback = managerService.getOrHarvest("Unknown Tennis Pro", SportType.TENNIS, TourType.ATP);

        assertThat(fallback).isNotNull();
        assertThat(fallback.getPlayerName()).isEqualTo("Unknown Tennis Pro");
        assertThat(fallback.getSourceProvider()).isEqualTo(AvatarSourceProvider.FALLBACK_CYBERPUNK);
        assertThat(fallback.getAvatarUrl()).contains("/cdn/avatars/unknown-tennis-pro.svg");

        Optional<PlayerFace> inDb = repository.findFirstByNormalizedName("unknown tennis pro");
        assertThat(inDb).isPresent();
        assertThat(inDb.get().getSourceProvider()).isEqualTo(AvatarSourceProvider.FALLBACK_CYBERPUNK);
    }

    @Test
    @DisplayName("Should batch resolve multiple players")
    void testResolveBatch() {
        when(mockHarvester1.harvest(any(), any(), any())).thenReturn(Optional.empty());

        PlayerBatchRequest request = PlayerBatchRequest.builder()
                .playerNames(List.of("Jannik Sinner", "Daniil Medvedev"))
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .build();

        PlayerBatchResponse response = managerService.resolveBatch(request);

        assertThat(response.getTotalRequested()).isEqualTo(2);
        assertThat(response.getTotalResolved()).isEqualTo(2);
        assertThat(response.getPlayers()).containsKey("Jannik Sinner");
        assertThat(response.getPlayers()).containsKey("Daniil Medvedev");
    }

    @Test
    @DisplayName("Should enrich TennisMatchCard and calculate surebet arbitrage profit")
    void testEnrichTennisMatchCard() {
        TennisMatchCardDto card = TennisMatchCardDto.builder()
                .matchId("card-101")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .player1(PlayerFaceDto.builder().playerName("Novak Djokovic").seedNumber(1).build())
                .player2(PlayerFaceDto.builder().playerName("Carlos Alcaraz").seedNumber(2).build())
                .odds1(2.10)
                .odds2(2.10)
                .build();

        when(mockHarvester1.harvest(any(), any(), any())).thenReturn(Optional.empty());

        TennisMatchCardDto enriched = managerService.enrichTennisMatchCard(card);

        assertThat(enriched).isNotNull();
        assertThat(enriched.getPlayer1().getAvatarUrl()).isNotNull();
        assertThat(enriched.getPlayer2().getAvatarUrl()).isNotNull();

        // 1/2.10 + 1/2.10 = 0.47619 + 0.47619 = 0.95238
        // Profit % = (1 - 0.95238) * 100 = 4.76%
        assertThat(enriched.getSurebetProfitPercent()).isNotNull();
        assertThat(enriched.getSurebetProfitPercent()).isGreaterThan(4.0);
    }

    @Test
    @DisplayName("Should seed catalog with top 50 athletes across ATP, WTA, and MMA")
    void testSeedTopAthletesCatalog() {
        int seeded = managerService.seedTopAthletesCatalog();

        assertThat(seeded).isEqualTo(50);
        assertThat(repository.count()).isEqualTo(50);

        List<PlayerFace> tennisPlayers = repository.findBySport(SportType.TENNIS);
        List<PlayerFace> mmaFighters = repository.findBySport(SportType.MMA);

        assertThat(tennisPlayers).hasSize(35); // 20 ATP + 15 WTA
        assertThat(mmaFighters).hasSize(15);  // 15 UFC/MMA

        // Verify specific athletes
        assertThat(repository.findFirstByNormalizedName("novak djokovic")).isPresent();
        assertThat(repository.findFirstByNormalizedName("iga swiatek")).isPresent();
        assertThat(repository.findFirstByNormalizedName("islam makhachev")).isPresent();
    }

    @Test
    @DisplayName("Should perform force harvest and return execution metrics")
    void testForceHarvest() {
        PlayerFaceDto harvestedDto = PlayerFaceDto.builder()
                .playerName("Alexander Zverev")
                .avatarUrl("https://cdn.example.com/zverev.webp")
                .sourceProvider(AvatarSourceProvider.THE_SPORTS_DB)
                .build();

        when(mockHarvester1.harvest(eq("Alexander Zverev"), eq(SportType.TENNIS), any()))
                .thenReturn(Optional.of(harvestedDto));

        HarvestResultDto result = managerService.forceHarvest("Alexander Zverev", SportType.TENNIS, TourType.ATP);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPlayerName()).isEqualTo("Alexander Zverev");
        assertThat(result.getProvider()).isEqualTo(AvatarSourceProvider.THE_SPORTS_DB);
        assertThat(result.getDurationMs()).isGreaterThanOrEqualTo(0);
    }
}

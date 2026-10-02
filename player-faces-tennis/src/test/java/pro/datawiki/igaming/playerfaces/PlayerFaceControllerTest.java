package pro.datawiki.igaming.playerfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.HarvestResultDto;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchRequest;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchResponse;
import pro.datawiki.igaming.playerfaces.dto.PlayerFaceDto;
import pro.datawiki.igaming.playerfaces.dto.TennisMatchCardDto;
import pro.datawiki.igaming.playerfaces.repository.PlayerFaceRepository;
import pro.datawiki.igaming.playerfaces.service.PlayerHeadshotManagerService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlayerFaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerHeadshotManagerService headshotManagerService;

    @MockBean
    private PlayerFaceRepository playerFaceRepository;

    private PlayerFaceDto samplePlayer;

    @BeforeEach
    void setUp() {
        samplePlayer = PlayerFaceDto.builder()
                .id(1L)
                .playerName("Novak Djokovic")
                .normalizedName("novak djokovic")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .ranking(1)
                .seedNumber(1)
                .country("Serbia")
                .countryCode("SRB")
                .avatarUrl("https://cdn.example.com/djokovic.png")
                .sourceProvider(AvatarSourceProvider.THE_SPORTS_DB)
                .build();
    }

    @Test
    @DisplayName("GET /api/players/faces/by-name should return resolved player")
    void testGetByName() throws Exception {
        when(headshotManagerService.getOrHarvest(eq("Novak Djokovic"), any(), any()))
                .thenReturn(samplePlayer);

        mockMvc.perform(get("/api/players/faces/by-name")
                        .param("name", "Novak Djokovic")
                        .param("sport", "TENNIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName").value("Novak Djokovic"))
                .andExpect(jsonPath("$.countryCode").value("SRB"))
                .andExpect(jsonPath("$.ranking").value(1));
    }

    @Test
    @DisplayName("POST /api/players/faces/batch should resolve multiple player faces")
    void testBatchResolve() throws Exception {
        PlayerBatchRequest request = PlayerBatchRequest.builder()
                .playerNames(List.of("Novak Djokovic"))
                .sport(SportType.TENNIS)
                .build();

        PlayerBatchResponse response = PlayerBatchResponse.builder()
                .players(Map.of("Novak Djokovic", samplePlayer))
                .totalRequested(1)
                .totalResolved(1)
                .totalGenerated(0)
                .build();

        when(headshotManagerService.resolveBatch(any())).thenReturn(response);

        mockMvc.perform(post("/api/players/faces/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.players['Novak Djokovic'].playerName").value("Novak Djokovic"));
    }

    @Test
    @DisplayName("POST /api/players/faces/harvest should force re-harvest and return metric")
    void testForceHarvestEndpoint() throws Exception {
        HarvestResultDto harvestResult = HarvestResultDto.builder()
                .playerName("Carlos Alcaraz")
                .success(true)
                .avatarUrl("https://cdn.example.com/alcaraz.webp")
                .provider(AvatarSourceProvider.THE_SPORTS_DB)
                .durationMs(142)
                .build();

        when(headshotManagerService.forceHarvest(eq("Carlos Alcaraz"), any(), any()))
                .thenReturn(harvestResult);

        mockMvc.perform(post("/api/players/faces/harvest")
                        .param("name", "Carlos Alcaraz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName").value("Carlos Alcaraz"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/players/faces/top should return top ranked players")
    void testGetTopPlayers() throws Exception {
        when(headshotManagerService.getTopPlayers(SportType.TENNIS))
                .thenReturn(List.of(samplePlayer));

        mockMvc.perform(get("/api/players/faces/top")
                        .param("sport", "TENNIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerName").value("Novak Djokovic"));
    }

    @Test
    @DisplayName("POST /api/players/faces/card should enrich tennis match card")
    void testEnrichCard() throws Exception {
        TennisMatchCardDto card = TennisMatchCardDto.builder()
                .matchId("m-1")
                .sport(SportType.TENNIS)
                .odds1(1.95)
                .odds2(2.15)
                .surebetProfitPercent(3.15)
                .player1(samplePlayer)
                .build();

        when(headshotManagerService.enrichTennisMatchCard(any())).thenReturn(card);

        mockMvc.perform(post("/api/players/faces/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surebetProfitPercent").value(3.15))
                .andExpect(jsonPath("$.player1.playerName").value("Novak Djokovic"));
    }
}

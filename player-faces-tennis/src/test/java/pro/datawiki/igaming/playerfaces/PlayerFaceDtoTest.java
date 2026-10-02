package pro.datawiki.igaming.playerfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchRequest;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchResponse;
import pro.datawiki.igaming.playerfaces.dto.PlayerFaceDto;
import pro.datawiki.igaming.playerfaces.dto.TennisMatchCardDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerFaceDtoTest {

    @Test
    @DisplayName("Should convert between PlayerFace entity and PlayerFaceDto")
    void testEntityDtoMapping() {
        PlayerFace entity = PlayerFace.builder()
                .id(100L)
                .playerName("Aryna Sabalenka")
                .normalizedName("aryna sabalenka")
                .aliases("Sabalenka")
                .sport(SportType.TENNIS)
                .tour(TourType.WTA)
                .country("Belarus")
                .countryCode("BLR")
                .ranking(1)
                .seedNumber(1)
                .avatarUrl("https://upload.wikimedia.org/sabalenka.webp")
                .sourceProvider(AvatarSourceProvider.WIKIDATA)
                .webpOptimized(true)
                .lastHarvestedAt(LocalDateTime.now())
                .build();

        PlayerFaceDto dto = PlayerFaceDto.fromEntity(entity);
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals("Aryna Sabalenka", dto.getPlayerName());
        assertEquals("aryna sabalenka", dto.getNormalizedName());
        assertEquals(SportType.TENNIS, dto.getSport());
        assertEquals(TourType.WTA, dto.getTour());
        assertEquals(1, dto.getRanking());

        PlayerFace reconstructed = dto.toEntity();
        assertEquals(dto.getPlayerName(), reconstructed.getPlayerName());
        assertEquals(dto.getNormalizedName(), reconstructed.getNormalizedName());
        assertEquals(dto.getSport(), reconstructed.getSport());
    }

    @Test
    @DisplayName("Should construct TennisMatchCardDto with both players")
    void testTennisMatchCardDto() {
        PlayerFaceDto p1 = PlayerFaceDto.builder()
                .playerName("Novak Djokovic")
                .ranking(2)
                .seedNumber(2)
                .avatarUrl("/cdn/avatars/djokovic.webp")
                .build();

        PlayerFaceDto p2 = PlayerFaceDto.builder()
                .playerName("Carlos Alcaraz")
                .ranking(3)
                .seedNumber(3)
                .avatarUrl("/cdn/avatars/alcaraz.webp")
                .build();

        TennisMatchCardDto card = TennisMatchCardDto.builder()
                .matchId("m-wimbledon-final-2026")
                .tournamentName("Wimbledon")
                .round("Final")
                .surface("Grass")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .player1(p1)
                .player2(p2)
                .odds1(1.95)
                .odds2(1.92)
                .surebetProfitPercent(1.45)
                .currentScore("6-4, 4-6, 3-2")
                .status("LIVE")
                .build();

        assertNotNull(card);
        assertEquals("Wimbledon", card.getTournamentName());
        assertEquals("Novak Djokovic", card.getPlayer1().getPlayerName());
        assertEquals("Carlos Alcaraz", card.getPlayer2().getPlayerName());
        assertEquals(1.45, card.getSurebetProfitPercent());
    }

    @Test
    @DisplayName("Should build batch request and response")
    void testBatchDtos() {
        PlayerBatchRequest request = PlayerBatchRequest.builder()
                .playerNames(List.of("Novak Djokovic", "Carlos Alcaraz"))
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .build();

        assertEquals(2, request.getPlayerNames().size());

        PlayerFaceDto dto = PlayerFaceDto.builder().playerName("Novak Djokovic").build();
        PlayerBatchResponse response = PlayerBatchResponse.builder()
                .players(Map.of("novak djokovic", dto))
                .totalRequested(2)
                .totalResolved(1)
                .totalGenerated(0)
                .build();

        assertEquals(2, response.getTotalRequested());
        assertEquals(1, response.getTotalResolved());
        assertTrue(response.getPlayers().containsKey("novak djokovic"));
    }
}

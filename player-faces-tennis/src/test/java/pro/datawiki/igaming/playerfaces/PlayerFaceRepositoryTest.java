package pro.datawiki.igaming.playerfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.repository.PlayerFaceRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class PlayerFaceRepositoryTest {

    @Autowired
    private PlayerFaceRepository repository;

    @Test
    @DisplayName("Should persist and find player face by normalized name and sport")
    void testSaveAndFindByNormalizedName() {
        PlayerFace player = PlayerFace.builder()
                .playerName("Novak Đoković")
                .normalizedName(PlayerFace.normalizeName("Novak Đoković"))
                .aliases("Novak Djokovic, Nole, Djoker")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .country("Serbia")
                .countryCode("SRB")
                .flagUrl("https://flagcdn.com/w40/rs.png")
                .ranking(2)
                .seedNumber(2)
                .avatarUrl("https://upload.wikimedia.org/wikipedia/commons/djokovic.webp")
                .sourceUrl("https://www.wikidata.org/wiki/Q5812")
                .sourceProvider(AvatarSourceProvider.WIKIDATA)
                .webpOptimized(true)
                .lastHarvestedAt(LocalDateTime.now())
                .build();

        PlayerFace saved = repository.save(player);
        assertNotNull(saved.getId());

        Optional<PlayerFace> found = repository.findFirstByNormalizedName("novak djokovic");
        assertTrue(found.isPresent());
        assertEquals("Novak Đoković", found.get().getPlayerName());
        assertEquals(SportType.TENNIS, found.get().getSport());
        assertEquals(TourType.ATP, found.get().getTour());

        List<PlayerFace> atpPlayers = repository.findBySportAndTour(SportType.TENNIS, TourType.ATP);
        assertEquals(1, atpPlayers.size());

        List<PlayerFace> aliasMatches = repository.searchByNameOrAlias("djoker");
        assertFalse(aliasMatches.isEmpty());
        assertEquals("Novak Đoković", aliasMatches.get(0).getPlayerName());
    }

    @Test
    @DisplayName("Should find top players ordered by ranking")
    void testFindTopByRanking() {
        repository.save(PlayerFace.builder()
                .playerName("Jannik Sinner")
                .normalizedName("jannik sinner")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .ranking(1)
                .avatarUrl("/cdn/avatars/sinner.webp")
                .sourceProvider(AvatarSourceProvider.THE_SPORTS_DB)
                .lastHarvestedAt(LocalDateTime.now())
                .build());

        repository.save(PlayerFace.builder()
                .playerName("Carlos Alcaraz")
                .normalizedName("carlos alcaraz")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .ranking(3)
                .avatarUrl("/cdn/avatars/alcaraz.webp")
                .sourceProvider(AvatarSourceProvider.WIKIMEDIA)
                .lastHarvestedAt(LocalDateTime.now())
                .build());

        List<PlayerFace> topTennis = repository.findTop50BySportOrderByRankingAsc(SportType.TENNIS);
        assertEquals(2, topTennis.size());
        assertEquals(1, topTennis.get(0).getRanking());
        assertEquals(3, topTennis.get(1).getRanking());
    }
}

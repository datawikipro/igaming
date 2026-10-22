package pro.datawiki.igaming.capture.sofascore.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.service.SofaRawEventService;
import pro.datawiki.igaming.dto.MatchLineupsDto;
import pro.datawiki.igaming.dto.MatchStorylineDto;
import pro.datawiki.igaming.dto.tournament.StandardTournamentStage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SofaScoreEnrichmentProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SofaRawEventService rawEventService;

    @Mock
    private pro.datawiki.igaming.capture.sofascore.repository.SofaRawEventRepository rawEventRepository;

    private SofaScoreEnrichmentProvider provider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        provider = new SofaScoreEnrichmentProvider(restTemplate, objectMapper, rawEventService, rawEventRepository);
    }

    @Test
    void testParseLineupsWithSubstitutionsAndBench() {
        String json = """
        {
          "confirmed": true,
          "home": {
            "formation": "4-3-3",
            "players": [
              {
                "player": {"name": "Oscar Jimenez", "position": "G"},
                "jerseyNumber": "1",
                "substitute": false,
                "captain": true,
                "statistics": {"minutesPlayed": 90, "rating": 7.1}
              },
              {
                "player": {"name": "Aaron Quiros", "position": "D"},
                "jerseyNumber": "12",
                "substitute": false,
                "captain": false,
                "statistics": {"minutesPlayed": 76, "rating": 6.8}
              },
              {
                "player": {"name": "Christian Bermudez", "position": "M"},
                "jerseyNumber": "18",
                "substitute": true,
                "captain": false,
                "statistics": {"minutesPlayed": 14, "rating": 6.6}
              },
              {
                "player": {"name": "Unused Sub", "position": "F"},
                "jerseyNumber": "99",
                "substitute": true,
                "captain": false
              }
            ],
            "missingPlayers": [
              {
                "player": {"name": "Injured Star"},
                "reason": "Knee injury",
                "type": "INJURY"
              }
            ]
          },
          "away": {
            "formation": "4-4-2",
            "players": [
              {
                "player": {"name": "Away Goalkeeper", "position": "G"},
                "jerseyNumber": "1",
                "substitute": false,
                "statistics": {"minutesPlayed": 90}
              }
            ]
          }
        }
        """;

        MatchLineupsDto dto = provider.parseLineupsJson(json);
        assertNotNull(dto);
        assertTrue(dto.isConfirmed());
        assertEquals("4-3-3", dto.getFormationTeam1());
        assertEquals("4-4-2", dto.getFormationTeam2());

        // Starters Team 1
        assertNotNull(dto.getStartersTeam1());
        assertEquals(2, dto.getStartersTeam1().size());
        assertEquals("Oscar Jimenez", dto.getStartersTeam1().get(0).getName());
        assertFalse(dto.getStartersTeam1().get(0).isSubstituted());
        assertEquals("Aaron Quiros", dto.getStartersTeam1().get(1).getName());
        assertTrue(dto.getStartersTeam1().get(1).isSubstituted());
        assertEquals(76, dto.getStartersTeam1().get(1).getMinuteOut());

        // Bench Team 1
        assertNotNull(dto.getBenchTeam1());
        assertEquals(2, dto.getBenchTeam1().size());
        assertEquals("Christian Bermudez", dto.getBenchTeam1().get(0).getName());
        assertTrue(dto.getBenchTeam1().get(0).isSubstituted());
        assertEquals(76, dto.getBenchTeam1().get(0).getMinuteIn());
        assertEquals(14, dto.getBenchTeam1().get(0).getMinutesPlayed());

        // Unused sub
        assertEquals("Unused Sub", dto.getBenchTeam1().get(1).getName());
        assertFalse(dto.getBenchTeam1().get(1).isSubstituted());
        assertNull(dto.getBenchTeam1().get(1).getMinuteIn());

        // Substitutions Team 1
        assertNotNull(dto.getSubstitutionsTeam1());
        assertEquals(1, dto.getSubstitutionsTeam1().size());
        MatchLineupsDto.SubstitutionDto sub = dto.getSubstitutionsTeam1().get(0);
        assertEquals("Christian Bermudez", sub.getPlayerIn());
        assertEquals(18, sub.getPlayerInNumber());
        assertEquals("Aaron Quiros", sub.getPlayerOut());
        assertEquals(12, sub.getPlayerOutNumber());
        assertEquals(76, sub.getMinute());

        // Missing Team 1
        assertNotNull(dto.getMissingTeam1());
        assertEquals(1, dto.getMissingTeam1().size());
        assertEquals("Injured Star", dto.getMissingTeam1().get(0).getName());
        assertEquals("Knee injury", dto.getMissingTeam1().get(0).getReason());
        assertEquals("INJURY", dto.getMissingTeam1().get(0).getType());
    }

    @Test
    void testParseReferee() throws Exception {
        String eventJson = """
        {
          "event": {
            "referee": {
              "name": "Michael Oliver",
              "yellowCards": 48,
              "redCards": 2,
              "games": 10,
              "country": {"name": "England"}
            }
          }
        }
        """;
        var eventRoot = objectMapper.readTree(eventJson).path("event");
        MatchStorylineDto.RefereeDto ref = provider.parseReferee(eventRoot);

        assertNotNull(ref);
        assertEquals("Michael Oliver", ref.getName());
        assertEquals("England", ref.getCountry());
        assertEquals(10, ref.getGames());
        assertEquals(48, ref.getYellowCards());
        assertEquals(4.8, ref.getAvgYellowCards());
        assertEquals(0.2, ref.getAvgRedCards());
        assertEquals("STRICT", ref.getStrictness());
    }

    @Test
    void testParseVenueFriendlyNeutral() throws Exception {
        String eventJson = """
        {
          "event": {
            "tournament": {
              "name": "Club Friendly Games",
              "category": {"name": "World"}
            },
            "homeTeam": {
              "name": "Arsenal",
              "country": {"name": "England"}
            },
            "awayTeam": {
              "name": "Bayern München",
              "country": {"name": "Germany"}
            },
            "venue": {
              "stadium": {"name": "MetLife Stadium", "capacity": 82500},
              "city": {"name": "East Rutherford"},
              "country": {"name": "USA"},
              "neutralGround": true
            }
          }
        }
        """;
        var eventRoot = objectMapper.readTree(eventJson).path("event");
        MatchStorylineDto.VenueDto venue = provider.parseVenue(eventRoot, "Club Friendly Games");

        assertNotNull(venue);
        assertEquals("MetLife Stadium", venue.getStadiumName());
        assertEquals("East Rutherford", venue.getCityName());
        assertEquals("USA", venue.getCountryName());
        assertEquals(82500, venue.getCapacity());
        assertTrue(venue.getNeutralGround());
        assertTrue(venue.getExhibitionOrFriendly());
        assertNotNull(venue.getVenueContextNotice());
        assertTrue(venue.getVenueContextNotice().contains("Товарищеский выездной матч"));
    }

    @Test
    void testGenerateContextTags() {
        MatchStorylineDto.VenueDto venue = MatchStorylineDto.VenueDto.builder()
                .neutralGround(true)
                .exhibitionOrFriendly(true)
                .build();

        MatchStorylineDto.RefereeDto referee = MatchStorylineDto.RefereeDto.builder()
                .name("Mateu Lahoz")
                .avgYellowCards(5.2)
                .strictness("STRICT")
                .build();

        MatchLineupsDto lineups = MatchLineupsDto.builder()
                .substitutionsTeam1(List.of(
                        MatchLineupsDto.SubstitutionDto.builder().build(),
                        MatchLineupsDto.SubstitutionDto.builder().build(),
                        MatchLineupsDto.SubstitutionDto.builder().build(),
                        MatchLineupsDto.SubstitutionDto.builder().build()
                ))
                .substitutionsTeam2(List.of(
                        MatchLineupsDto.SubstitutionDto.builder().build(),
                        MatchLineupsDto.SubstitutionDto.builder().build(),
                        MatchLineupsDto.SubstitutionDto.builder().build(),
                        MatchLineupsDto.SubstitutionDto.builder().build()
                ))
                .missingTeam1(List.of(
                        MatchLineupsDto.MissingPlayerDto.builder().name("P1").build(),
                        MatchLineupsDto.MissingPlayerDto.builder().name("P2").build()
                ))
                .missingTeam2(List.of(
                        MatchLineupsDto.MissingPlayerDto.builder().name("P3").build(),
                        MatchLineupsDto.MissingPlayerDto.builder().name("P4").build()
                ))
                .build();

        List<String> tags = provider.generateContextTags(venue, referee, lineups, StandardTournamentStage.SEMI_FINAL);

        assertNotNull(tags);
        assertTrue(tags.stream().anyMatch(t -> t.contains("Глубокая ротация: 8 замен")));
        assertTrue(tags.stream().anyMatch(t -> t.contains("Кадровый дефицит (4 травм/дискв.)")));
        assertTrue(tags.stream().anyMatch(t -> t.contains("Выездной спарринг")));
        assertTrue(tags.stream().anyMatch(t -> t.contains("Риск низкой мотивации")));
        assertTrue(tags.stream().anyMatch(t -> t.contains("Строгий арбитр (5.2 ЖК)")));
        assertTrue(tags.stream().anyMatch(t -> t.contains("Матч на выбывание")));
    }
}

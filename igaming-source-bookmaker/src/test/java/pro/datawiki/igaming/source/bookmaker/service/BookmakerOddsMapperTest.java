package pro.datawiki.igaming.source.bookmaker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bookmaker.dto.BookmakerMatchDto;

import static org.junit.jupiter.api.Assertions.*;

class BookmakerOddsMapperTest {

    private BookmakerOddsMapper oddsMapper;

    @BeforeEach
    void setUp() {
        oddsMapper = new BookmakerOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(oddsMapper.supports("bookmaker", SportType.BASEBALL));
        assertTrue(oddsMapper.supports("bookmaker.eu", SportType.FOOTBALL));
        assertTrue(oddsMapper.supports("BOOKMAKER", SportType.BASKETBALL));
        assertFalse(oddsMapper.supports("pinnacle", SportType.BASEBALL));
    }

    @Test
    void testAmericanOddsConversion() {
        assertEquals(2.300, oddsMapper.parseAmericanOdds("+130"), 0.001);
        assertEquals(1.053, oddsMapper.parseAmericanOdds("-1900"), 0.001);
        assertEquals(1.625, oddsMapper.parseAmericanOdds("-160"), 0.001);
        assertEquals(10.000, oddsMapper.parseAmericanOdds("+900"), 0.001);
        assertNull(oddsMapper.parseAmericanOdds("-"));
        assertNull(oddsMapper.parseAmericanOdds(""));
    }

    @Test
    void testFractionLineParsing() {
        assertEquals(11.5, oddsMapper.parseLine("11½"), 0.001);
        assertEquals(1.5, oddsMapper.parseLine("+1½"), 0.001);
        assertEquals(-3.0, oddsMapper.parseLine("-3"), 0.001);
        assertEquals(8.5, oddsMapper.parseLine("8½"), 0.001);
    }

    @Test
    void testMapBookmakerMatchDto() {
        BookmakerMatchDto dto = BookmakerMatchDto.builder()
                .id("bm_padres_giants")
                .sport("baseball")
                .league("MLB")
                .timeRaw("9/12 1:05pm PT")
                .team1("San Diego Padres")
                .team2("San Francisco Giants")
                .spread1("-3")
                .spread2("+3")
                .total("11½")
                .ml1("-1900")
                .ml2("+900")
                .eventUrl("https://lines.bookmaker.eu/en/sports/baseball/")
                .isLive(false)
                .build();

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(dto, SportType.BASEBALL);

        assertNotNull(request);
        assertEquals("bookmaker", request.getBookmaker());
        assertTrue(request.getRegions().contains(BookmakerRegion.US));
        assertEquals("San Diego Padres", request.getTeam1());
        assertEquals("San Francisco Giants", request.getTeam2());
        assertEquals("MLB", request.getLeagueName());
        assertEquals(6, request.getOdds().size());

        boolean foundMlHome = false;
        boolean foundMlAway = false;
        boolean foundSpreadAway = false;
        boolean foundTotalOver = false;

        for (OddItem item : request.getOdds()) {
            assertTrue(item.getValue() > 1.0);
            assertNotNull(item.getBetType());

            if (item.getBetType() instanceof MatchResultBet mrb) {
                if (MatchResultBet.Outcome.WIN1.equals(mrb.outcome())) {
                    foundMlAway = true;
                    assertEquals(1.053, item.getValue(), 0.001);
                } else if (MatchResultBet.Outcome.WIN2.equals(mrb.outcome())) {
                    foundMlHome = true;
                    assertEquals(10.000, item.getValue(), 0.001);
                }
            } else if (item.getBetType() instanceof HandicapBet hb && HandicapBet.Outcome.TEAM1.equals(hb.outcome())) {
                foundSpreadAway = true;
                assertEquals(-3.0, hb.param());
            } else if (item.getBetType() instanceof TotalBet tb && TotalBet.Direction.OVER.equals(tb.direction())) {
                foundTotalOver = true;
                assertEquals(11.5, tb.param());
            }
        }

        assertTrue(foundMlAway, "Must map Moneyline Padres (Away)");
        assertTrue(foundMlHome, "Must map Moneyline Giants (Home)");
        assertTrue(foundSpreadAway, "Must map Spread Padres -3.0");
        assertTrue(foundTotalOver, "Must map Total Over 11.5");
    }

    @Test
    void testNoYieldCapGoldenRule9() {
        BookmakerMatchDto dto = BookmakerMatchDto.builder()
                .id("bm_huge_dog")
                .sport("boxing")
                .league("Championship")
                .team1("Heavy Underdog")
                .team2("World Champion")
                .ml1("+9900") // 100.0x payout!
                .ml2("-50000")
                .build();

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(dto, SportType.BOXING);
        assertNotNull(request);

        OddItem dogOdd = request.getOdds().stream()
                .filter(o -> o.getName().equals("Heavy Underdog"))
                .findFirst()
                .orElse(null);

        assertNotNull(dogOdd, "Huge underdog odd must be retained");
        assertEquals(100.0, dogOdd.getValue(), 0.001);
    }
}

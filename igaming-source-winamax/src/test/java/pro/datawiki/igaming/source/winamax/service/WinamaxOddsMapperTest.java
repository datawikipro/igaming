package pro.datawiki.igaming.source.winamax.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.winamax.dto.WinamaxEventDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WinamaxOddsMapperTest {

    private WinamaxOddsMapper oddsMapper;

    @BeforeEach
    public void setUp() {
        oddsMapper = new WinamaxOddsMapper();
    }

    @Test
    public void testMapMatchResultOdds() {
        WinamaxEventDto event = new WinamaxEventDto();
        event.setId("evt-1001");
        event.setHomeTeam("Team Home");
        event.setAwayTeam("Team Away");
        event.setIsLive(false);

        WinamaxEventDto.WinamaxMarketDto market = new WinamaxEventDto.WinamaxMarketDto();
        market.setName("1X2 Winner");

        WinamaxEventDto.WinamaxOutcomeDto o1 = new WinamaxEventDto.WinamaxOutcomeDto();
        o1.setName("Home 1");
        o1.setOdds(2.45);

        WinamaxEventDto.WinamaxOutcomeDto oX = new WinamaxEventDto.WinamaxOutcomeDto();
        oX.setName("Draw X");
        oX.setOdds(3.30);

        WinamaxEventDto.WinamaxOutcomeDto o2 = new WinamaxEventDto.WinamaxOutcomeDto();
        o2.setName("Away 2");
        o2.setOdds(3.10);

        market.setOutcomes(List.of(o1, oX, o2));
        event.setMarkets(List.of(market));

        List<OddItem> items = oddsMapper.mapOdds(event);
        assertEquals(3, items.size());

        OddItem win1 = items.stream().filter(i -> i.getBetType() instanceof MatchResultBet m && m.outcome() == MatchResultBet.Outcome.WIN1).findFirst().orElse(null);
        assertNotNull(win1);
        assertEquals(2.45, win1.getValue(), 0.001);
    }

    @Test
    public void testMapTotalsOdds() {
        WinamaxEventDto event = new WinamaxEventDto();
        event.setId("evt-1002");
        event.setIsLive(false);

        WinamaxEventDto.WinamaxMarketDto market = new WinamaxEventDto.WinamaxMarketDto();
        market.setName("Total Over/Under 2.5");
        market.setLine(2.5);

        WinamaxEventDto.WinamaxOutcomeDto over = new WinamaxEventDto.WinamaxOutcomeDto();
        over.setName("Over");
        over.setOdds(1.85);

        WinamaxEventDto.WinamaxOutcomeDto under = new WinamaxEventDto.WinamaxOutcomeDto();
        under.setName("Under");
        under.setOdds(1.95);

        market.setOutcomes(List.of(over, under));
        event.setMarkets(List.of(market));

        List<OddItem> items = oddsMapper.mapOdds(event);
        assertEquals(2, items.size());

        OddItem overItem = items.stream().filter(i -> i.getBetType() instanceof TotalBet t && t.direction() == TotalBet.Direction.OVER).findFirst().orElse(null);
        assertNotNull(overItem);
        assertEquals(1.85, overItem.getValue(), 0.001);
    }
}
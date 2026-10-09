package pro.datawiki.igaming.source.caliente.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalienteOddsMapperTest {

    private CalienteOddsMapper oddsMapper;

    @BeforeEach
    public void setUp() {
        oddsMapper = new CalienteOddsMapper();
    }

    @Test
    public void testMapMatchResultOdds() {
        CalienteEventDto event = new CalienteEventDto();
        event.setId("evt-1001");
        event.setHomeTeam("Team Home");
        event.setAwayTeam("Team Away");
        event.setIsLive(false);

        CalienteEventDto.CalienteMarketDto market = new CalienteEventDto.CalienteMarketDto();
        market.setName("1X2 Winner");

        CalienteEventDto.CalienteOutcomeDto o1 = new CalienteEventDto.CalienteOutcomeDto();
        o1.setName("Home 1");
        o1.setOdds(2.45);

        CalienteEventDto.CalienteOutcomeDto oX = new CalienteEventDto.CalienteOutcomeDto();
        oX.setName("Draw X");
        oX.setOdds(3.30);

        CalienteEventDto.CalienteOutcomeDto o2 = new CalienteEventDto.CalienteOutcomeDto();
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
        CalienteEventDto event = new CalienteEventDto();
        event.setId("evt-1002");
        event.setIsLive(false);

        CalienteEventDto.CalienteMarketDto market = new CalienteEventDto.CalienteMarketDto();
        market.setName("Total Over/Under 2.5");
        market.setLine(2.5);

        CalienteEventDto.CalienteOutcomeDto over = new CalienteEventDto.CalienteOutcomeDto();
        over.setName("Over");
        over.setOdds(1.85);

        CalienteEventDto.CalienteOutcomeDto under = new CalienteEventDto.CalienteOutcomeDto();
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
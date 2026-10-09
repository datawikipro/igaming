package pro.datawiki.igaming.source.betrivers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiBetOfferDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiEventDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetriversOddsMapperTest {

    private BetriversOddsMapper oddsMapper;

    @BeforeEach
    void setUp() {
        oddsMapper = new BetriversOddsMapper();
    }

    @Test
    void testSupportsBetriversAndKambi() {
        assertTrue(oddsMapper.supports("betrivers", SportType.AMERICAN_FOOTBALL));
        assertTrue(oddsMapper.supports("kambi", SportType.FOOTBALL));
        assertTrue(oddsMapper.supports("BETRIVERS", SportType.BASKETBALL));
        assertFalse(oddsMapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMapKambiEventAndBetOffers() {
        KambiEventDto event = new KambiEventDto();
        event.setId(1028636016L);
        event.setName("Weber State @ Colorado");
        event.setHomeName("Colorado");
        event.setAwayName("Weber State");
        event.setSport("AMERICAN_FOOTBALL");
        event.setGroup("NCAA");
        event.setState("NOT_STARTED");
        event.setStart("2026-09-12T19:30:00Z");

        List<KambiBetOfferDto> betOffers = new ArrayList<>();

        // 1. Moneyline
        KambiBetOfferDto mlOffer = new KambiBetOfferDto();
        mlOffer.setId(2001L);
        KambiBetOfferDto.KambiCriterionDto mlCrit = new KambiBetOfferDto.KambiCriterionDto();
        mlCrit.setLabel("Moneyline");
        mlCrit.setEnglishLabel("Moneyline");
        mlOffer.setCriterion(mlCrit);

        KambiOutcomeDto ml1 = new KambiOutcomeDto();
        ml1.setId(3001L);
        ml1.setLabel("Colorado");
        ml1.setType("OT_ONE");
        ml1.setOdds(1250); // 1.250

        KambiOutcomeDto ml2 = new KambiOutcomeDto();
        ml2.setId(3002L);
        ml2.setLabel("Weber State");
        ml2.setType("OT_TWO");
        ml2.setOdds(4100); // 4.100

        mlOffer.setOutcomes(List.of(ml1, ml2));
        betOffers.add(mlOffer);

        // 2. Point Spread
        KambiBetOfferDto spreadOffer = new KambiBetOfferDto();
        spreadOffer.setId(2002L);
        KambiBetOfferDto.KambiCriterionDto spreadCrit = new KambiBetOfferDto.KambiCriterionDto();
        spreadCrit.setLabel("Point Spread");
        spreadCrit.setEnglishLabel("Point Spread");
        spreadOffer.setCriterion(spreadCrit);

        KambiOutcomeDto sp1 = new KambiOutcomeDto();
        sp1.setId(3003L);
        sp1.setLabel("Colorado");
        sp1.setType("OT_ONE");
        sp1.setLine(-10.5);
        sp1.setOdds(1910); // 1.910

        KambiOutcomeDto sp2 = new KambiOutcomeDto();
        sp2.setId(3004L);
        sp2.setLabel("Weber State");
        sp2.setType("OT_TWO");
        sp2.setLine(10.5);
        sp2.setOdds(1910); // 1.910

        spreadOffer.setOutcomes(List.of(sp1, sp2));
        betOffers.add(spreadOffer);

        // 3. Total Points
        KambiBetOfferDto totalOffer = new KambiBetOfferDto();
        totalOffer.setId(2003L);
        KambiBetOfferDto.KambiCriterionDto totCrit = new KambiBetOfferDto.KambiCriterionDto();
        totCrit.setLabel("Total Points");
        totCrit.setEnglishLabel("Total Points");
        totalOffer.setCriterion(totCrit);

        KambiOutcomeDto tOver = new KambiOutcomeDto();
        tOver.setId(3005L);
        tOver.setLabel("Over");
        tOver.setType("OT_OVER");
        tOver.setLine(52.5);
        tOver.setOdds(1950); // 1.950

        KambiOutcomeDto tUnder = new KambiOutcomeDto();
        tUnder.setId(3006L);
        tUnder.setLabel("Under");
        tUnder.setType("OT_UNDER");
        tUnder.setLine(52.5);
        tUnder.setOdds(1870); // 1.870

        totalOffer.setOutcomes(List.of(tOver, tUnder));
        betOffers.add(totalOffer);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                event, betOffers, SportType.AMERICAN_FOOTBALL, "American Football", "NCAA");

        assertNotNull(request);
        assertEquals("betrivers", request.getBookmaker());
        assertTrue(request.getRegions().contains(BookmakerRegion.US));
        assertEquals("Colorado", request.getTeam1());
        assertEquals("Weber State", request.getTeam2());
        assertEquals("1028636016", request.getExternalEventId());
        assertEquals(6, request.getOdds().size());

        boolean foundMlHome = false;
        boolean foundSpreadHome = false;
        boolean foundTotalOver = false;

        for (OddItem item : request.getOdds()) {
            assertTrue(item.getValue() > 1.0);
            assertNotNull(item.getBetType());

            if (item.getBetType() instanceof MatchResultBet mrb && MatchResultBet.Outcome.WIN1.equals(mrb.outcome())) {
                foundMlHome = true;
                assertEquals(1.250, item.getValue(), 0.001);
            } else if (item.getBetType() instanceof HandicapBet hb && HandicapBet.Outcome.TEAM1.equals(hb.outcome())) {
                foundSpreadHome = true;
                assertEquals(-10.5, hb.param());
                assertEquals(1.910, item.getValue(), 0.001);
            } else if (item.getBetType() instanceof TotalBet tb && TotalBet.Direction.OVER.equals(tb.direction())) {
                foundTotalOver = true;
                assertEquals(52.5, tb.param());
                assertEquals(1.950, item.getValue(), 0.001);
            }
        }

        assertTrue(foundMlHome, "Must map Moneyline Colorado (Home)");
        assertTrue(foundSpreadHome, "Must map Spread Colorado -10.5");
        assertTrue(foundTotalOver, "Must map Total Over 52.5");
    }

    @Test
    void testNoYieldCapGoldenRule9() {
        KambiEventDto event = new KambiEventDto();
        event.setId(9999L);
        event.setName("Underdog @ Giant");
        event.setHomeName("Giant");
        event.setAwayName("Underdog");

        KambiBetOfferDto offer = new KambiBetOfferDto();
        offer.setId(101L);
        KambiBetOfferDto.KambiCriterionDto crit = new KambiBetOfferDto.KambiCriterionDto();
        crit.setLabel("Moneyline");
        offer.setCriterion(crit);

        // Huge odds 150.0 (150000 millicents)
        KambiOutcomeDto oc = new KambiOutcomeDto();
        oc.setId(201L);
        oc.setLabel("Underdog");
        oc.setType("OT_TWO");
        oc.setOdds(150000);

        offer.setOutcomes(List.of(oc));

        OddsUpdateRequest req = oddsMapper.mapToOddsUpdateRequest(
                event, List.of(offer), SportType.AMERICAN_FOOTBALL, "Football", "NFL");

        assertNotNull(req);
        assertEquals(1, req.getOdds().size());
        assertEquals(150.0, req.getOdds().get(0).getValue(), 0.001);
    }
}

package pro.datawiki.igaming.source.betrivers.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiBetOfferDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiEventDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiOutcomeDto;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;
import pro.datawiki.igaming.source.core.engine.kambi.service.AbstractKambiOddsMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BetriversOddsMapper extends AbstractKambiOddsMapper {

    public BetriversOddsMapper() {
        this(null, null, null);
    }

    public BetriversOddsMapper(UnmappedBetService unmappedBetService,
                               SportNormalizationService sportNormalizationService,
                               BetTypeResolverService betTypeResolver) {
        super(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Override
    public String getBookmakerName() {
        return "betrivers";
    }

    @Override
    public List<BookmakerRegion> getRegions() {
        return List.of(BookmakerRegion.US, BookmakerRegion.GLOBAL);
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(KambiEventDto event,
                                                    List<KambiBetOfferDto> betOffers,
                                                    SportType sportType,
                                                    String sportName,
                                                    String leagueName) {
        if (event == null) return null;

        MatchCache cached = new MatchCache();
        cached.setExternalId(String.valueOf(event.getId()));
        cached.setSportName(sportName != null ? sportName : "General");
        cached.setLeagueName(leagueName != null ? leagueName : "League");

        String team1 = event.getHomeName();
        String team2 = event.getAwayName();
        if ((team1 == null || team2 == null) && event.getName() != null) {
            String desc = event.getName();
            if (desc.contains(" - ")) {
                String[] parts = desc.split(" - ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (desc.contains(" vs ")) {
                String[] parts = desc.split(" vs ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (desc.contains(" @ ")) {
                String[] parts = desc.split(" @ ", 2);
                team2 = parts[0].trim(); // Away @ Home
                team1 = parts[1].trim();
            }
        }
        cached.setTeam1(team1 != null ? team1 : "Home Team");
        cached.setTeam2(team2 != null ? team2 : "Away Team");
        cached.setIsLive("STARTED".equalsIgnoreCase(event.getState()));

        if (event.getStart() != null) {
            try {
                cached.setStartTime(Instant.parse(event.getStart()).toEpochMilli());
            } catch (Exception e) {
                log.debug("Failed to parse start time '{}' for event {}", event.getStart(), event.getId());
            }
        }

        cached.setEventUrl("https://ny.betrivers.com/?page=sportsbook#event/" + event.getId());

        List<KambiBetOffer> coreOffers = new ArrayList<>();
        if (betOffers != null) {
            for (KambiBetOfferDto dto : betOffers) {
                KambiBetOffer core = convertToCore(dto);
                if (core != null) coreOffers.add(core);
            }
        }

        return super.mapToOddsUpdateRequest(cached, coreOffers);
    }

    private KambiBetOffer convertToCore(KambiBetOfferDto dto) {
        if (dto == null) return null;
        KambiBetOffer offer = new KambiBetOffer();
        offer.setId(dto.getId());
        offer.setEventId(dto.getEventId());

        if (dto.getCriterion() != null) {
            KambiBetOffer.KambiCriterion crit = new KambiBetOffer.KambiCriterion();
            crit.setId(dto.getCriterion().getId());
            crit.setLabel(dto.getCriterion().getLabel());
            crit.setEnglishLabel(dto.getCriterion().getEnglishLabel());
            offer.setCriterion(crit);
        }

        if (dto.getBetOfferType() != null) {
            KambiBetOffer.KambiBetOfferType bot = new KambiBetOffer.KambiBetOfferType();
            bot.setId(dto.getBetOfferType().getId());
            bot.setName(dto.getBetOfferType().getName());
            offer.setBetOfferType(bot);
        }

        if (dto.getOutcomes() != null) {
            List<KambiOutcome> outcomes = new ArrayList<>();
            for (KambiOutcomeDto o : dto.getOutcomes()) {
                KambiOutcome out = new KambiOutcome();
                out.setId(o.getId());
                out.setLabel(o.getLabel());
                out.setEnglishLabel(o.getEnglishLabel());
                out.setOdds(o.getOdds());
                out.setOddsAmerican(o.getOddsAmerican());
                out.setType(o.getType());
                out.setLine(o.getLine());
                out.setStatus(o.getStatus());
                outcomes.add(out);
            }
            offer.setOutcomes(outcomes);
        }

        return offer;
    }
}

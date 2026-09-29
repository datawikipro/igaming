package pro.datawiki.igaming.source.stoiximan.service;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.source.core.engine.kambi.service.AbstractKambiOddsMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.List;

@Component
public class StoiximanOddsMapper extends AbstractKambiOddsMapper {

    public StoiximanOddsMapper(UnmappedBetService unmappedBetService,
                               SportNormalizationService sportNormalizationService) {
        super(unmappedBetService, sportNormalizationService);
    }

    @Override
    public String getBookmakerName() {
        return "stoiximan";
    }

    @Override
    public List<BookmakerRegion> getRegions() {
        return List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU);
    }
}

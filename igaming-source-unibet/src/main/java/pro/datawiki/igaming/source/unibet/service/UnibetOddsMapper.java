package pro.datawiki.igaming.source.unibet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.source.core.engine.kambi.service.AbstractKambiOddsMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.List;

@Component
@Slf4j
public class UnibetOddsMapper extends AbstractKambiOddsMapper {

    @Value("${app.bookmaker.name:unibet}")
    private String bookmakerName;

    @org.springframework.beans.factory.annotation.Autowired
    public UnibetOddsMapper(UnmappedBetService unmappedBetService,
                            SportNormalizationService sportNormalizationService,
                            @org.springframework.context.annotation.Lazy pro.datawiki.igaming.source.core.service.BetTypeResolverService betTypeResolver) {
        super(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Override
    public String getBookmakerName() {
        return bookmakerName != null && !bookmakerName.isBlank() ? bookmakerName : "unibet";
    }

    @Override
    public List<BookmakerRegion> getRegions() {
        return List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU);
    }
}

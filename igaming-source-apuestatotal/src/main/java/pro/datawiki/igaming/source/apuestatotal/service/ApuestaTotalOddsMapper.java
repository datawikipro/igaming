package pro.datawiki.igaming.source.apuestatotal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarOddsMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

@Component
public class ApuestaTotalOddsMapper extends AbstractAltenarOddsMapper {

    @Value("${app.bookmaker.name:apuestatotal}")
    private String bookmakerName;

    public ApuestaTotalOddsMapper(UnmappedBetService unmappedBetService,
                            SportNormalizationService sportNormalizationService,
                            BetTypeResolverService betTypeResolver) {
        super(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Override
    public String getBookmakerName() {
        return (bookmakerName != null && !bookmakerName.isEmpty()) ? bookmakerName : "apuestatotal";
    }
}

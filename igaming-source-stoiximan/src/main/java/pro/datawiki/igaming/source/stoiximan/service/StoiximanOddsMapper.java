package pro.datawiki.igaming.source.stoiximan.service;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.source.core.engine.kambi.service.AbstractKambiOddsMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.List;

@Component
public class StoiximanOddsMapper extends AbstractKambiOddsMapper {

    @org.springframework.beans.factory.annotation.Value("${app.bookmaker.name:stoiximan}")
    private String bookmakerName = "stoiximan";

    @org.springframework.beans.factory.annotation.Value("${app.bookmaker.regions:GLOBAL,EU}")
    private String regionsConfig = "GLOBAL,EU";

    public StoiximanOddsMapper(SportNormalizationService sportNormalizationService) {
        super(null, sportNormalizationService);
    }

    @Override
    public String getBookmakerName() {
        return bookmakerName != null && !bookmakerName.isBlank() ? bookmakerName : "stoiximan";
    }

    @Override
    public List<BookmakerRegion> getRegions() {
        if (regionsConfig != null && !regionsConfig.isBlank()) {
            java.util.List<BookmakerRegion> parsed = new java.util.ArrayList<>();
            for (String part : regionsConfig.split(",")) {
                try {
                    parsed.add(BookmakerRegion.valueOf(part.trim().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }
        return List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU);
    }
}

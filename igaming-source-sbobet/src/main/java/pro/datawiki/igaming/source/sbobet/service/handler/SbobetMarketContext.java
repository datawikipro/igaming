package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.dto.SportType;

/**
 * Context object carrying event-level metadata during SBOBET market parsing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SbobetMarketContext {
    private String sportName;
    private SportType sportType;
    private String leagueName;
    private String team1;
    private String team2;
    private boolean isLive;
    private String externalEventId;
    private JsonNode eventNode;

    public boolean isEsports() {
        return AbstractSbobetMarketHandler.isEsports(sportType);
    }
}

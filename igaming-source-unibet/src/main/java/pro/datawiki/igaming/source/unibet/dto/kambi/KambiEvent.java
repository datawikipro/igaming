package pro.datawiki.igaming.source.unibet.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiEvent {
    private Long id;
    private String name; // e.g. "Manchester United - Liverpool"
    private String homeName;
    private String awayName;
    private String start; // e.g. "2026-06-02T15:00:00Z"
    private String state; // e.g. "NOT_STARTED", "STARTED"
    private String group;
    private List<KambiPath> path;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiPath {
        private Long id;
        private String name;
        private String termKey; // e.g. "football", "england", "premier_league"
    }
}

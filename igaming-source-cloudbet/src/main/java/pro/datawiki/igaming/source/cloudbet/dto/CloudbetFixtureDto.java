package pro.datawiki.igaming.source.cloudbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudbetFixtureDto {
    private String id;
    private String name;
    private TeamRef home;
    private TeamRef away;
    private String cutoffTime;
    private String status;
    private SportRef sport;
    private CompetitionRef competition;
    private Map<String, CloudbetMarketDto> markets;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamRef {
        private String key;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SportRef {
        private String key;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompetitionRef {
        private String key;
        private String name;
        private String category;
    }
}

package pro.datawiki.igaming.source.stake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StakeFixtureDto {
    private String id;
    private String slug;
    private String name;
    private String status;
    private Object startTime; // can be String or Long
    private SportRef sport;
    private TournamentRef tournament;
    private List<CompetitorRef> competitors;
    private List<StakeMarketDto> markets;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SportRef {
        private String id;
        private String name;
        private String slug;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TournamentRef {
        private String id;
        private String name;
        private String slug;
        private CategoryRef category;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryRef {
        private String id;
        private String name;
        private String slug;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompetitorRef {
        private String id;
        private String name;
        private String qualifier; // "home" or "away"
    }
}

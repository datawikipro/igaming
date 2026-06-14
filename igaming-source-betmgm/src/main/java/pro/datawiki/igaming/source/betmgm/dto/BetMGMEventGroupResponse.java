package pro.datawiki.igaming.source.betmgm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetMGMEventGroupResponse {
    
    private List<Fixture> fixtures;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fixture {
        private String id;
        private FixtureName name;
        private String startDate;
        private List<OptionMarket> optionMarkets;
        private List<Game> games;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FixtureName {
        private String value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Game {
        private String id;
        private FixtureName name;
        private List<OptionMarket> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OptionMarket {
        private String id;
        private String name;
        private List<Option> options;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Option {
        private String id;
        private FixtureName name;
        private Double price;
        private Double americanPrice;
    }
}

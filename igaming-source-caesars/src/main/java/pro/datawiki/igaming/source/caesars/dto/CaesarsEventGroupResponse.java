package pro.datawiki.igaming.source.caesars.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaesarsEventGroupResponse {
    
    private List<Competition> competitions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Competition {
        private String id;
        private String name;
        private List<Event> events;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        private String id;
        private String name;
        private String startTime;
        private Boolean started;
        private List<Market> markets;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Market {
        private String id;
        private String name;
        private String type;
        private Double line;
        private List<Selection> selections;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Selection {
        private String id;
        private String name;
        private Price price;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        private Double d; // Decimal odds
        private Integer a; // American odds
    }
}

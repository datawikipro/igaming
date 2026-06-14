package pro.datawiki.igaming.source.fanduel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FanDuelEventGroupResponse {
    
    private Attachments attachments;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachments {
        private Map<String, FanDuelEvent> events;
        private Map<String, FanDuelMarket> markets;
        private Map<String, FanDuelSelection> selections;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FanDuelEvent {
        private Integer eventId;
        private String name;
        private String openDate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FanDuelMarket {
        private String marketId;
        private Integer eventId;
        private String marketName;
        private String marketType;
        private String marketStatus;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FanDuelSelection {
        private String selectionId;
        private String marketId;
        private String name;
        private String type;
        private Double trueOdds;
        private Price price;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        private Double decimal;
        private Integer american;
    }
}

package pro.datawiki.igaming.source.fanduel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FanDuelEventGroupResponse {

    private Attachments attachments;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachments {
        /** Key: eventId (as string) */
        private Map<String, FanDuelEvent> events;
        /** Key: marketId */
        private Map<String, FanDuelMarket> markets;
        /** Key: selectionId */
        private Map<String, FanDuelSelection> selections;
        /** Key: eventGroupId */
        private Map<String, FanDuelEventGroup> eventGroups;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FanDuelEvent {
        private Long eventId;
        private String name;
        /** ISO-8601 string, e.g. "2025-01-12T18:00:00.000Z" */
        private String openDate;
        private String eventStatus;
        private Boolean inPlay;
        private Long eventGroupId;
        private String sportId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FanDuelMarket {
        private String marketId;
        private Long eventId;
        private String marketName;
        private String marketType;
        private String marketStatus;
        /** e.g. "ACTIVE", "SUSPENDED" */
        private String status;
        private Double handicap;
        private Double totalPoints;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FanDuelSelection {
        private String selectionId;
        private String marketId;
        private String name;
        private String type;
        /** Direct decimal odds if provided */
        private Double trueOdds;
        private Price price;
        /** Runner handicap / line (e.g. +2.5 for spread) */
        private Double handicap;
        private String result;
        private String status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        @JsonProperty("d")
        private Double decimal;
        @JsonProperty("a")
        private Integer american;
        /** Fractional numerator */
        @JsonProperty("n")
        private Integer numerator;
        /** Fractional denominator */
        @JsonProperty("den")
        private Integer denominator;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FanDuelEventGroup {
        private Long eventGroupId;
        private String name;
        private String sport;
    }
}

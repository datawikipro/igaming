package pro.datawiki.igaming.source.winamax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WinamaxEventDto {
    private String id;
    private String name;
    private String sportName;
    private String categoryName;
    private String homeTeam;
    private String awayTeam;
    private Long startTime;
    private Boolean isLive;
    private List<WinamaxMarketDto> markets;

    public String getSport() {
        return sportName != null ? sportName : "FOOTBALL";
    }

    public String getLeague() {
        return categoryName != null ? categoryName : "General";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WinamaxMarketDto {
        private String id;
        private String name;
        private Double line;
        private List<WinamaxOutcomeDto> outcomes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WinamaxOutcomeDto {
        private String id;
        private String name;
        private Double odds;
        private Double line;
    }
}
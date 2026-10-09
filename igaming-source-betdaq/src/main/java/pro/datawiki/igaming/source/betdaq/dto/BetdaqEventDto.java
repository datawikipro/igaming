package pro.datawiki.igaming.source.betdaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetdaqEventDto {
    private String id;
    private String name;
    private String sportId;
    private String sportName;
    private String competitionName;
    private Long startTime;
    private Boolean isLive;
    private String status;
    private List<BetdaqMarketDto> markets;
}

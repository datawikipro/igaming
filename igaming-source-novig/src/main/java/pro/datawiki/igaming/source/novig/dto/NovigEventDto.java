package pro.datawiki.igaming.source.novig.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NovigEventDto {
    private String id;
    private String description;
    private String status;

    @JsonProperty("scheduled_start")
    private String scheduledStart;

    private String league;
    private List<NovigMarketDto> markets = new ArrayList<>();
}

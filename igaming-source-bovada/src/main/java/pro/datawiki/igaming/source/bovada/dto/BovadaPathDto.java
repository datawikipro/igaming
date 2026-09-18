package pro.datawiki.igaming.source.bovada.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BovadaPathDto {
    private String id;
    private String description;
    private String type; // "SPORT", "LEAGUE"
}

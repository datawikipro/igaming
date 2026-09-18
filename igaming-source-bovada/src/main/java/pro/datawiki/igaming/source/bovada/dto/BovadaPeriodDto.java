package pro.datawiki.igaming.source.bovada.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BovadaPeriodDto {
    private String id;
    private String description;
    private String abbreviation;
    private Boolean live;
    private Boolean main;
}

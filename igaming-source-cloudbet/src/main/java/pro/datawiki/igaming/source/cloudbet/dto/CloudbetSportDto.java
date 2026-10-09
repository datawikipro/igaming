package pro.datawiki.igaming.source.cloudbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudbetSportDto {
    private String key;
    private String name;
}

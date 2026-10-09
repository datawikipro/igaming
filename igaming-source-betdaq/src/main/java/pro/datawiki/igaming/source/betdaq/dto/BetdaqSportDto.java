package pro.datawiki.igaming.source.betdaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetdaqSportDto {
    private String id;
    private String name;
    private String code;
}

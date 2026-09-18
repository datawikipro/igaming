package pro.datawiki.igaming.source.bovada.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BovadaPriceDto {
    private String id;
    private String decimal;
    private String american;
    private String fractional;
    private String handicap;
}

package pro.datawiki.igaming.source.bovada.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BovadaDisplayGroupDto {
    private String id;
    private String description;
    private List<BovadaMarketDto> markets;
}

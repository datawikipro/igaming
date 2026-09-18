package pro.datawiki.igaming.source.bovada.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BovadaEventGroupDto {
    private String id;
    private List<BovadaPathDto> path;
    private List<BovadaEventDto> events;
}

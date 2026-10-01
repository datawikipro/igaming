package pro.datawiki.igaming.source.betesporte.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetesporteResponseDto {

    @Builder.Default
    private List<BetesporteEventDto> events = new ArrayList<>();
    private Integer total;
    private Boolean success;
}

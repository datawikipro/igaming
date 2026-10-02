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

    private Boolean success;
    private String message;
    @Builder.Default
    private List<BetesporteEventDto> events = new ArrayList<>();
    @Builder.Default
    private List<BetesporteEventDto> items = new ArrayList<>();
    @Builder.Default
    private List<BetesporteEventDto> data = new ArrayList<>();
}

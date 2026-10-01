package pro.datawiki.igaming.source.caliente.dto;

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
public class CalienteResponseDto {

    @Builder.Default
    private List<CalienteEventDto> events = new ArrayList<>();
    private Integer total;
    private Boolean success;
}

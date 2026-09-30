package pro.datawiki.igaming.source.betway.dto;

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
public class BetwayResponseDto {

    @Builder.Default
    private List<BetwayEventDto> events = new ArrayList<>();
    private Integer totalCount;
}

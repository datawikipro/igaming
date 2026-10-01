package pro.datawiki.igaming.source.tenbet.dto;

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
public class TenBetResponseDto {

    @Builder.Default
    private List<TenBetEventDto> events = new ArrayList<>();
    private Integer totalCount;
}

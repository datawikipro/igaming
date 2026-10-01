package pro.datawiki.igaming.source.wplay.dto;

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
public class WplayResponseDto {

    @Builder.Default
    private List<WplayEventDto> events = new ArrayList<>();
    private Integer totalCount;
}

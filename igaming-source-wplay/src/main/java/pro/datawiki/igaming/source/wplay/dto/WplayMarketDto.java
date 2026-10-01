package pro.datawiki.igaming.source.wplay.dto;

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
public class WplayMarketDto {
    private String id;
    private String name;
    private String group;
    @Builder.Default
    private List<WplayOutcomeDto> outcomes = new ArrayList<>();
}

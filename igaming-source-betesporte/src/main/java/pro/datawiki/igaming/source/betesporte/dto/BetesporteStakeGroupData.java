package pro.datawiki.igaming.source.betesporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetesporteStakeGroupData {
    private Long id;
    private String nameRu;
    private String nameEn;
    private List<BetesporteStakeData> stakes;
}

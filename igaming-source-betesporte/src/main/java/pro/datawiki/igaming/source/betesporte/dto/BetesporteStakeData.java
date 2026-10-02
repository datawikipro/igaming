package pro.datawiki.igaming.source.betesporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetesporteStakeData {
    private Long id;
    private String nameRu;
    private String nameEn;
    private Double factor;
    private Double argument;
}

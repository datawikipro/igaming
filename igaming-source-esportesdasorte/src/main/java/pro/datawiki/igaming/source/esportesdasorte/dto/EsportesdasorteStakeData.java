package pro.datawiki.igaming.source.esportesdasorte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsportesdasorteStakeData {
    private Long id;
    private String nameRu;
    private String nameEn;
    private Double factor;
    private Double argument;
}

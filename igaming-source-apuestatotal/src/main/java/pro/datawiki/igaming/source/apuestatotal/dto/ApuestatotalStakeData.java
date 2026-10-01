package pro.datawiki.igaming.source.apuestatotal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApuestatotalStakeData {
    private Long id;
    private String nameRu;
    private String nameEn;
    private Double factor;
    private Double argument;
}

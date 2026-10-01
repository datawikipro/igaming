package pro.datawiki.igaming.source.vaidebet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaidebetStakeData {
    private Long id;
    private String nameRu;
    private String nameEn;
    private Double factor;
    private Double argument;
}

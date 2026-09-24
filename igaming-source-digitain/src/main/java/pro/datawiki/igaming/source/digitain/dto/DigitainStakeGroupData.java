package pro.datawiki.igaming.source.digitain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitainStakeGroupData {
    private Long id;
    private String nameRu;
    private String nameEn;
    private List<DigitainStakeData> stakes;
}
